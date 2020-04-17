package org.jusecase.jte.internal;

final class ParameterParser {
    int parse(String tagCode, ParameterParserVisitor visitor) {
        int lastIndex = 0;

        while (true) {
            int attributeStart = tagCode.indexOf("@param", lastIndex);
            if (attributeStart == -1) {
                break;
            }

            int attributeEnd = tagCode.indexOf("\n", attributeStart);
            if (attributeEnd == -1) {
                break;
            }

            String parameter = tagCode.substring(attributeStart + 6, attributeEnd).trim();
            visitor.onParameter(parameter);
            lastIndex = attributeEnd + 1;
        }

        return lastIndex;
    }
}
