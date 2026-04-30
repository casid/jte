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
        int parenDepth = 0;

        boolean stringMode = false;
        for (int i = 0; i < parameterString.length(); ++i) {
            char character = parameterString.charAt(i);

            if (character == '"') {
                if (stringMode) {
                    int escapeCount = 0;
                    while (parameterString.charAt(i - escapeCount - 1) == '\\') {
                        ++escapeCount;
                    }

                    if (escapeCount % 2 == 0) {
                        stringMode = false;
                    }
                } else {
                    stringMode = true;
                }
            }

            if (stringMode) {
                continue;
            }

            if (character == '<') {
                ++genericDepth;
            } else if (character == '>') {
                --genericDepth;
            } else if (character == '(') {
                ++parenDepth;
            } else if (character == ')') {
                --parenDepth;
            }

            if (genericDepth > 0 || parenDepth > 0) {
                continue;
            }

            if (character == '=') {
                nameEndIndex = i - 1;

                while (Character.isWhitespace(character)) {
                    ++i;
                    character = parameterString.charAt(i);
                }

                defaultValueStartIndex = i + 1;
                break;
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
