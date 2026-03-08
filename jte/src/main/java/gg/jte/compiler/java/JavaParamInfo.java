package gg.jte.compiler.java;

import gg.jte.compiler.ParamInfo;
import gg.jte.compiler.TemplateParserVisitor;

public class JavaParamInfo {
    public static ParamInfo parse(String parameterString, TemplateParserVisitor visitor, int templateLine) {
        String type;
        String name;
        String defaultValue;

        int nameEndIndex = -1;
        int defaultValueStartIndex = -1;
        int genericDepth = 0;
        for (int i = 0; i < parameterString.length(); ++i) {
            char character = parameterString.charAt(i);

            if (character == '<') {
                ++genericDepth;
            } else if (character == '>') {
                --genericDepth;
            }

            if (genericDepth > 0) {
                continue;
            }

            if (nameEndIndex == -1) {
                if (character == '=') {
                    nameEndIndex = i - 1;
                }
            } else if (defaultValueStartIndex == -1) {
                if (!Character.isWhitespace(character)) {
                    defaultValueStartIndex = i;
                }
            }
        }

        if (nameEndIndex == -1) {
            nameEndIndex = parameterString.length() - 1;
        }

        int typeNameSeparator = -1;
        for (int i = nameEndIndex; i >= 0; --i) {
            char character = parameterString.charAt(i);

            if (Character.isWhitespace(character)) {
                if (i == nameEndIndex) {
                    --nameEndIndex; // trailing name whitespace
                } else {
                    typeNameSeparator = i;
                    break;
                }
            }
        }

        if (typeNameSeparator == -1) {
            visitor.onError("Missing parameter name: '@param " + parameterString + "'");
        }

        type = parameterString.substring(0, typeNameSeparator);
        name = parameterString.substring(typeNameSeparator + 1, nameEndIndex + 1);

        if (defaultValueStartIndex == -1) {
            defaultValue = null;
        } else {
            defaultValue = parameterString.substring(defaultValueStartIndex);
        }

        return new ParamInfo(type, name, defaultValue, type.contains("..."), templateLine);
    }
}
