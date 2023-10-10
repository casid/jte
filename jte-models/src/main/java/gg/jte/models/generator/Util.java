package gg.jte.models.generator;

import gg.jte.extension.api.ParamDescription;
import gg.jte.extension.api.TemplateDescription;

import java.util.stream.Collectors;

public class Util {
    private Util() {
    }

    public static String typedParams(TemplateDescription template) {
        return template.params().stream().map(param -> String.format("%s %s", param.type(), param.name())).collect(Collectors.joining(", "));
    }

    public static String kotlinTypedParams(TemplateDescription template, boolean includeParamDefaultValue) {
        return template.params().stream().map(param -> {
            StringBuilder formatedParam = new StringBuilder();
            formatedParam.append(param.name()).append(": ").append(param.type());
            if (includeParamDefaultValue && param.defaultValue() != null && !param.defaultValue().startsWith("@`")) {
                formatedParam.append(" = ").append(param.defaultValue());
            }
            return formatedParam.toString();
        }).collect(Collectors.joining(", "));
    }

    public static String paramNames(TemplateDescription template) {
        if (template.params().isEmpty()) {
            return "";
        }
        return template.params().stream().map(ParamDescription::name).collect(Collectors.joining(", ", ", ", ""));
    }

    public static String methodName(TemplateDescription template) {
        String name = template.name();
        StringBuilder builder = new StringBuilder();
        boolean end = false;
        boolean capitalizeNextCharacter = false;
        for (int i = 0; i < name.length() && !end; i++) {
            char c = name.charAt(i);
            switch (c) {
                case '.':
                    end = true;
                    break;
                case '-':
                case '/':
                    capitalizeNextCharacter = true;
                    break;
                default:
                    if (allowedCharacter(i, c)) {
                        builder.append(capitalizeNextCharacter ? Character.toUpperCase(c) : c);
                    }
                    capitalizeNextCharacter = false;
                    break;
            }
        }
        return builder.toString();
    }

    private static boolean allowedCharacter(int index, char c) {
        return index == 0 ? Character.isJavaIdentifierStart(c) : Character.isJavaIdentifierPart(c);
    }
}
