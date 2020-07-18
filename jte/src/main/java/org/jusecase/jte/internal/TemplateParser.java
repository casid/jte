package org.jusecase.jte.internal;

import java.util.*;

import org.jusecase.jte.ContentType;

final class TemplateParser {
    private static final int LAYOUT_DEFINITION_DEPTH = 2;

    private final String templateCode;
    private final TemplateType type;
    private final TemplateParserVisitor visitor;
    private final ContentType contentType;
    private final String[] htmlTags;
    private final String[] htmlAttributes;

    private final Deque<Mode> stack = new ArrayDeque<>();
    private final Deque<HtmlTag> htmlStack = new ArrayDeque<>();

    private Mode currentMode;
    private HtmlTag currentHtmlTag;
    private int depth;
    private boolean paramsComplete;
    private boolean tagClosed;

    private int lastIndex = 0;

    @SuppressWarnings("FieldCanBeLocal")
    private char previousChar8;
    private char previousChar7;
    private char previousChar6;
    private char previousChar5;
    private char previousChar4;
    private char previousChar3;
    private char previousChar2;
    private char previousChar1;
    private char previousChar0;
    private char currentChar;

    TemplateParser(String templateCode, TemplateType type, TemplateParserVisitor visitor, ContentType contentType, String[] htmlTags, String[] htmlAttributes) {
        this.templateCode = templateCode;
        this.type = type;
        this.visitor = visitor;
        this.contentType = contentType;
        this.htmlTags = htmlTags;
        this.htmlAttributes = htmlAttributes;
    }

