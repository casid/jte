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
        if (value == null || value.isEmpty()) {
            return null;
        }

        return output -> output.writeContent(value);
    }

    @SuppressWarnings("unused") // Called by template code
    default Content localize(String key, Object ... params) {
        String value = lookup(key);
        if (value == null || value.isEmpty()) {
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
                if (param instanceof String string) {
                    output.writeUserContent(string);
                } else if (param instanceof Content content) {
                    output.writeUserContent(content);
                } else if (param instanceof Enum<?> enum1) {
                    output.writeUserContent(enum1);
                } else if (param instanceof Boolean boolean1) {
                    output.writeUserContent(boolean1);
                } else if (param instanceof Byte byte1) {
                    output.writeUserContent(byte1);
                } else if (param instanceof Short short1) {
                    output.writeUserContent(short1);
                } else if (param instanceof Integer integer) {
                    output.writeUserContent(integer);
                } else if (param instanceof Long long1) {
                    output.writeUserContent(long1);
                } else if (param instanceof Float float1) {
                    output.writeUserContent(float1);
                } else if (param instanceof Double double1) {
                    output.writeUserContent(double1);
                } else if (param instanceof Character character) {
                    output.writeUserContent(character);
                }
            }
        };
    }
}
