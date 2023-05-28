package gg.jte.models.generator;

import gg.jte.extension.ParamDescription;
import gg.jte.extension.TemplateDescription;

import java.util.stream.Collectors;

public class Util {
    private Util(){}

    public static String typedParams(TemplateDescription template) {
        return template.params()
                .stream()
                .map(param -> String.format("%s %s", param.type(), param.name()))
                .collect(Collectors.joining(", "));
    }

    public static String paramNames(TemplateDescription template) {
        if (template.params().isEmpty()) {
            return "";
        }
        return template.params()
                .stream()
                .map(ParamDescription::name)
                .collect(Collectors.joining(", ", ", ", ""));
    }

    public static String methodName(TemplateDescription template) {
        String name = template.name();
        StringBuilder builder = new StringBuilder();
        boolean end = false;
        boolean slash = false;
        for (int i = 0; i < name.length() && !end; i++) {
            char c = name.charAt(i);
            switch (c) {
                case '.':
                    end = true;
                    break;
                case '/':
                    slash = true;
                    break;
                default:
                    builder.append(slash ? Character.toUpperCase(c) : c);
                    slash = false;
                    break;
            }
        }
        return builder.toString();
    }
}
