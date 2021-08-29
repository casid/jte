package gg.jte.compiler;

import gg.jte.ContentType;
import gg.jte.TemplateConfig;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.runtime.StringUtils;
import gg.jte.runtime.TemplateType;

import java.util.*;

public final class TemplateParser {
    private final String templateCode;
    private final TemplateType type;
    private final TemplateParserVisitor visitor;
    private final TemplateConfig config;
    private final ContentType contentType;
    private final HtmlPolicy htmlPolicy;
    private final String[] htmlTags;
    private final String[] htmlAttributes;
    private final boolean trimControlStructures;
    private final boolean htmlCommentsPreserved;

    private final Deque<Mode> stack = new ArrayDeque<>();
    private final Deque<Indent> indentStack = new ArrayDeque<>();
    private final Deque<HtmlTag> htmlStack = new ArrayDeque<>();

    private Mode currentMode;
    private Mode previousControlStructureTrimmed;
    private List<Mode> initialModes;
    private HtmlTag currentHtmlTag;
    private int depth;
    private boolean paramsComplete;
    private boolean outputPrevented;
    private boolean tagClosed;
    private int i;
    private int startIndex;
    private int endIndex;

    private int lastIndex = 0;
    private int lastLineIndex = 0;
    private int lastTrimmedIndex = -1;

    @SuppressWarnings("FieldCanBeLocal")
    private char previousChar7;
    private char previousChar6;
    private char previousChar5;
    private char previousChar4;
    private char previousChar3;
    private char previousChar2;
    private char previousChar1;
    private char previousChar0;
    private char currentChar;

