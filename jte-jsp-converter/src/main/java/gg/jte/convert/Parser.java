package gg.jte.convert;

import gg.jte.convert.xml.XmlAttributesParser;

import java.util.*;

public class Parser {
    private final Set<String> importStatements = new TreeSet<>();
    private final List<Converter> converters = new ArrayList<>();
    private final ArrayDeque<Converter> converterStack = new ArrayDeque<>();

    private String content;
    private int index;
    private int lastContentIndex;
    private StringBuilder result;
    private Converter currentConverter;
    private int importIndex;
    private int skipIndentations;
    private int indentationCount = 4;
    private char indentationChar = ' ';
    private String lineSeparator = "\n";
    private String prefix;

    public void register(Converter converter) {
        converters.add(converter);
    }

    public void setIndentationCount(int indentationCount) {
        this.indentationCount = indentationCount;
    }

    public void setIndentationChar(char indentationChar) {
        this.indentationChar = indentationChar;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public boolean startsWith(String token) {
        return content.startsWith(token, index);
    }

    public boolean endsWith(String token) {
        return content.startsWith(token, index - token.length());
    }

    public boolean hasNextToken(String token, int offset) {
        for (int i = index + offset; i < content.length(); ++i) {
            char c = content.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }

            return content.startsWith(token, i);
        }
        return false;
    }

    public XmlAttributesParser parseXmlAttributes(int offset) {
        XmlAttributesParser parser = new XmlAttributesParser(content, index + offset);
        index = parser.getEndIndex();
        markLastContentIndex();
        return parser;
    }

    public String convert(String content) {
        this.content = content.replace("\r\n", "\n");
        this.result = new StringBuilder(content.length());

        if (prefix != null) {
            result.append(prefix);
            importIndex = result.length();
        }

        for (index = 0; index < content.length(); ++index) {
            for (Converter converter : converters) {
                if (converter.canConvert(this)) {
                    pushConverter(converter);
                    break;
                }
            }

            if (currentConverter != null) {
                if (currentConverter.advance(this)) {
                    popConverter();
                }
            }
        }

        appendContentToResultIfRequired();
        insertImportStatementsIfRequired();

        String resultString = result.toString();
        if (!"\n".equals(lineSeparator)) {
            return resultString.replace("\n", lineSeparator);
        }
        return resultString;
    }

    private void insertImportStatementsIfRequired() {
        if (!importStatements.isEmpty()) {
            StringBuilder imports = new StringBuilder();
            for (String importStatement : importStatements) {
                imports.append(importStatement).append('\n');
            }
            result.insert(importIndex, imports);
        }
    }

    public void appendContentToResultIfRequired() {
        if (lastContentIndex < index && lastContentIndex < content.length()) {
            if (skipIndentations > 0) {
                int amountToSkip = 0;

                if (lastContentIndex > 0 && content.charAt(lastContentIndex - 1) == '\n') {
                    amountToSkip = skipIndentations * indentationCount;
                }

                for (int i = lastContentIndex; i < index; i++) {
                    char c = content.charAt(i);
                    if (amountToSkip > 0 && c == indentationChar) {
                        --amountToSkip;
                    } else {
                        result.append(c);
                        amountToSkip = 0;
                    }

                    if (c == '\n') {
                        amountToSkip = skipIndentations * indentationCount;
                    }
                }
            } else {
                result.append(content, lastContentIndex, Math.min(index, content.length()));
            }
            lastContentIndex = index;
        }
    }

    private void pushConverter(Converter converter) {

        currentConverter = converter.newInstance();
        converterStack.push(currentConverter);

        currentConverter.onPushed(this);

        appendContentToResultIfRequired();
    }

    private void popConverter() {
        Converter converter = converterStack.pop();
        converter.onPopped(this);
        currentConverter = converterStack.peek();
    }

    public void markLastContentIndex() {
        lastContentIndex = index;
    }

    public void markLastContentIndexAfterTag(boolean skipNewLine) {
        if (index < content.length() && ((skipNewLine && content.charAt(index) == '\n') || content.charAt(index) == '>')) {
            lastContentIndex = index + 1;
        } else {
            lastContentIndex = index;
        }
    }

    public int getIndex() {
        return index;
    }

    public void advanceIndex(int offset) {
        index += offset;
    }

    public String substring(int beginIndex, int endIndex) {
        return content.substring(beginIndex, endIndex);
    }

    public void addImportStatement(String importStatement) {
        importStatements.add(importStatement);
    }

    public Converter getCurrentConverter() {
        return currentConverter;
    }

    public Converter getParentConverter() {
        boolean first = true;
        for (Converter converter : converterStack) {
            if (first) {
                first = false;
            } else {
                return converter;
            }
        }
        return null;
    }

    public StringBuilder getResult() {
        return result;
    }

    public void advanceIndexAfter(char character) {
        for (; index < content.length(); ++index) {
            char c = content.charAt(index);
            if (!Character.isWhitespace(c)) {
                --index;
                return;
            }

            if (c == character) {
                return;
            }
        }
    }

    public void incrementSkipIndentations() {
        ++skipIndentations;
    }

    public void decrementSkipIndentations() {
        --skipIndentations;
    }

    public void removeLeadingSpaces() {
        int i;
        for (i = result.length() - 1; i >= 0; --i) {
            char c = result.charAt(i);
            if (c != indentationChar) {
                break;
            }
        }

        if (i < result.length() - 1) {
            result.delete(i + 1, result.length());
        }
    }
}
