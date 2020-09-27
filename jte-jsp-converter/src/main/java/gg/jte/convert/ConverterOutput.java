package gg.jte.convert;

public class ConverterOutput {

    private final StringBuilder buffer = new StringBuilder();

    private int indentationCount = 4;
    private char indentationChar = ' ';

    private int skipIndent;
    private boolean trim = false;
    private boolean insideScript = false;

    public ConverterOutput append(String s) {
        if (s != null) {
            buffer.append(trimIndent(s));
        }
        return this;
    }

    public ConverterOutput prepend(String s) {
        if (s != null) {
            buffer.insert(0, s);
        }
        return this;
    }

    public boolean isTrimWhitespace() {
        return trim;
    }

    public void setTrimWhitespace(boolean value) {
        trim = value;
    }

    public boolean isInsideScript() {
        return insideScript;
    }

    public void setInsideScript(boolean value) {
        insideScript = value;
    }

    public ConverterOutput trim() {
        String trimmed = buffer.toString().trim();
        buffer.setLength(0);
        buffer.append(trimmed);
        return this;
    }

    public ConverterOutput newLine() {
        int indentationBegin = buffer.lastIndexOf("\n");
        int indentationEnd = -1;
        if (indentationBegin != -1) {
            indentationBegin++;
            for (int i = indentationBegin; i < buffer.length(); ++i) {
                if (!Character.isWhitespace(buffer.charAt(i))) {
                    indentationEnd = i;
                    break;
                }
            }
        }

        if (indentationBegin != -1 && indentationEnd != -1) {
            String indent = buffer.substring(indentationBegin, indentationEnd);
            buffer.append("\n");
            buffer.append(indent);
        } else {
            buffer.append("\n");
        }

        return this;
    }

    public ConverterOutput indent(int amount) {
        //noinspection StringRepeatCanBeUsed
        for(int i = 0; i < amount * indentationCount; ++i) {
            buffer.append(indentationChar);
        }
        return this;
    }

    public void incrementSkipIndent() {
        skipIndent++;
    }

    public void decrementSkipIndent() {
        skipIndent--;
    }

    private String trimIndent(String content) {
        if (skipIndent == 0) {
            return content;
        }

        StringBuilder result = new StringBuilder(content.length());

        int trim = 0;

        for (int i = 0; i < content.length(); ++i) {
            char c = content.charAt(i);
            if (c == '\n') {
                trim = skipIndent * indentationCount;
            } else if (trim > 0) {
                if (c == indentationChar) {
                    --trim;
                    continue;
                } else {
                    trim = 0;
                }
            }

            result.append(c);
        }

        return result.toString();
    }

    public void setIndentationChar(char indentationChar) {
        this.indentationChar = indentationChar;
    }

    public void setIndentationCount(int indentationCount) {
        this.indentationCount = indentationCount;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
