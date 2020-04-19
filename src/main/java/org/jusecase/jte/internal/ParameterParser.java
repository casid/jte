package org.jusecase.jte.internal;

import java.util.function.Consumer;

final class ParameterParser {

    private final String code;
    private final ParameterParserVisitor visitor;
    private int lastIndex;

    public ParameterParser(String code, ParameterParserVisitor visitor) {
        this.code = code;
        this.visitor = visitor;
    }

    int parse() {
        parse("@import", visitor::onImport);
        parse("@param", visitor::onParameter);

        return lastIndex;
    }

    private void parse(String keyword, Consumer<String> callback) {
        while (true) {
            int attributeStart = code.indexOf(keyword, lastIndex);
            if (attributeStart == -1) {
                break;
            }

            int attributeEnd = code.indexOf("\n", attributeStart);
            if (attributeEnd == -1) {
                break;
            }

            String parameter = code.substring(attributeStart + keyword.length(), attributeEnd).trim();
            callback.accept(parameter);
            lastIndex = attributeEnd + 1;
        }
    }
}
