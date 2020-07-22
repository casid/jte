package org.jusecase.jte.html;

import org.jusecase.jte.output.TemplateOutputSupplier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface HtmlTemplateLocalizer {
    Pattern pattern = Pattern.compile("\\{(\\d+)}");

    String lookup(String key);

    @SuppressWarnings("unused") // Called by template code
    default HtmlTemplateOutputSupplier localize(String key) {
        String value = lookup(key);
        if (value == null) {
            return null;
        }

        return new HtmlTemplateOutputSupplier() {
            @Override
            public void writeContent(HtmlTemplateOutput output) {
                output.writeContent(value);
            }

            @Override
            public void writeTagBodyUserContent(HtmlTemplateOutput output, String tagName) {
                output.writeContent(value);
            }

            @Override
            public void writeTagAttributeUserContent(HtmlTemplateOutput output, String tagName, String attributeName) {
                output.writeContent(value);
            }
        };
    }

    @SuppressWarnings("unused") // Called by template code
    default HtmlTemplateOutputSupplier localize(String key, Object ... params) {
        String value = lookup(key);
        if (value == null) {
            return null;
        }

        return new HtmlTemplateOutputSupplier() {
            @Override
            public void writeTagBodyUserContent(HtmlTemplateOutput output, String tagName) {
                process(output, tagName, null);
            }

            @Override
            public void writeTagAttributeUserContent(HtmlTemplateOutput output, String tagName, String attributeName) {
                process(output, tagName, attributeName);
            }

            @Override
            public void writeContent(HtmlTemplateOutput output) {
                process(output, null, null);
            }

            private void process(HtmlTemplateOutput output, String tagName, String attributeName) {
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
                                writeParam(output, param, tagName, attributeName);
                            }
                        }
                    } while (matcher.find());

                    output.writeContent(value.substring(startIndex));
                } else {
                    output.writeContent(value);
                }
            }

            private void writeParam(HtmlTemplateOutput output, Object param, String tagName, String attributeName) {
                if (tagName == null && attributeName == null) {
                    if (param instanceof String) {
                        output.writeContent((String) param);
                    } else if (param instanceof TemplateOutputSupplier) {
                        output.writeContent((TemplateOutputSupplier) param);
                    } else if (param instanceof Enum) {
                        output.writeContent((Enum<?>) param);
                    } else if (param instanceof Boolean) {
                        output.writeContent((boolean) param);
                    } else if (param instanceof Byte) {
                        output.writeContent((byte) param);
                    } else if (param instanceof Short) {
                        output.writeContent((short) param);
                    } else if (param instanceof Integer) {
                        output.writeContent((int) param);
                    } else if (param instanceof Long) {
                        output.writeContent((long) param);
                    } else if (param instanceof Float) {
                        output.writeContent((float) param);
                    } else if (param instanceof Double) {
                        output.writeContent((double) param);
                    } else if (param instanceof Character) {
                        output.writeContent((char) param);
                    }
                } else if (tagName != null && attributeName == null) {
                    if (param instanceof String) {
                        output.writeTagBodyUserContent((String) param, tagName);
                    } else if (param instanceof HtmlTemplateOutputSupplier) {
                        output.writeTagBodyUserContent((HtmlTemplateOutputSupplier) param, tagName);
                    } else if (param instanceof Enum) {
                        output.writeTagBodyUserContent((Enum<?>) param, tagName);
                    } else if (param instanceof Boolean) {
                        output.writeTagBodyUserContent((boolean) param, tagName);
                    } else if (param instanceof Byte) {
                        output.writeTagBodyUserContent((byte) param, tagName);
                    } else if (param instanceof Short) {
                        output.writeTagBodyUserContent((short) param, tagName);
                    } else if (param instanceof Integer) {
                        output.writeTagBodyUserContent((int) param, tagName);
                    } else if (param instanceof Long) {
                        output.writeTagBodyUserContent((long) param, tagName);
                    } else if (param instanceof Float) {
                        output.writeTagBodyUserContent((float) param, tagName);
                    } else if (param instanceof Double) {
                        output.writeTagBodyUserContent((double) param, tagName);
                    } else if (param instanceof Character) {
                        output.writeTagBodyUserContent((char) param, tagName);
                    }
                } else {
                    if (param instanceof String) {
                        output.writeTagAttributeUserContent((String) param, tagName, attributeName);
                    } else if (param instanceof HtmlTemplateOutputSupplier) {
                        output.writeTagAttributeUserContent((HtmlTemplateOutputSupplier) param, tagName, attributeName);
                    } else if (param instanceof Enum) {
                        output.writeTagAttributeUserContent((Enum<?>) param, tagName, attributeName);
                    } else if (param instanceof Boolean) {
                        output.writeTagAttributeUserContent((boolean) param, tagName, attributeName);
                    } else if (param instanceof Byte) {
                        output.writeTagAttributeUserContent((byte) param, tagName, attributeName);
                    } else if (param instanceof Short) {
                        output.writeTagAttributeUserContent((short) param, tagName, attributeName);
                    } else if (param instanceof Integer) {
                        output.writeTagAttributeUserContent((int) param, tagName, attributeName);
                    } else if (param instanceof Long) {
                        output.writeTagAttributeUserContent((long) param, tagName, attributeName);
                    } else if (param instanceof Float) {
                        output.writeTagAttributeUserContent((float) param, tagName, attributeName);
                    } else if (param instanceof Double) {
                        output.writeTagAttributeUserContent((double) param, tagName, attributeName);
                    } else if (param instanceof Character) {
                        output.writeTagAttributeUserContent((char) param, tagName, attributeName);
                    }
                }
            }
        };
    }
}
