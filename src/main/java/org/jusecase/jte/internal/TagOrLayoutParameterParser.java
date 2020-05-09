package org.jusecase.jte.internal;

import java.util.ArrayList;
import java.util.List;

final class TagOrLayoutParameterParser {
    List<String> importClasses = new ArrayList<>();
    List<ParamInfo> parameters = new ArrayList<>();

    public int parse(String templateCode) {
        return new ParameterParser(templateCode, new ParameterParserVisitor() {
            @Override
            public void onImport(String importClass) {
                importClasses.add(importClass);
            }

            @Override
            public void onParameter(String parameter) {
                parameters.add(new ParamInfo(parameter));
            }
        }).parse();
    }

    static final class ParamInfo {
        final String type;
        final String name;
        final String defaultValue;

        private ParamInfo(String parameterString) {
            int typeStartIndex = -1;
            int typeEndIndex = -1;
            int nameStartIndex = -1;
            int nameEndIndex = -1;
            int defaultValueStartIndex = -1;
            for (int i = 0; i < parameterString.length(); ++i) {
                char character = parameterString.charAt(i);

                if (typeStartIndex == -1) {
                    if (!Character.isWhitespace(character)) {
                        typeStartIndex = i;
                    }
                } else if (typeEndIndex == -1) {
                    if (Character.isWhitespace(character)) {
                        typeEndIndex = i;
                    }
                } else if (nameStartIndex == -1) {
                    if (!Character.isWhitespace(character)) {
                        nameStartIndex = i;
                    }
                } else if (nameEndIndex == -1) {
                    if (Character.isWhitespace(character) || character == '=') {
                        nameEndIndex = i;
                        i += 1;
                    }
                } else if (defaultValueStartIndex == -1) {
                    if (!Character.isWhitespace(character)) {
                        defaultValueStartIndex = i;
                    }
                }
            }

            if (typeStartIndex == -1 || typeEndIndex == -1) {
                this.type = "";
            } else {
                this.type = parameterString.substring(typeStartIndex, typeEndIndex);
            }

            if (nameEndIndex == -1) {
                nameEndIndex = parameterString.length();
            }

            this.name = parameterString.substring(nameStartIndex, nameEndIndex);

            if (defaultValueStartIndex == -1) {
                this.defaultValue = null;
            } else {
                this.defaultValue = parameterString.substring(defaultValueStartIndex);
            }
        }
    }
}
