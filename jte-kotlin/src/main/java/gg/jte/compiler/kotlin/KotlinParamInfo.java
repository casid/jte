package gg.jte.compiler.kotlin;

import gg.jte.compiler.ParamInfo;
import gg.jte.compiler.TemplateParserVisitor;

public final class KotlinParamInfo {

    private KotlinParamInfo() {
    }

    public static ParamInfo parse(String parameterString, TemplateParserVisitor visitor, int templateLine) {
        String type;
        String name;
        String defaultValue;
        boolean varargs = parameterString.startsWith("vararg");

        int nameStartIndex = -1;
        int nameEndIndex = -1;
        int typeStartIndex = -1;
        int typeEndIndex = -1;
        int defaultValueStartIndex = -1;
        int genericDepth = 0;
        for (int i = varargs ? 7 : 0; i < parameterString.length(); ++i) {
            char character = parameterString.charAt(i);

            if (character == '<') {
                ++genericDepth;
            } else if (character == '>') {
                --genericDepth;
            }

            if (genericDepth > 0) {
                continue;
            }

            if (nameStartIndex == -1) {
                if (!Character.isWhitespace(character)) {
                    nameStartIndex = i;
                }
            } else if (nameEndIndex == -1) {
                if (Character.isWhitespace(character) || character == ':') {
                    nameEndIndex = i;
                }
            } else if (typeStartIndex == -1) {
                if (!Character.isWhitespace(character) && character != ':') {
                    typeStartIndex = i;
                }
            } else if (typeEndIndex == -1) {
                if ((Character.isWhitespace(character)) || character == '=') {
                    typeEndIndex = i;
                } else if (i == parameterString.length() - 1) {
                    typeEndIndex = i + 1;
                    break;
                }
            } else if (defaultValueStartIndex == -1) {
                if (!Character.isWhitespace(character) && character != '=') {
                    defaultValueStartIndex = i;
                }
            }
        }

        if (typeStartIndex == -1 || typeEndIndex == -1) {
            type = "";
        } else {
            type = parameterString.substring(typeStartIndex, typeEndIndex);
        }

        if (nameEndIndex == -1) {
            nameEndIndex = parameterString.length();
        }

        if (typeStartIndex == -1) {
            visitor.onError("Missing parameter type: '@param " + parameterString + "'");
        }

        name = parameterString.substring(nameStartIndex, nameEndIndex);

        if (defaultValueStartIndex == -1) {
            defaultValue = null;
        } else {
            defaultValue = parameterString.substring(defaultValueStartIndex).stripTrailing();
        }

        return new ParamInfo(type, name, defaultValue, varargs, templateLine);
    }
}
