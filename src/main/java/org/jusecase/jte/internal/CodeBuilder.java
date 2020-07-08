package org.jusecase.jte.internal;

import java.util.Arrays;

@SuppressWarnings("UnusedReturnValue")
final class CodeBuilder {
    private static final int INITIAL_CAPACITY = 10;
    private static final int LOAD_FACTOR = 2;

    private final StringBuilder javaCode = new StringBuilder(1024);
    private int currentJavaLine;
    private int currentTemplateLine;
    private int fieldsIndex;
    private int fieldsJavaLine;
    private int fieldsTemplateLine;
    private int[] lineInfo = new int[INITIAL_CAPACITY];

    public CodeBuilder append(String code) {
        javaCode.append(code);
        if (code.endsWith("\n")) {
            addLine(currentTemplateLine);
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public CodeBuilder append(String code, int start, int end) {
        javaCode.append(code, start, end);
        return this;
    }

    public CodeBuilder append(char code) {
        javaCode.append(code);
        return this;
    }

    public CodeBuilder finishTemplateLine() {
        ++currentTemplateLine;
        return this;
    }

    public CodeBuilder insertFieldLines(int count) {
        fillLines(fieldsJavaLine, fieldsTemplateLine, count);
        return this;
    }

    public int getCurrentTemplateLine() {
        return currentTemplateLine;
    }

    public String getCode() {
        return javaCode.toString();
    }

    public void addLineInfoField(StringBuilder fields) {
        fields.append("\tpublic static final int[] ").append(TemplateCompiler.LINE_INFO_FIELD).append(" = {");
        for (int i = 0; i < currentJavaLine; ++i) {
            if (i > 0) {
                fields.append(',');
            }
            fields.append(lineInfo[i]);
        }
        fields.append("};\n");
    }

    public void markFieldsIndex() {
        fieldsIndex = javaCode.length();
        fieldsJavaLine = currentJavaLine;
        fieldsTemplateLine = currentTemplateLine;
    }

    public void insertFields(StringBuilder fields) {
        javaCode.insert(fieldsIndex, fields);
    }

    private void addLine(int templateLine) {
        if (currentJavaLine + 1 > lineInfo.length) {
            lineInfo = Arrays.copyOf(lineInfo, lineInfo.length * LOAD_FACTOR);
        }
        lineInfo[currentJavaLine] = templateLine;
        currentJavaLine++;
    }

    private void fillLines(int fromJavaLine, int templateLine, int count) {
        if (currentJavaLine + count > lineInfo.length) {
            lineInfo = Arrays.copyOf(lineInfo, currentJavaLine + count);
        }

        System.arraycopy(lineInfo, fromJavaLine, lineInfo, fromJavaLine + count, currentJavaLine - fromJavaLine);

        Arrays.fill(lineInfo, fromJavaLine, fromJavaLine + count, templateLine);

        currentJavaLine += count;
    }

    public int[] getLineInfo() {
        return Arrays.copyOf(lineInfo, currentJavaLine);
    }

    public StringBuilder getStringBuilder() {
        return javaCode;
    }
}
