package gg.jte.html.escape;

import gg.jte.TemplateOutput;

public class Escape {

    @SuppressWarnings("DuplicatedCode")
    public static void htmlContent(String value, TemplateOutput output) {
        int lastIndex = 0;
        int length = value.length();

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> lastIndex = flushAndEscape(value, lastIndex, i, "&amp;", output);
                case '<' -> lastIndex = flushAndEscape(value, lastIndex, i, "&lt;", output);
                case '>' -> lastIndex = flushAndEscape(value, lastIndex, i, "&gt;", output);
            }
        }

        flushRemaining(value, output, lastIndex, length);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void htmlAttribute(String value, TemplateOutput output) {
        int lastIndex = 0;
        int length = value.length();

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\'' -> lastIndex = flushAndEscape(value, lastIndex, i, "&#39;", output);
                case '"' -> lastIndex = flushAndEscape(value, lastIndex, i, "&#34;", output);
                case '&' -> lastIndex = flushAndEscape(value, lastIndex, i, "&amp;", output);
                case '<' -> lastIndex = flushAndEscape(value, lastIndex, i, "&lt;", output);
            }
        }

        flushRemaining(value, output, lastIndex, length);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void javaScriptBlock(String value, TemplateOutput output) {
        int lastIndex = 0;
        int length = value.length();

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\'' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\'", output);
                case '"' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\\"", output);
                case '`' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\`", output);
                case '$' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\$", output);
                case '/' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\/", output);
                case '-' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\-", output);
                case '\\' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\\\", output);
                case '\n' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\n", output);
                case '\t' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\t", output);
                case '\r' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\r", output);
                case '\f' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\f", output);
                case '\b' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\b", output);
            }
        }

        flushRemaining(value, output, lastIndex, length);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void javaScriptAttribute(String value, TemplateOutput output) {
        int lastIndex = 0;
        int length = value.length();

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\'' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\x27", output);
                case '"' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\x22", output);
                case '`' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\x60", output);
                case '$' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\x24", output);
                case '\\' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\\\", output);
                case '\n' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\n", output);
                case '\t' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\t", output);
                case '\r' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\r", output);
                case '\f' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\f", output);
                case '\b' -> lastIndex = flushAndEscape(value, lastIndex, i, "\\b", output);
            }
        }

        flushRemaining(value, output, lastIndex, length);
    }

    private static int flushAndEscape(String value, int lastIndex, int currentIndex, String escapeSequence, TemplateOutput output) {
        output.writeContent(value, lastIndex, currentIndex);
        output.writeContent(escapeSequence);
        return currentIndex + 1;
    }

    private static void flushRemaining(String value, TemplateOutput output, int lastIndex, int length) {
        if (lastIndex == 0) {
            output.writeContent(value);
        } else if (lastIndex < length) {
            output.writeContent(value, lastIndex, length);
        }
    }
}