    public TemplateParser(String templateCode, TemplateType type, TemplateParserVisitor visitor, TemplateConfig config) {
        this.templateCode = templateCode;
        this.type = type;
        this.visitor = visitor;
        this.config = config;
        this.contentType = config.contentType;
        this.htmlPolicy = config.htmlPolicy;
        this.htmlTags = config.htmlTags;
        this.htmlAttributes = config.htmlAttributes;
        this.trimControlStructures = config.trimControlStructures;
        this.htmlCommentsPreserved = config.htmlCommentsPreserved;

        this.startIndex = 0;
        this.endIndex = templateCode.length();
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        this.lastIndex = startIndex;
        this.lastLineIndex = startIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public void setInitialModes(List<Mode> initialModes) {
        this.initialModes = initialModes;
    }

    public void setParamsComplete(boolean paramsComplete) {
        this.paramsComplete = paramsComplete;
    }

    public void parse() {
        parse(0);
    }

    public void parse(int startingDepth) {
        try {
            doParse(startingDepth);
        } catch (HtmlPolicyException e) {
            visitor.onError(e.getMessage());
        }
    }

    private void doParse(int startingDepth) {
        currentMode = Mode.Text;
        stack.push(currentMode);

        if (initialModes != null) {
            for (Mode initialMode : initialModes) {
                push(initialMode);
            }
        }

        depth = startingDepth;

        for (i = startIndex; i < endIndex; ++i) {
            previousChar7 = previousChar6;
            previousChar6 = previousChar5;
            previousChar5 = previousChar4;
            previousChar4 = previousChar3;
            previousChar3 = previousChar2;
            previousChar2 = previousChar1;
            previousChar1 = previousChar0;
            previousChar0 = currentChar;
            currentChar = templateCode.charAt(i);

            if (!currentMode.isComment() && previousChar5 == '@' && previousChar4 == 'i' && previousChar3 == 'm' && previousChar2 == 'p' && previousChar1 == 'o' && previousChar0 == 'r' && currentChar == 't' && isParamOrImportAllowed()) {
                push(Mode.Import);
                lastIndex = i + 1;
            } else if (currentMode == Mode.Import && currentChar == '\n') {
                extract(templateCode, lastIndex, i, (depth, content) -> visitor.onImport(content.trim()));
                pop();
                lastIndex = i + 1;
            } else if (!currentMode.isComment() && previousChar4 == '@' && previousChar3 == 'p' && previousChar2 == 'a' && previousChar1 == 'r' && previousChar0 == 'a' && currentChar == 'm' && isParamOrImportAllowed()) {
                push(Mode.Param);
                lastIndex = i + 1;
            } else if (currentMode == Mode.Param && currentChar == '\n') {
                extract(templateCode, lastIndex, i, (depth, content) -> visitor.onParam(content.trim()));
                pop();
                lastIndex = i + 1;
            } else if (isCommentAllowed() && previousChar2 == '<' && previousChar1 == '%' && previousChar0 == '-' && currentChar == '-') {
                extractComment(Mode.Comment, i - 3);
            } else if (isCommentAllowed() && previousChar2 == '<' && previousChar1 == '!' && previousChar0 == '-' && currentChar == '-' && isHtmlCommentAllowed()) {
                extractComment(Mode.HtmlComment, i - 3);
            } else if (isCommentAllowed() && previousChar0 == '/' && currentChar == '*' && isCssCommentAllowed()) {
                extractComment(Mode.CssComment, i - 1);
            } else if (isCommentAllowed() && previousChar0 == '/' && currentChar == '/' && isJsCommentAllowed()) {
                extractComment(Mode.JsComment, i - 1);
            } else if (isCommentAllowed() && previousChar0 == '/' && currentChar == '*' && isJsCommentAllowed()) {
                extractComment(Mode.JsBlockComment, i - 1);
            } else if (currentMode == Mode.Comment) {
                if (previousChar2 == '-' && previousChar1 == '-' && previousChar0 == '%' && currentChar == '>') {
                    pop();
                    lastIndex = i + 1;
                }
            } else if (currentMode == Mode.HtmlComment) {
                if (previousChar1 == '-' && previousChar0 == '-' && currentChar == '>') {
                    pop();
                    lastIndex = i + 1;
                }
            } else if (currentMode == Mode.CssComment || currentMode == Mode.JsBlockComment) {
                if (previousChar0 == '*' && currentChar == '/') {
                    pop();
                    lastIndex = i + 1;
                }
            } else if (currentMode == Mode.JsComment) {
                if (currentChar == '\n') {
                    pop();
                    lastIndex = i + 1;
                } else if (previousChar7 == '<' && previousChar6 == '/' && previousChar5 == 's' && previousChar4 == 'c' && previousChar3 == 'r' && previousChar2 == 'i' && previousChar1 == 'p' && previousChar0 == 't' && currentChar == '>') {
                    pop();
                    lastIndex = i - 8;
                }
            } else if (previousChar0 == '$' && currentChar == '{' && currentMode == Mode.Text) {
                if (!outputPrevented) {
                    extractTextPart(i - 1, Mode.Code);
                    lastIndex = i + 1;
                }
                push(Mode.Code);
            } else if (previousChar6 == '$' && previousChar5 == 'u' && previousChar4 == 'n' && previousChar3 == 's' && previousChar2 == 'a' && previousChar1 == 'f' && previousChar0 == 'e' && currentChar == '{' && currentMode == Mode.Text) {
                extractTextPart(i - 7, Mode.UnsafeCode);
                lastIndex = i + 1;
                push(Mode.UnsafeCode);
            } else if (previousChar0 == '!' && currentChar == '{' && currentMode == Mode.Text) {
                extractTextPart(i - 1, Mode.CodeStatement);
                lastIndex = i + 1;
                push(Mode.CodeStatement);
            } else if (currentChar == '}' && currentMode == Mode.CodeStatement) {
                pop();
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i, visitor::onCodeStatement);
                    lastIndex = i + 1;
                }
            } else if (currentChar == '"' && currentMode.isTrackStrings()) {
                push(Mode.JavaCodeString);
            } else if (currentChar == '"' && currentMode == Mode.JavaCodeString && previousChar0 != '\\') {
                pop();
            } else if (currentChar == '}' && currentMode == Mode.Code) {
                pop();
                if (currentMode == Mode.Text && !outputPrevented) {
                    extractCodePart();
                }
            } else if (currentChar == '}' && currentMode == Mode.UnsafeCode) {
                pop();
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i, visitor::onUnsafeCodePart);
                    lastIndex = i + 1;
                }
            } else if (previousChar1 == '@' && previousChar0 == 'i' && currentChar == 'f' && currentMode == Mode.Text) {
                extractTextPart(i - 2, Mode.Condition);
                lastIndex = i + 1;
                push(Mode.Condition);
            } else if (currentChar == '(' && (currentMode == Mode.Condition || currentMode == Mode.ConditionElse)) {
                lastIndex = i + 1;
                push(Mode.JavaCode);
            } else if (currentMode.isTrackBraces() && (currentChar == '(' || currentChar == '{')) {
                push(Mode.JavaCode);
            } else if (currentMode.isTrackBraces() && (currentChar == ')' || currentChar == '}')) {
                if (currentMode == Mode.JavaCodeParam) {
                    TagMode previousMode = getPreviousMode(TagMode.class);
                    extract(templateCode, lastIndex, i, (d, c) -> {
                        if (!StringUtils.isBlank(c)) {
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
                    if (contentType == ContentType.Html && currentHtmlTag != null && currentHtmlTag.innerTagsIgnored) {
                        visitor.onError("@" + tagMode.type.toString().toLowerCase() + " calls in <" + currentHtmlTag.name + "> blocks are not allowed.");
                    }

                    extract(templateCode, lastIndex, i, (d, c) -> visitor.onTag(d, tagMode.type, tagMode.name.toString(), tagMode.params));
                    lastIndex = i + 1;
                    pop();
                }
            } else if (previousChar0 == '@' && currentChar == '`' && isContentExpressionAllowed()) {
                push(Mode.Content);
            } else if (currentChar == '`' && currentMode == Mode.Content) {
                pop();
            } else if (currentChar == ',' && currentMode == Mode.JavaCodeParam) {
                TagMode previousMode = getPreviousMode(TagMode.class);
                extract(templateCode, lastIndex, i, (d, c) -> {
                    if (!StringUtils.isBlank(c)) {
                        previousMode.params.add(c);
                    }
                });
                lastIndex = i + 1;
            } else if (previousChar3 == '@' && previousChar2 == 'e' && previousChar1 == 'l' && previousChar0 == 's' && currentChar == 'e' && templateCode.charAt(i + 1) != 'i' && currentMode == Mode.Text) {
                extractTextPart(i - 4, Mode.ConditionElse);
                lastIndex = i + 1;

                pop();

                visitor.onConditionElse(depth);
                push(Mode.Text);
            } else if (previousChar5 == '@' && previousChar4 == 'e' && previousChar3 == 'l' && previousChar2 == 's' && previousChar1 == 'e' && previousChar0 == 'i' && currentChar == 'f' && currentMode == Mode.Text) {
                extractTextPart(i - 6, Mode.ConditionElse);
                lastIndex = i + 1;

                pop();

                if (currentMode == Mode.Condition || currentMode == Mode.ConditionElse) {
                    pop();
                }

                push(Mode.ConditionElse);

            } else if (previousChar4 == '@' && previousChar3 == 'e' && previousChar2 == 'n' && previousChar1 == 'd' && previousChar0 == 'i' && currentChar == 'f' && currentMode == Mode.Text) {
                extractTextPart(i - 5, Mode.ConditionEnd);
                lastIndex = i + 1;

                pop();

                if (currentMode == Mode.Condition || currentMode == Mode.ConditionElse) {
                    visitor.onConditionEnd(depth);
                    pop();
                }
            } else if (previousChar2 == '@' && previousChar1 == 'f' && previousChar0 == 'o' && currentChar == 'r' && currentMode == Mode.Text) {
                extractTextPart(i - 3, Mode.ForLoop);
                lastIndex = i + 1;
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
            } else if (previousChar5 == '@' && previousChar4 == 'e' && previousChar3 == 'n' && previousChar2 == 'd' && previousChar1 == 'f' && previousChar0 == 'o' && currentChar == 'r' && currentMode == Mode.Text) {
                extractTextPart(i - 6, Mode.ForLoopEnd);
                lastIndex = i + 1;

                pop();

                if (currentMode == Mode.ForLoop) {
                    visitor.onForLoopEnd(depth);
                    pop();
                }
            } else if (previousChar3 == '@' && previousChar2 == 't' && previousChar1 == 'a' && previousChar0 == 'g' && currentChar == '.' && currentMode == Mode.Text) {
                TagMode mode = new TagMode(TemplateType.Tag);
                extractTextPart(i - 4, mode);
                lastIndex = i + 1;

                push(mode);
                push(Mode.TagName);
            } else if (previousChar6 == '@' && previousChar5 == 'l' && previousChar4 == 'a' && previousChar3 == 'y' && previousChar2 == 'o' && previousChar1 == 'u' && previousChar0 == 't' && currentChar == '.' && currentMode == Mode.Text) {
                TagMode mode = new TagMode(TemplateType.Layout);
                extractTextPart(i - 7, mode);
                lastIndex = i + 1;

                push(mode);
                push(Mode.TagName);
            } else if (currentMode == Mode.TagName) {
                if (currentChar == '(') {
                    pop();
                    push(Mode.JavaCodeParam);
                    lastIndex = i + 1;
                } else if (currentChar != ' ') {
                    getPreviousMode(TagMode.class).name.append(currentChar);
                }
            } else if (currentMode == Mode.Text && contentType == ContentType.Html) {
                interceptHtmlTags();
            }

            if (currentChar == '\n' && currentMode != Mode.Content) {
                visitor.onLineFinished();
                lastLineIndex = i + 1;
            }
        }

        if (lastIndex < endIndex) {
            extractTextPart(endIndex, null);
        }

        if (type != TemplateType.Content) {
            completeParamsIfRequired();
            visitor.onComplete();
        }
    }

    private boolean isCommentAllowed() {
        return currentMode == Mode.Text;
    }

    private boolean isParamOrImportAllowed() {
        if (paramsComplete) {
            return false;
        }

        int endIndex = templateCode.lastIndexOf('@', this.i);
        for (int j = lastIndex; j < endIndex; j++) {
            char currentChar = templateCode.charAt(j);
            if (!Character.isWhitespace(currentChar)) {
                return false;
            }
        }
        return true;
    }

    private boolean areParamsComplete(int startIndex) {
        if (visitor instanceof TemplateParametersCompleteVisitor) {
            return false;
        }

        try {
            TemplateParser templateParser = new TemplateParser(templateCode, type, new TemplateParametersCompleteVisitor(), config);
            templateParser.setStartIndex(startIndex);
            templateParser.parse();
        } catch (TemplateParametersCompleteVisitor.Result result) {
            if (result.complete) {
                return true;
            }
        }

        return false;
    }

    private boolean isContentExpressionAllowed() {
        return currentMode == Mode.Content || currentMode == Mode.JavaCodeParam || currentMode == Mode.Code || currentMode == Mode.CodeStatement || currentMode == Mode.Param;
    }

    private void extractTextPart(int endIndex, Mode mode) {
        if (trimControlStructures) {
            extractTextPartAndTrimControlStructures(endIndex, mode);
        } else {
            extract(templateCode, lastIndex, endIndex, visitor::onTextPart);
        }
    }

    private void extractTextPartAndTrimControlStructures(int endIndex, Mode mode) {
        completeParamsIfRequired();

        int startIndex = lastIndex;
        if (lastTrimmedIndex != -1) {
            if (lastTrimmedIndex > lastIndex) {
                startIndex = lastTrimmedIndex;
            }
            lastTrimmedIndex = -1;
        }

        if (startIndex < 0) {
            return;
        }
        if (endIndex < startIndex) {
            return;
        }

        if (LineInfo.isSingleControlStructure(templateCode, endIndex, this.endIndex, lastLineIndex, mode)) {
            lastTrimmedIndex = templateCode.indexOf('\n', endIndex) + 1;
            extractTrimmed(startIndex, lastLineIndex);
            previousControlStructureTrimmed = mode;
        } else {
            extractTrimmed(startIndex, endIndex);
            previousControlStructureTrimmed = null;
        }

        if (mode == Mode.Condition || mode == Mode.ForLoop) {
            pushIndent(endIndex, mode);
        } else if (mode == Mode.ConditionEnd || mode == Mode.ForLoopEnd) {
            popIndent();
        }
    }

    private void extractTrimmed(int startIndex, int endIndex) {
        int indentationsToSkip = getIndentationsToSkip();
        visitor.onTextPart(depth, trimIndentations(startIndex, endIndex, indentationsToSkip));
    }

    private String trimIndentations(int startIndex, int endIndex, int indentationsToSkip) {
        StringBuilder resultText = new StringBuilder(endIndex - startIndex);

        int indentation = 0;
        int line = 0;
        boolean writeLine = false;
        boolean firstNonWhitespaceReached = false;
        if (previousControlStructureTrimmed == null) {
            firstNonWhitespaceReached = true;
            writeLine = true;
        }

        for (int j = startIndex; j < endIndex; ++j) {
            char currentChar = templateCode.charAt(j);

            if ((line > 0 || firstNonWhitespaceReached) && (currentChar == '\r' || currentChar == '\n')) {
                resultText.append(currentChar);
            }

            if (currentChar == '\r') {
                continue;
            }

            if (currentChar == '\n') {
                ++line;
                indentation = 0;
                writeLine = false;
                continue;
            }

            if (!firstNonWhitespaceReached && !Character.isWhitespace(currentChar)) {
                firstNonWhitespaceReached = true;
            }

            if (!writeLine && isIndentationCharacter(currentChar) && indentation < indentationsToSkip) {
                ++indentation;
            } else {
                writeLine = true;
            }

            if (writeLine) {
                resultText.append(currentChar);
            }
        }

        return resultText.toString();
    }

    private void pushIndent(int endIndex, Mode mode) {
        int currentLineIndentation = 0;
        for (int j = endIndex - 1; j >= lastIndex; --j) {
            char currentChar = templateCode.charAt(j);
            if (isIndentationCharacter(currentChar)) {
                ++currentLineIndentation;
            } else {
                break;
            }
        }

        int nextLineIndentation = 0;
        int nextIndex = templateCode.indexOf('\n', endIndex);
        if (nextIndex > 0) {
            for (int j = nextIndex + 1; j < this.endIndex; ++j) {
                char currentChar = templateCode.charAt(j);
                if (isIndentationCharacter(currentChar)) {
                    ++nextLineIndentation;
                } else {
                    break;
                }
            }
        }

        int amount = Math.max(nextLineIndentation - currentLineIndentation, 0);

        indentStack.push(new Indent(mode, amount));
    }

    private void popIndent() {
        if (!indentStack.isEmpty()) {
            indentStack.pop();
        }
    }

    private boolean isIndentationCharacter(char c) {
        return c == ' ' || c == '\t';
    }

    private int getIndentationsToSkip() {
        int amount = 0;
        for (Indent indent : indentStack) {
            amount += indent.amount;
        }
        return amount;
    }

    private void extractCodePart() {
        if (contentType == ContentType.Html) {
            extractHtmlCodePart();
        } else {
            extractPlainCodePart();
        }
        lastIndex = i + 1;
    }

    private void extractPlainCodePart() {
        extract(templateCode, lastIndex, i, visitor::onCodePart);
    }

    private void extractHtmlCodePart() {
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

    private void extractComment(Mode mode, int startIndex) {
        if (paramsComplete || areParamsComplete(startIndex)) {
            extractTextPart(startIndex, mode);
        }
        push(mode);
    }

    private boolean isHtmlCommentAllowed() {
        if (contentType != ContentType.Html) {
            return false;
        }

        if (htmlCommentsPreserved) {
            return false;
        }

        if (currentHtmlTag == null) {
            return true;
        }

        return !currentHtmlTag.isScript && !currentHtmlTag.isStyle && !currentHtmlTag.isInAttribute();
    }

    private boolean isCssCommentAllowed() {
        if (contentType != ContentType.Html) {
            return false;
        }

        if (htmlCommentsPreserved) {
            return false;
        }

        if (currentHtmlTag == null) {
            return false;
        }

        return currentHtmlTag.isStyle && !currentHtmlTag.isInStringLiteral() && !currentHtmlTag.isInAttribute();
    }

    private boolean isJsCommentAllowed() {
        if (contentType != ContentType.Html) {
            return false;
        }

        if (htmlCommentsPreserved) {
            return false;
        }

        if (currentHtmlTag == null) {
            return false;
        }

        return currentHtmlTag.isScript && !currentHtmlTag.isInStringLiteral() && !currentHtmlTag.isInAttribute();
    }

    private void interceptHtmlTags() {
        if ( isOpeningHtmlTag() ) {
            String name = parseHtmlTagName(i + 1);
            if (!name.isEmpty()) {
                HtmlTag htmlTag = new HtmlTag(name, isHtmlTagIntercepted(name), i + name.length());
                htmlPolicy.validateHtmlTag(htmlTag);
                pushHtmlTag(htmlTag);
                tagClosed = false;
            }
        } else if (currentHtmlTag != null && i > currentHtmlTag.attributeStartIndex) {
            if (!currentHtmlTag.attributesProcessed && currentHtmlTag.isCurrentAttributeQuote(currentChar)) {
                HtmlAttribute currentAttribute = currentHtmlTag.getCurrentAttribute();
                currentAttribute.quoteCount++;
                if (currentAttribute.quoteCount == 1) {
                    currentAttribute.valueStartIndex = i + 1;

                    if (isHtmlAttributeIntercepted(currentAttribute.name)) {
                        extractTextPart(i + 1, null);
                        lastIndex = i + 1;

                        visitor.onInterceptHtmlAttributeStarted(depth, currentHtmlTag, currentAttribute);
                    }
                } else if (currentAttribute.quoteCount == 2) {
                    currentAttribute.value = templateCode.substring(currentAttribute.valueStartIndex, i);

                    if (currentAttribute.containsSingleOutput) {
                        extractTextPart(getLastWhitespaceIndex(currentAttribute.startIndex - 1), Mode.Code);
                        lastIndex = i + 1;

                        visitor.onHtmlAttributeOutput(depth, currentHtmlTag, currentAttribute);

                        outputPrevented = false;
                    }
                }
            } else if (!currentHtmlTag.attributesProcessed && previousChar0 == '/' && currentChar == '>') {
                if (currentHtmlTag.intercepted) {
                    extractTextPart(i - 1, null);
                    lastIndex = i - 1;
                    visitor.onInterceptHtmlTagOpened(depth, currentHtmlTag);
                }
                currentHtmlTag.attributesProcessed = true;

                popHtmlTag();
            } else if (!currentHtmlTag.attributesProcessed && currentChar == '>') {
                if (tagClosed) {
                    tagClosed = false;
                } else {
                    if (currentHtmlTag.intercepted) {
                        extractTextPart(i, null);
                        lastIndex = i;
                        visitor.onInterceptHtmlTagOpened(depth, currentHtmlTag);
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
                            extractTextPart(i - 1, null);
                            lastIndex = i - 1;
                            visitor.onInterceptHtmlTagClosed(depth, currentHtmlTag);
                        }

                        popHtmlTag();
                    }
                } else if (!currentHtmlTag.innerTagsIgnored) {
                    String tagName = parseHtmlTagName(i + 1);
                    visitor.onError("Unclosed tag <" + currentHtmlTag.name + ">, expected " + "</" + currentHtmlTag.name + ">, got </" + tagName + ">.");
                }
                tagClosed = true;
            } else if (!currentHtmlTag.attributesProcessed && !Character.isWhitespace(currentChar) && currentChar != '/' && currentHtmlTag.isCurrentAttributeComplete()) {
                HtmlAttribute attribute = parseHtmlAttribute();
                if (attribute != null) {
                    htmlPolicy.validateHtmlAttribute(currentHtmlTag, attribute);

                    currentHtmlTag.attributes.add(attribute);

                    if (attribute.containsSingleOutput) {
                        outputPrevented = true;
                    }

                    if (attribute.quotes == 0) {
                        i += attribute.name.length() - 1;
                        outputPrevented = false;
                    }
                } else {
                    outputPrevented = false;
                }
            } else if (currentHtmlTag.isStyle || currentHtmlTag.isScript) {
                handleStringLiterals('\'');
                handleStringLiterals('"');
            }
        }
    }

    private void handleStringLiterals(char quote) {
        if (currentChar == quote && (currentHtmlTag.stringLiteralQuote == 0 || currentHtmlTag.stringLiteralQuote == quote)) {
            if (currentHtmlTag.stringLiteralQuote == 0) {
                currentHtmlTag.stringLiteralQuote = currentChar;
            } else if (previousChar0 != '\\') {
                currentHtmlTag.stringLiteralQuote = 0;
            }
        }
    }

    private boolean isOpeningHtmlTag() {
        if (currentChar != '<') {
            return false;
        }

        if (templateCode.startsWith("<%--", i)) {
            return false;
        }

        if (templateCode.startsWith("<!", i)) {
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

    private void pushHtmlTag(HtmlTag htmlTag) {
        htmlStack.push(htmlTag);
        currentHtmlTag = htmlTag;
    }

    private void popHtmlTag() {
        htmlStack.pop();
        currentHtmlTag = htmlStack.peek();
    }

    private String parseHtmlTagName(int index) {
        if (templateCode.startsWith("!--", index)) {
            return "!--";
        }

        int startIndex = index;
        while (index < endIndex) {
            char c = templateCode.charAt(index);
            if (Character.isWhitespace(c) || c == '/' || c == '>') {
                break;
            }
            ++index;
        }
        return templateCode.substring(startIndex, index);
    }

    private HtmlAttribute parseHtmlAttribute() {
        int nameEndIndex = -1;
        char quotes = 0;
        for (int j = i; j < endIndex; ++j) {
            char c = templateCode.charAt(j);

            if (c == '=') {
                quotes = parseHtmlAttributeQuotes(j + 1);
            }

            if (nameEndIndex == -1) {
                if (c == '=' || c == '/' || c == '>' || Character.isWhitespace(c)) {
                    nameEndIndex = j;
                }
            } else if (!Character.isWhitespace(c)) {
                break;
            }
        }

        if (nameEndIndex == -1) {
            return null;
        }

        return new HtmlAttribute(templateCode.substring(i, nameEndIndex), quotes, i, isHtmlAttributeSingleOutput(nameEndIndex, quotes));
    }

    private char parseHtmlAttributeQuotes(int index) {
        while (Character.isWhitespace(templateCode.charAt(index))) {
            ++index;
        }

        return templateCode.charAt(index);
    }

    private boolean isHtmlAttributeSingleOutput(int nameEndIndex, char quotes) {
        int openingQuoteIndex = -1;
        int openingOutputIndex = -1;
        int closingOutputIndex = -1;
        for (int j = nameEndIndex + 1; j < endIndex; ++j) {
            char c = templateCode.charAt(j);

            if (openingQuoteIndex == -1) {
                if (c == quotes) {
                    openingQuoteIndex = j;
                }
            } else if (openingOutputIndex == -1) {
                if (templateCode.startsWith("${", j)) {
                    openingOutputIndex = j;
                } else {
                    return false;
                }
            } else if (closingOutputIndex == -1) {
                if (c == '}') {
                    closingOutputIndex = j;
                }
            } else {
                return c == quotes;
            }
        }

        return false;
    }

    private int getLastWhitespaceIndex(int index) {
        for (; index >= 0; --index) {
            if (!Character.isWhitespace(templateCode.charAt(index))) {
                ++index;
                break;
            }
        }
        return index;
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
        if (!paramsComplete && currentMode != Mode.Param && currentMode != Mode.Import && type != TemplateType.Content) {
            visitor.onParamsComplete();
            paramsComplete = true;
        }
    }

    interface VisitorCallback {
        void accept(int depth, String content);
    }

    interface Mode {
        Mode Import = new StatelessMode("Import");
        Mode Param = new StatelessMode("Param");
        Mode Text = new StatelessMode("Text");
        Mode Code = new StatelessMode("Code", true, true, false);
        Mode UnsafeCode = new StatelessMode("UnsafeCode", true, true, false);
        Mode CodeStatement = new StatelessMode("CodeStatement", true, true, false);
        Mode Condition = new StatelessMode("Condition");
        Mode JavaCode = new StatelessMode("JavaCode", true, true, false);
        Mode JavaCodeParam = new StatelessMode("JavaCodeParam", true, true, false);
        Mode JavaCodeString = new StatelessMode("JavaCodeString");
        Mode ConditionElse = new StatelessMode("ConditionElse");
        Mode ConditionEnd = new StatelessMode("ConditionEnd");
        Mode ForLoop = new StatelessMode("ForLoop");
        Mode ForLoopCode = new StatelessMode("ForLoopCode", true, false, false);
        Mode ForLoopEnd = new StatelessMode("ForLoopEnd");
        Mode TagName = new StatelessMode("TagName");
        Mode Comment = new StatelessMode("Comment");
        Mode HtmlComment = new StatelessMode("HtmlComment", false, false, true);
        Mode CssComment = new StatelessMode("CssComment", false, false, true);
        Mode JsComment = new StatelessMode("JsComment", false, false, true);
        Mode JsBlockComment = new StatelessMode("JsBlockComment", false, false, true);
        Mode Content = new StatelessMode("Content", false, false, true);

        boolean isTrackStrings();
        boolean isTrackBraces();
        boolean isComment();
    }

    private static class StatelessMode implements Mode {
        private final String debugName;
        private final boolean trackStrings;
        private final boolean trackBraces;
        private final boolean comment;

        private StatelessMode(String debugName) {
            this(debugName, false, false, false);
        }

        private StatelessMode(String debugName, boolean trackStrings, boolean trackBraces, boolean comment) {
            this.debugName = debugName;
            this.trackStrings = trackStrings;
            this.trackBraces = trackBraces;
            this.comment = comment;
        }

        @Override
        public boolean isTrackStrings() {
            return trackStrings;
        }

        @Override
        public boolean isTrackBraces() {
            return trackBraces;
        }

        @Override
        public boolean isComment() {
            return comment;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + debugName + "]";
        }
    }

    private static class TagMode implements Mode {
        final TemplateType type;
        final StringBuilder name = new StringBuilder();
        final List<String> params = new ArrayList<>();

        private TagMode(TemplateType type) {
            this.type = type;
        }

        @Override
        public boolean isTrackStrings() {
            return false;
        }

        @Override
        public boolean isTrackBraces() {
            return false;
        }

        @Override
        public boolean isComment() {
            return false;
        }
    }

    private static class Indent {
        public final Mode mode;
        public final int amount;

        public Indent(Mode mode, int amount) {
            this.mode = mode;
            this.amount = amount;
        }
    }

    public static class HtmlTag implements gg.jte.html.HtmlTag {

        // See https://www.lifewire.com/html-singleton-tags-3468620
        private static final Set<String> VOID_HTML_TAGS = new HashSet<>(Arrays.asList("area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"));

        public final String name;
        public final boolean intercepted;
        public final int attributeStartIndex;
        public final boolean bodyIgnored;
        public final boolean innerTagsIgnored;
        public final boolean isScript;
        public final boolean isStyle;
        public final List<HtmlAttribute> attributes = new ArrayList<>();
        public boolean attributesProcessed;
        public char stringLiteralQuote;

        public HtmlTag(String name, boolean intercepted, int attributeStartIndex) {
            this.name = name;
            this.intercepted = intercepted;
            this.attributeStartIndex = attributeStartIndex;
            this.bodyIgnored = VOID_HTML_TAGS.contains(name);
            this.isScript = "script".equals(name);
            this.isStyle = "style".equals(name);
            this.innerTagsIgnored = isScript || isStyle;
        }

        public HtmlAttribute getCurrentAttribute() {
            if (attributes.isEmpty()) {
                return null;
            }
            return attributes.get(attributes.size() - 1);
        }

        public boolean isCurrentAttributeComplete() {
            HtmlAttribute currentAttribute = getCurrentAttribute();
            if (currentAttribute == null) {
                return true;
            }

            if (currentAttribute.quotes == 0) {
                return true;
            }

            return currentAttribute.quoteCount > 1;
        }

        public boolean isCurrentAttributeQuote(char currentChar) {
            HtmlAttribute currentAttribute = getCurrentAttribute();
            if (currentAttribute == null) {
                return false;
            }

            return currentChar == currentAttribute.quotes;
        }

        public boolean isInAttribute() {
            if (attributesProcessed) {
                return false;
            }

            HtmlAttribute currentAttribute = getCurrentAttribute();
            return currentAttribute != null && currentAttribute.quoteCount < 2;
        }

        public boolean isInStringLiteral() {
            return stringLiteralQuote != 0;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static class HtmlAttribute implements gg.jte.html.HtmlAttribute {
        // See https://meiert.com/en/blog/boolean-attributes-of-html/
        @SuppressWarnings("SpellCheckingInspection")
        private static final Set<String> BOOLEAN_HTML_ATTRIBUTES = new HashSet<>(Arrays.asList("allowfullscreen", "allowpaymentrequest", "async", "autofocus", "autoplay", "checked", "controls", "default", "disabled", "formnovalidate", "hidden", "ismap", "itemscope", "loop", "multiple", "muted", "nomodule", "novalidate", "open", "playsinline", "readonly", "required", "reversed", "selected", "truespeed"));

        public final String name;
        public final char quotes;
        public final int startIndex;
        public final boolean containsSingleOutput;
        public final boolean bool;
        public String value;

        public int quoteCount;
        public int valueStartIndex;

        private HtmlAttribute(String name, char quotes, int startIndex, boolean containsSingleOutput) {
            this.name = name;
            this.quotes = quotes;
            this.startIndex = startIndex;
            this.containsSingleOutput = containsSingleOutput;
            this.bool = BOOLEAN_HTML_ATTRIBUTES.contains(name);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isEmpty() {
            return quotes == 0;
        }

        @Override
        public char getQuotes() {
            return quotes;
        }

        @Override
        public boolean isBoolean() {
            return bool;
        }
    }
}
