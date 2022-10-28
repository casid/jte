package gg.jte.compiler;

import gg.jte.TemplateConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface CodeGenerator extends TemplateParserVisitor {
    String getCode();

    List<byte[]> getBinaryTextParts();

    int getCurrentTemplateLine();

    static void writeAttributeMap(CodeBuilder code, TemplateParser.HtmlTag htmlTag) {
        code.append("gg.jte.runtime.TemplateUtils.toMap(");
        boolean firstWritten = false;
        for (TemplateParser.HtmlAttribute attribute : htmlTag.attributes) {
            if (firstWritten) {
                code.append(",");
            } else {
                firstWritten = true;
            }
            code.append("\"").append(attribute.name).append("\",");
            if (attribute.value == null) {
                if (attribute.bool) {
                    code.append("true");
                } else {
                    code.append("null");
                }
            } else {
                List<TemplateExpressionPart> expressionParts = extractTemplateExpressionParts(attribute.value);
                if (!expressionParts.isEmpty()) {
                    int index = 0;
                    for (TemplateExpressionPart expressionPart : expressionParts) {
                        if (index++ > 0) {
                            code.append(" + \"\" + ");
                        }

                        switch (expressionPart.type) {
                            case Code:
                                code.append("(").append(expressionPart.value).append(")");
                                break;
                            case Text:
                                code.append("\"").appendEscaped(expressionPart.value).append("\"");
                                break;
                        }
                    }
                } else {
                    code.append("\"").appendEscaped(attribute.value).append("\"");
                }
            }
        }
        code.append(")");
    }

    /**
     * In case the given value was previously checked to contain only a single template expression,
     * this method could be used instead of a more expensive call to extractTemplateExpressionParts.
     */
    static String extractSingleOutputTemplateExpression(String value) {
        int startIndex = value.indexOf("${");
        if (startIndex == -1) {
            return null;
        }

        int endIndex = value.lastIndexOf('}');
        if (endIndex == -1) {
            return null;
        }

        return value.substring(startIndex + 2, endIndex);
    }

    static List<TemplateExpressionPart> extractTemplateExpressionParts(String value) {
        if (!value.contains("${") || !value.contains("}")) {
            return Collections.emptyList(); // Avoid parser creation in case of no expression
        }

        List<TemplateExpressionPart> parts = new ArrayList<>();

        TemplateParser parser = new TemplateParser(value, TemplateType.Content, new TemplateParserVisitorAdapter() {

            @Override
            public void onTextPart(int depth, String textPart) {
                if (!textPart.isEmpty()) {
                    parts.add(new TemplateExpressionPart(TemplateExpressionPart.Type.Text, textPart));
                }
            }

            @Override
            public void onCodePart(int depth, String codePart) {
                parts.add(new TemplateExpressionPart(TemplateExpressionPart.Type.Code, codePart));
            }

        }, TemplateConfig.PLAIN, null);

        parser.parse();

        return parts;
    }

    class TemplateExpressionPart {
        final Type type;
        final String value;

        public TemplateExpressionPart(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        enum Type {
            Text,
            Code,
        }
    }
}
