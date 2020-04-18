package org.jusecase.jte.internal;

import java.util.ArrayDeque;
import java.util.Deque;

final class TemplateParser {
    private static final int LAYOUT_SECTION_DEPTH = 4;

    private final TemplateType type;

    private Mode currentMode;
    private Deque<Mode> stack = new ArrayDeque<>();
    private int depth;

    TemplateParser(TemplateType type) {
        this.type = type;
    }

    public void parse(int startIndex, String templateCode, TemplateParserVisitor visitor) {
        int lastIndex = startIndex;

        char previousChar9;
        char previousChar8 = 0;
        char previousChar7 = 0;
        char previousChar6 = 0;
        char previousChar5 = 0;
        char previousChar4 = 0;
        char previousChar3 = 0;
        char previousChar2 = 0;
        char previousChar1 = 0;
        char previousChar0 = 0;
        char currentChar = 0;
        currentMode = Mode.Text;
        stack.push(currentMode);
        depth = 0;
        StringBuilder currentTagName = new StringBuilder();

        for (int i = 0; i < templateCode.length(); ++i) {
            previousChar9 = previousChar8;
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

            if (previousChar0 == '$' && currentChar == '{') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 1, visitor::onTextPart);
                }
                lastIndex = i + 1;
                push(Mode.Code);
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
            } else if (currentChar == '}' && currentMode == Mode.Code) {
                pop();
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i, visitor::onCodePart);
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
                push(Mode.ConditionCode);
            } else if (currentChar == '(' && currentMode == Mode.ConditionCode) {
                push(Mode.ConditionCode);
            } else if (currentChar == ')' && currentMode == Mode.ConditionCode) {
                pop();
                if (currentMode == Mode.Condition) {
                    extract(templateCode, lastIndex, i, visitor::onConditionStart);
                    lastIndex = i + 1;
                    push(Mode.Text);
                } else if (currentMode == Mode.ConditionElse) {
                    extract(templateCode, lastIndex, i, visitor::onConditionElse);
                    lastIndex = i + 1;
                    push(Mode.Text);
                }
            } else if (previousChar3 == '@' && previousChar2 == 'e' && previousChar1 == 'l' && previousChar0 == 's' && currentChar == 'e' && templateCode.charAt(i + 1) != 'i') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 4, visitor::onTextPart);
                }
                lastIndex = i + 1;

                pop();

                visitor.onConditionElse(depth);
                push(Mode.Text);
            } else if(previousChar5 == '@' && previousChar4 == 'e' && previousChar3 == 'l' && previousChar2 == 's' && previousChar1 == 'e' && previousChar0 == 'i' && currentChar == 'f') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 6, visitor::onTextPart);
                }
                lastIndex = i + 1;

                pop();

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

                push(Mode.Tag);
                push(Mode.TagName);
                currentTagName.setLength(0);
            } else if (currentMode == Mode.TagName) {
                if (currentChar == '(') {
                    pop();
                    push(Mode.TagCode);
                    lastIndex = i + 1;
                } else if (currentChar != ' ') {
                    currentTagName.append(currentChar);
                }
            } else if (currentChar == ')' && currentMode == Mode.TagCode) {
                pop();
                if (currentMode == Mode.Tag) {
                    extract(templateCode, lastIndex, i, (d, c) -> visitor.onTag(d, currentTagName.toString(), c));
                    lastIndex = i + 1;
                    pop();
                } else if (currentMode == Mode.Layout) {
                    extract(templateCode, lastIndex, i, (d, c) -> visitor.onLayout(d, currentTagName.toString(), c));
                }
            } else if (previousChar6 == '@' && previousChar5 == 'l' && previousChar4 == 'a' && previousChar3 == 'y' && previousChar2 == 'o' && previousChar1 == 'u' && previousChar0 == 't' && currentChar == '.') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 7, visitor::onTextPart);
                    lastIndex = i + 1;
                }

                push(Mode.Layout);
                push(Mode.TagName);
                currentTagName.setLength(0);
            } else if (previousChar8 == '@' && previousChar7 == 'e' && previousChar6 == 'n' && previousChar5 == 'd' && previousChar4 == 'l' && previousChar3 == 'a' && previousChar2 == 'y' && previousChar1 == 'o' && previousChar0 == 'u' && currentChar == 't') {
                pop();
                lastIndex = i + 1;

                visitor.onLayoutEnd(depth);
            } else if (previousChar6 == '@' && previousChar5 == 's' && previousChar4 == 'e' && previousChar3 == 'c' && previousChar2 == 't' && previousChar1 == 'i' && previousChar0 == 'o' && currentChar == 'n') {
                if (type == TemplateType.Layout && currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 7, visitor::onTextPart);
                    lastIndex = i + 1;
                }
                push(Mode.LayoutSection);
            } else if (currentChar == '(' && currentMode == Mode.LayoutSection) {
                lastIndex = i + 1;
            } else if (currentChar == ')' && currentMode == Mode.LayoutSection) {
                extract(templateCode, lastIndex, i, visitor::onLayoutSection);
                lastIndex = i + 1;
                if (type != TemplateType.Layout) {
                    push(Mode.Text);
                    depth += LAYOUT_SECTION_DEPTH;
                } else {
                    pop();
                }
            } else if (previousChar9 == '@' && previousChar8 == 'e' && previousChar7 == 'n' && previousChar6 == 'd' && previousChar5 == 's' && previousChar4 == 'e' && previousChar3 == 'c' && previousChar2 == 't' && previousChar1 == 'i' && previousChar0 == 'o' && currentChar == 'n') {
                if (currentMode == Mode.Text) {
                    extract(templateCode, lastIndex, i - 10, visitor::onTextPart);
                }

                pop();
                pop();
                depth -= LAYOUT_SECTION_DEPTH;
                lastIndex = i + 1;

                visitor.onLayoutSectionEnd(depth);
            }
        }

        if (lastIndex < templateCode.length()) {
            extract(templateCode, lastIndex, templateCode.length(), visitor::onTextPart);
        }
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

    private void extract(String templateCode, int startIndex, int endIndex, VisitorCallback callback) {
        if (startIndex < 0) {
            return;
        }
        if (endIndex < startIndex) {
            return;
        }
        callback.accept(depth, templateCode.substring(startIndex, endIndex));
    }

    interface VisitorCallback {
        void accept(int depth, String content);
    }

    enum Mode {
        Text,
        Code,
        CodeStatement,
        Condition,
        ConditionCode,
        ConditionElse,
        ForLoop,
        ForLoopCode,
        Tag,
        TagName,
        TagCode,
        Layout,
        LayoutSection,
    }
}