    public void parse() {
        currentMode = Mode.Text;
        stack.push(currentMode);
        depth = 0;

        for (int i = 0; i < templateCode.length(); ++i) {
            previousChar8 = previousChar7;
            previousChar7 = previousChar6;
            previousChar6 = previousChar5;
            previousChar5 = previousChar4;
            previousChar4 = previousChar3;
            previousChar3 = previousChar2;
            previousChar2 = previousChar1;
            previousChar1 = previousChar0;
            previousChar0 = currentChar;
            currentChar = templateCode.charAt(i);

            if (previousChar5 == '@' && previousChar4 == 'i' && previousChar3 == 'm' && previousChar2 == 'p' && previousChar1 == 'o' && previousChar0 == 'r' && currentChar == 't') {
                push(Mode.Import);
                lastIndex = i + 1;
            } else if (currentMode == Mode.Import && currentChar == '\n') {
                extract(templateCode, lastIndex, i, (depth, content) -> visitor.onImport(content.trim()));
                pop();
                lastIndex = i + 1;
            } else if (previousChar4 == '@' && previousChar3 == 'p' && previousChar2 == 'a' && previousChar1 == 'r' && previousChar0 == 'a' && currentChar == 'm') {
                push(Mode.Param);
                lastIndex = i + 1;
            } else if (currentMode == Mode.Param && currentChar == '\n') {
                extract(templateCode, lastIndex, i, (depth, content) -> visitor.onParam(new ParamInfo(content.trim())));
                pop();
                lastIndex = i + 1;
            } else if (currentMode != Mode.Comment && previousChar2 == '<' && previousChar1 == '%' && previousChar0 == '-' && currentChar == '-') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 3, visitor::onTextPart);
                }
                push(Mode.Comment);
            } else if (currentMode == Mode.Comment) {
                if (previousChar2 == '-' && previousChar1 == '-' && previousChar0 == '%' && currentChar == '>') {
                    pop();
                    lastIndex = i + 1;
                }
            } else if (previousChar0 == '$' && currentChar == '{') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 1, visitor::onTextPart);
                }
                lastIndex = i + 1;
                push(Mode.Code);
            } else if (previousChar6 == '$' && previousChar5 == 'u' && previousChar4 == 'n' && previousChar3 == 's' && previousChar2 == 'a' && previousChar1 == 'f' && previousChar0 == 'e' && currentChar == '{') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 7, visitor::onTextPart);
                }
                lastIndex = i + 1;
                push(Mode.UnsafeCode);
            } else if (previousChar0 == '!' && currentChar == '{') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 1, visitor::onTextPart);
                }
                lastIndex = i + 1;
                push(Mode.CodeStatement);
            } else if (currentChar == '}' && currentMode == Mode.CodeStatement) {
                pop();
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i, visitor::onCodeStatement);
                    lastIndex = i + 1;
                }
            } else if (currentChar == '\"' && currentMode.isJava()) {
                push(Mode.JavaCodeString);
            } else if (currentChar == '\"' && currentMode == Mode.JavaCodeString && previousChar0 != '\\') {
                pop();
            } else if (currentChar == '}' && currentMode == Mode.Code) {
                pop();
                if (currentMode == Mode.Text) {
                    extractCodePart(i);
                }
            } else if (currentChar == '}' && currentMode == Mode.UnsafeCode) {
                pop();
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i, visitor::onUnsafeCodePart);
                    lastIndex = i + 1;
                }
            } else if (previousChar1 == '@' && previousChar0 == 'i' && currentChar == 'f') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 2, visitor::onTextPart);
                    lastIndex = i + 1;
                }

                push(Mode.Condition);
            } else if (currentChar == '(' && (currentMode == Mode.Condition || currentMode == Mode.ConditionElse)) {
                lastIndex = i + 1;
                push(Mode.JavaCode);
            } else if (currentChar == '(' && currentMode.isJava()) {
                push(Mode.JavaCode);
            } else if (currentChar == ')' && currentMode.isJava()) {
                if (currentMode == Mode.JavaCodeParam) {
                    TagOrLayoutMode previousMode = getPreviousMode(TagOrLayoutMode.class);
                    extract(templateCode, lastIndex, i, (d, c) -> {
                        if (c != null && !c.isBlank()) {
                            previousMode.params.add(c);
                        }
                    });
                }

                pop();

                if (currentMode == Mode.Condition) {
                    extract(templateCode, lastIndex, i, visitor::onConditionStart);
                    lastIndex = i + 1;
                    push(Mode.Text);
                } else if (currentMode == Mode.ConditionElse) {
                    extract(templateCode, lastIndex, i, visitor::onConditionElse);
                    lastIndex = i + 1;
                    push(Mode.Text);
                } else if (currentMode instanceof TagMode) {
                    TagMode tagMode = (TagMode) currentMode;
                    extract(templateCode, lastIndex, i, (d, c) -> visitor.onTag(d, tagMode.name.toString(), tagMode.params));
                    lastIndex = i + 1;
                    pop();
                } else if (currentMode instanceof LayoutMode) {
                    LayoutMode layoutMode = (LayoutMode) currentMode;
                    extract(templateCode, lastIndex, i, (d, c) -> visitor.onLayout(d, layoutMode.name.toString(), layoutMode.params));
                }
            } else if (currentChar == ',' && currentMode == Mode.JavaCodeParam) {
                TagOrLayoutMode previousMode = getPreviousMode(TagOrLayoutMode.class);
                extract(templateCode, lastIndex, i, (d, c) -> {
                    if (c != null && !c.isBlank()) {
                        previousMode.params.add(c);
                    }
                });
                lastIndex = i + 1;
            } else if (previousChar3 == '@' && previousChar2 == 'e' && previousChar1 == 'l' && previousChar0 == 's' && currentChar == 'e' && templateCode.charAt(i + 1) != 'i') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 4, visitor::onTextPart);
                }
                lastIndex = i + 1;

                pop();

                visitor.onConditionElse(depth);
                push(Mode.Text);
            } else if (previousChar5 == '@' && previousChar4 == 'e' && previousChar3 == 'l' && previousChar2 == 's' && previousChar1 == 'e' && previousChar0 == 'i' && currentChar == 'f') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 6, visitor::onTextPart);
                }
                lastIndex = i + 1;

                pop();

                if (currentMode == Mode.Condition || currentMode == Mode.ConditionElse) {
                    pop();
                }

                push(Mode.ConditionElse);

            } else if (previousChar4 == '@' && previousChar3 == 'e' && previousChar2 == 'n' && previousChar1 == 'd' && previousChar0 == 'i' && currentChar == 'f') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 5, visitor::onTextPart);
                }
                lastIndex = i + 1;

                pop();

                if (currentMode == Mode.Condition || currentMode == Mode.ConditionElse) {
                    visitor.onConditionEnd(depth);
                    pop();
                }
            } else if (previousChar2 == '@' && previousChar1 == 'f' && previousChar0 == 'o' && currentChar == 'r') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 3, visitor::onTextPart);
                    lastIndex = i + 1;
                }

                push(Mode.ForLoop);
            } else if (currentChar == '(' && currentMode == Mode.ForLoop) {
                lastIndex = i + 1;
                push(Mode.ForLoopCode);
            } else if (currentChar == '(' && currentMode == Mode.ForLoopCode) {
                push(Mode.ForLoopCode);
            } else if (currentChar == ')' && currentMode == Mode.ForLoopCode) {
                pop();
                if (currentMode == Mode.ForLoop) {
                    extract(templateCode, lastIndex, i, visitor::onForLoopStart);
                    lastIndex = i + 1;
                    push(Mode.Text);
                }
            } else if (previousChar5 == '@' && previousChar4 == 'e' && previousChar3 == 'n' && previousChar2 == 'd' && previousChar1 == 'f' && previousChar0 == 'o' && currentChar == 'r') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 6, visitor::onTextPart);
                }
                lastIndex = i + 1;

                pop();

                if (currentMode == Mode.ForLoop) {
                    visitor.onForLoopEnd(depth);
                    pop();
                }
            } else if (previousChar3 == '@' && previousChar2 == 't' && previousChar1 == 'a' && previousChar0 == 'g' && currentChar == '.') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 4, visitor::onTextPart);
                    lastIndex = i + 1;
                }

                push(new TagMode());
                push(Mode.TagName);
            } else if (currentMode == Mode.TagName) {
                if (currentChar == '(') {
                    pop();
                    push(Mode.JavaCodeParam);
                    lastIndex = i + 1;
                } else if (currentChar != ' ') {
                    getPreviousMode(TagOrLayoutMode.class).name.append(currentChar);
                }
            } else if (previousChar6 == '@' && previousChar5 == 'l' && previousChar4 == 'a' && previousChar3 == 'y' && previousChar2 == 'o' && previousChar1 == 'u' && previousChar0 == 't' && currentChar == '.') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 7, visitor::onTextPart);
                    lastIndex = i + 1;
                }

                push(new LayoutMode());
                push(Mode.TagName);
            } else if (previousChar8 == '@' && previousChar7 == 'e' && previousChar6 == 'n' && previousChar5 == 'd' && previousChar4 == 'l' && previousChar3 == 'a' && previousChar2 == 'y' && previousChar1 == 'o' && previousChar0 == 'u' && currentChar == 't') {
                pop();
                lastIndex = i + 1;

                visitor.onLayoutEnd(depth);
            } else if (previousChar5 == '@' && previousChar4 == 'd' && previousChar3 == 'e' && previousChar2 == 'f' && previousChar1 == 'i' && previousChar0 == 'n' && currentChar == 'e') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 6, visitor::onTextPart);
                    lastIndex = i + 1;
                }
                push(Mode.LayoutDefine);
            } else if (currentChar == '(' && currentMode == Mode.LayoutDefine) {
                lastIndex = i + 1;
            } else if (currentChar == ')' && currentMode == Mode.LayoutDefine) {
                extract(templateCode, lastIndex, i, visitor::onLayoutDefine);
                lastIndex = i + 1;
                push(Mode.Text);
                depth += LAYOUT_DEFINITION_DEPTH;
            } else if (previousChar8 == '@' && previousChar7 == 'e' && previousChar6 == 'n' && previousChar5 == 'd' && previousChar4 == 'd' && previousChar3 == 'e' && previousChar2 == 'f' && previousChar1 == 'i' && previousChar0 == 'n' && currentChar == 'e') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 9, visitor::onTextPart);
                }

                pop();
                pop();
                depth -= LAYOUT_DEFINITION_DEPTH;
                lastIndex = i + 1;

                visitor.onLayoutDefineEnd(depth);
            } else if (previousChar5 == '@' && previousChar4 == 'r' && previousChar3 == 'e' && previousChar2 == 'n' && previousChar1 == 'd' && previousChar0 == 'e' && currentChar == 'r') {
                if (type == TemplateType.Layout && currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 6, visitor::onTextPart);
                    lastIndex = i + 1;
                }
                push(Mode.LayoutRender);
            } else if (currentChar == '(' && currentMode == Mode.LayoutRender) {
                lastIndex = i + 1;
            } else if (currentChar == ')' && currentMode == Mode.LayoutRender) {
                extract(templateCode, lastIndex, i, visitor::onLayoutRender);
                lastIndex = i + 1;
                pop();
            } else if (currentMode == Mode.Text && contentType == ContentType.Html) {
                interceptHtmlTags(i);
            }

            if (currentChar == '\n') {
                visitor.onLineFinished();
            }
        }

        if (lastIndex < templateCode.length()) {
            extract(templateCode, lastIndex, templateCode.length(), visitor::onTextPart);
        }

        completeParamsIfRequired();
        visitor.onComplete();
    }

    private void extractCodePart(int i) {
        if (contentType == ContentType.Html) {
            extractHtmlCodePart(i);
        } else {
            extractPlainCodePart(i);
        }
        lastIndex = i + 1;
    }

    private void extractPlainCodePart(int i) {
        extract(templateCode, lastIndex, i, visitor::onCodePart);
    }

    private void extractHtmlCodePart(int i) {
        if (currentHtmlTag != null) {
            if (currentHtmlTag.attributesProcessed) {
                extract(templateCode, lastIndex, i, (depth, codePart) -> visitor.onHtmlTagBodyCodePart(depth, codePart, currentHtmlTag.name));
                return;
            }

            HtmlAttribute currentAttribute = currentHtmlTag.getCurrentAttribute();
            if (currentAttribute != null && currentAttribute.quoteCount < 2) {
                extract(templateCode, lastIndex, i, (depth, codePart) -> visitor.onHtmlTagAttributeCodePart(depth, codePart, currentHtmlTag.name, currentAttribute.name));
                return;
            }
        }

        extract(templateCode, lastIndex, i, (depth, codePart) -> visitor.onHtmlTagBodyCodePart(depth, codePart, "html"));
    }

    private void interceptHtmlTags(int i) {
        if ( isOpeningHtmlTag(i) ) {
            String name = parseHtmlTagName(i + 1);
            if (!name.isEmpty()) {
                validateHtmlTag(name);

                HtmlTag htmlTag = new HtmlTag(name, isHtmlTagIntercepted(name));
                pushHtmlTag(htmlTag);
                tagClosed = false;
            }
        } else if (currentHtmlTag != null) {
            if (!currentHtmlTag.attributesProcessed && currentChar == '=') {
                HtmlAttribute attribute = new HtmlAttribute(parseHtmlAttributeName(i));
                currentHtmlTag.attributes.add(attribute);
            } else if (!currentHtmlTag.attributesProcessed && currentChar == '\"') {
                HtmlAttribute currentAttribute = currentHtmlTag.getCurrentAttribute();
                if (currentAttribute != null) {
                    currentAttribute.quoteCount++;
                    if (currentAttribute.quoteCount == 1) {
                        currentAttribute.startIndex = i + 1;

                        if (isHtmlAttributeIntercepted(currentAttribute.name)) {
                            extract(templateCode, lastIndex, i + 1, visitor::onTextPart);
                            lastIndex = i + 1;

                            visitor.onHtmlAttributeStarted(depth, currentHtmlTag, currentAttribute);
                        }
                    } else if (currentAttribute.quoteCount == 2) {
                        currentAttribute.value = templateCode.substring(currentAttribute.startIndex, i);
                    }
                }
            } else if (!currentHtmlTag.attributesProcessed && previousChar0 == '/' && currentChar == '>') {
                if (currentHtmlTag.intercepted) {
                    extract(templateCode, lastIndex, i - 1, visitor::onTextPart);
                    lastIndex = i - 1;
                    visitor.onHtmlTagOpened(depth, currentHtmlTag);
                }
                currentHtmlTag.attributesProcessed = true;

                popHtmlTag();
            } else if (!currentHtmlTag.attributesProcessed && currentChar == '>') {
                if (tagClosed) {
                    tagClosed = false;
                } else {
                    if (currentHtmlTag.intercepted) {
                        extract(templateCode, lastIndex, i, visitor::onTextPart);
                        lastIndex = i;
                        visitor.onHtmlTagOpened(depth, currentHtmlTag);
                    }
                    currentHtmlTag.attributesProcessed = true;

                    if (currentHtmlTag.bodyIgnored) {
                        popHtmlTag();
                    }
                }
            } else if (previousChar0 == '<' && currentChar == '/') {
                if (templateCode.startsWith(currentHtmlTag.name, i + 1)) {
                    if (!currentHtmlTag.bodyIgnored) {
                        if (currentHtmlTag.intercepted) {
                            extract(templateCode, lastIndex, i - 1, visitor::onTextPart);
                            lastIndex = i - 1;
                            visitor.onHtmlTagClosed(depth, currentHtmlTag);
                        }

                        popHtmlTag();
                    }
                } else if (!currentHtmlTag.innerTagsIgnored) {
                    String tagName = parseHtmlTagName(i + 1);
                    visitor.onError("Unclosed tag <" + currentHtmlTag.name + ">, expected " + "</" + currentHtmlTag.name + ">, got </" + tagName + ">.");
                }
                tagClosed = true;
            }
        }
    }

    private boolean isOpeningHtmlTag(int i) {
        if (currentChar != '<') {
            return false;
        }

        if (templateCode.startsWith("<%--", i)) {
            return false;
        }

        if (currentHtmlTag == null) {
            return true;
        }

        if (currentHtmlTag.innerTagsIgnored) {
            return false;
        }

        return currentHtmlTag.attributesProcessed;
    }

    private void validateHtmlTag( String name ) {
        if ( name.contains("$") ) {
            visitor.onError("Illegal tag name " + name + "! Expressions in tag names are not allowed.");
        }
    }

    private void pushHtmlTag(HtmlTag htmlTag) {
        htmlStack.push(htmlTag);
        currentHtmlTag = htmlTag;
    }

    private void popHtmlTag() {
        htmlStack.pop();
        currentHtmlTag = htmlStack.peek();
    }

    private String parseHtmlTagName(int index) {
        int startIndex = index;
        while (index < templateCode.length()) {
            char c = templateCode.charAt(index);
            if (Character.isWhitespace(c) || c == '/' || c == '>') {
                break;
            }
            ++index;
        }
        return templateCode.substring(startIndex, index);
    }

    private String parseHtmlAttributeName(int index) {
        while (Character.isWhitespace(templateCode.charAt(index))) {
            --index;
        }

        int endIndex = index--;

        while (!Character.isWhitespace(templateCode.charAt(index))) {
            --index;
        }

        return templateCode.substring(index + 1, endIndex);
    }

    private boolean isHtmlTagIntercepted(String name) {
        if (htmlTags != null) {
            for (String htmlTag : htmlTags) {
                if (name.equals(htmlTag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHtmlAttributeIntercepted(String name) {
        if (htmlAttributes != null && currentHtmlTag.intercepted) {
            for (String htmlAttribute : htmlAttributes) {
                if (name.equals(htmlAttribute)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void push(Mode mode) {
        currentMode = mode;
        stack.push(currentMode);
        if (mode == Mode.Text) {
            ++depth;
        }
    }

    private void pop() {
        Mode previousMode = stack.pop();
        if (previousMode == Mode.Text) {
            --depth;
        }

        currentMode = stack.peek();
    }

    @SuppressWarnings({"SameParameterValue", "unchecked"})
    private <T extends Mode> T getPreviousMode(Class<T> modeClass) {
        for (Mode mode : stack) {
            if (modeClass.isAssignableFrom(mode.getClass())) {
                return (T) mode;
            }
        }
        throw new IllegalStateException("Expected mode of type " + modeClass + " on the stack, but found nothing!");
    }

    private void extract(String templateCode, int startIndex, int endIndex, VisitorCallback callback) {
        completeParamsIfRequired();

        if (startIndex < 0) {
            return;
        }
        if (endIndex < startIndex) {
            return;
        }
        callback.accept(depth, templateCode.substring(startIndex, endIndex));
    }

    private void completeParamsIfRequired() {
        if (!paramsComplete && currentMode != Mode.Param && currentMode != Mode.Import) {
            visitor.onParamsComplete();
            paramsComplete = true;
        }
    }

    interface VisitorCallback {
        void accept(int depth, String content);
    }

    private interface Mode {
        Mode Import = new StatelessMode();
        Mode Param = new StatelessMode();
        Mode Text = new StatelessMode();
        Mode Code = new StatelessMode();
        Mode UnsafeCode = new StatelessMode();
        Mode CodeStatement = new StatelessMode();
        Mode Condition = new StatelessMode();
        Mode JavaCode = new StatelessMode(true);
        Mode JavaCodeParam = new StatelessMode(true);
        Mode JavaCodeString = new StatelessMode();
        Mode ConditionElse = new StatelessMode();
        Mode ForLoop = new StatelessMode();
        Mode ForLoopCode = new StatelessMode();
        Mode TagName = new StatelessMode();
        Mode LayoutDefine = new StatelessMode();
        Mode LayoutRender = new StatelessMode();
        Mode Comment = new StatelessMode();

        boolean isJava();
    }

    private static class StatelessMode implements Mode {
        private final boolean java;

        private StatelessMode() {
            this(false);
        }

        private StatelessMode(boolean java) {
            this.java = java;
        }

        @Override
        public boolean isJava() {
            return java;
        }
    }

    private static abstract class TagOrLayoutMode implements Mode {
        final StringBuilder name = new StringBuilder();
        final List<String> params = new ArrayList<>();

        @Override
        public boolean isJava() {
            return false;
        }
    }

    private static class TagMode extends TagOrLayoutMode {
    }

    private static class LayoutMode extends TagOrLayoutMode {
    }

    public static class HtmlTag {

        // See https://www.lifewire.com/html-singleton-tags-3468620
        private static final Set<String> VOID_HTML_TAGS = Set.of("area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr");

        public final String name;
        public final boolean intercepted;
        public final boolean bodyIgnored;
        public final boolean innerTagsIgnored;
        public final List<HtmlAttribute> attributes = new ArrayList<>();
        public boolean attributesProcessed;

        public HtmlTag(String name, boolean intercepted) {
            this.name = name;
            this.intercepted = intercepted;
            this.bodyIgnored = VOID_HTML_TAGS.contains(name);
            this.innerTagsIgnored = "script".equals(name) || "style".equals(name);
        }

        public HtmlAttribute getCurrentAttribute() {
            if (attributes.isEmpty()) {
                return null;
            }
            return attributes.get(attributes.size() - 1);
        }
    }

    public static class HtmlAttribute {
        public final String name;
        public String value;

        public int quoteCount;
        public int startIndex;

        private HtmlAttribute(String name) {
            this.name = name;
        }
    }
}
