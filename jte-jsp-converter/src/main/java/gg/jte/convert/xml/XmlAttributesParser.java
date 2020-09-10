package gg.jte.convert.xml;

import java.util.LinkedHashMap;
import java.util.Map;

public class XmlAttributesParser {
    private final String content;
    private final int startIndex;
    private final Map<String, String> attributes;

    private int i;

    private int nameStartIndex;
    private int nameEndIndex;
    private int valueStartIndex;
    private int valueEndIndex;
    private char quotes;

    public XmlAttributesParser(String content, int startIndex) {
        this.content = content;
        this.startIndex = startIndex;
        this.attributes = new LinkedHashMap<>();

        reset();
        parse();
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public int getEndIndex() {
        return i;
    }

    @SuppressWarnings("UnnecessaryContinue")
    private void parse() {
        for (i = startIndex; i < content.length(); i++) {
            char character = content.charAt(i);

            if (nameStartIndex == -1) {
                if (Character.isWhitespace(character)) {
                    continue;
                } else if (character == '>') {
                    return;
                } else {
                    nameStartIndex = i;
                }
            } else if (nameEndIndex == -1) {
                if (Character.isWhitespace(character)) {
                    nameEndIndex = i;
                } else if ('=' == character) {
                    nameEndIndex = i;
                }
            } else if (valueStartIndex == -1) {
                if (Character.isWhitespace(character)) {
                    continue;
                } else if (character == '\'' || character == '"') {
                    quotes = character;
                    valueStartIndex = i + 1;
                }
            } else if (valueEndIndex == -1) {
                if (character == quotes) {
                    valueEndIndex = i;

                    extractAttribute();
                    reset();
                }
            }
        }
    }

    private void extractAttribute() {
        String name = content.substring(nameStartIndex, nameEndIndex);
        String value = content.substring(valueStartIndex, valueEndIndex);
        attributes.put(name, value);
    }

    private void reset() {
        nameStartIndex = -1;
        nameEndIndex = -1;
        valueStartIndex = -1;
        valueEndIndex = -1;
        quotes = 0;
    }

}
