package gg.jte.compiler;

import gg.jte.runtime.StringUtils;

import java.util.Arrays;

@SuppressWarnings("UnusedReturnValue")
public final class CodeBuilder {
    private static final int INITIAL_CAPACITY = 10;
    private static final int LOAD_FACTOR = 2;

    private final StringBuilder code = new StringBuilder(1024);
    private int currentCodeLine;
    private int currentTemplateLine;
    private int fieldsIndex;
    private int fieldsCodeLine;
    private int fieldsTemplateLine;
    private int[] lineInfo = new int[INITIAL_CAPACITY];

    public CodeBuilder append(String code) {
        this.code.append(code);
        addLines(code, 0, code.length());
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CodeBuilder append(String code, int start, int end) {
        this.code.append(code, start, end);
        addLines(code, start, end);
        return this;
    }

    public CodeBuilder append(char code) {
        this.code.append(code);
        if (code == '\n') {
            addLine(currentTemplateLine);
        }
        return this;
    }

    public CodeBuilder append(int integer) {
        code.append(integer);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CodeBuilder appendEscaped(String text) {
        StringUtils.appendEscaped(code, text);
        return this;
    }

    public CodeBuilder finishTemplateLine() {
        ++currentTemplateLine;
        return this;
    }

    public CodeBuilder insertFieldLines(int count) {
        fillLines(fieldsCodeLine, fieldsTemplateLine, count);
        return this;
    }

    public int getCurrentTemplateLine() {
        return currentTemplateLine;
    }

    public String getCode() {
        return code.toString();
    }

    public void markFieldsIndex() {
        fieldsIndex = code.length();
        fieldsCodeLine = currentCodeLine;
        fieldsTemplateLine = currentTemplateLine;
    }

    public void insertFields(StringBuilder fields) {
        code.insert(fieldsIndex, fields);
    }

    private void addLine(int templateLine) {
        if (currentCodeLine + 1 > lineInfo.length) {
            lineInfo = Arrays.copyOf(lineInfo, lineInfo.length * LOAD_FACTOR);
        }
        lineInfo[currentCodeLine] = templateLine;
        currentCodeLine++;
    }

    private void fillLines(int fromJavaLine, int templateLine, int count) {
        if (currentCodeLine + count > lineInfo.length) {
            lineInfo = Arrays.copyOf(lineInfo, currentCodeLine + count);
        }

        System.arraycopy(lineInfo, fromJavaLine, lineInfo, fromJavaLine + count, currentCodeLine - fromJavaLine);

        Arrays.fill(lineInfo, fromJavaLine, fromJavaLine + count, templateLine);

        currentCodeLine += count;
    }

    private void addLines(String code, int start, int end) {
        for (int i = start; i < end; ++i) {
            if (code.charAt(i) == '\n') {
                addLine(currentTemplateLine);
            }
        }
    }

    public int[] getLineInfo() {
        return Arrays.copyOf(lineInfo, currentCodeLine);
    }

    public int getCurrentCodeLine() {
        return currentCodeLine;
    }

    public int getLineInfo(int index) {
        return lineInfo[index];
    }

    public void setCurrentTemplateLine(int templateLine) {
        currentTemplateLine = templateLine;
    }
}
