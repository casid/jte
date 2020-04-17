package org.jusecase.jte.internal;

import java.util.ArrayDeque;
import java.util.Deque;

final class TemplateParser {
    private Mode currentMode;
    private Deque<Mode> stack = new ArrayDeque<>();
    private int depth;

    public void parse(int startIndex, String templateCode, TemplateParserVisitor visitor) {
        int lastIndex = startIndex;

        char previousChar5;
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
                }

                lastIndex = i + 1;
                pop();
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
    }
}
