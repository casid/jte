package gg.jte.support;

import gg.jte.TemplateOutput;
import gg.jte.Content;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface LocalizationSupport {
    Pattern pattern = Pattern.compile("\\{(\\d+)}");

    String lookup(String key);

    @SuppressWarnings("unused") // Called by template code
    default Content localize(String key) {
        String value = lookup(key);
        if (value == null || value.length() == 0) {
            return null;
        }

        return output -> output.writeContent(value);
    }

    @SuppressWarnings("unused") // Called by template code
    default Content localize(String key, Object ... params) {
        String value = lookup(key);
        if (value == null || value.length() == 0) {
            return null;
        }

        return new Content() {

            @Override
            public void writeTo(TemplateOutput output) {
                Matcher matcher = pattern.matcher(value);
                if (matcher.find()) {
                    int startIndex = 0;
                    do {
                        output.writeContent(value.substring(startIndex, matcher.start()));
                        startIndex = matcher.end();

                        int argumentIndex = Integer.parseInt(matcher.group(1));
                        if (argumentIndex < params.length) {
                            Object param = params[argumentIndex];

                            if (param != null) {
                                writeParam(output, param);
                            }
                        }
                    } while (matcher.find());

                    output.writeContent(value.substring(startIndex));
                } else {
                    output.writeContent(value);
                }
            }

            private void writeParam(TemplateOutput output, Object param) {
                if (param instanceof String) {
                    output.writeUserContent((String) param);
                } else if (param instanceof Content) {
                    output.writeUserContent((Content) param);
                } else if (param instanceof Enum) {
                    output.writeUserContent((Enum<?>) param);
                } else if (param instanceof Boolean) {
                    output.writeUserContent((boolean) param);
                } else if (param instanceof Byte) {
                    output.writeUserContent((byte) param);
                } else if (param instanceof Short) {
                    output.writeUserContent((short) param);
                } else if (param instanceof Integer) {
                    output.writeUserContent((int) param);
                } else if (param instanceof Long) {
                    output.writeUserContent((long) param);
                } else if (param instanceof Float) {
                    output.writeUserContent((float) param);
                } else if (param instanceof Double) {
                    output.writeUserContent((double) param);
                } else if (param instanceof Character) {
                    output.writeUserContent((char) param);
                }
            }
        };
    }
}
