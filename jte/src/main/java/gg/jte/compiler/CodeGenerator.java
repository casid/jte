package gg.jte.compiler;

import gg.jte.TemplateConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                String javaExpression = extractTemplateExpression(attribute.value);
                if (javaExpression != null) {
                    code.append(javaExpression);
                } else {
                    code.append("\"");
                    code.appendEscaped(attribute.value);
                    code.append("\"");
                }
            }
        }
        code.append(")");
    }

    static String extractTemplateExpression(String value) {
        if (!value.contains("${") || !value.contains("}")) {
            return null; // Avoid parser creation in case of no expression
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

        if (parts.isEmpty()) {
            return null;
        }

        return parts.stream().map(TemplateExpressionPart::toJavaExpression).collect(Collectors.joining(" + \"\" + "));
    }

    class TemplateExpressionPart {
        final Type type;
        final String value;

        public TemplateExpressionPart(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        public String toJavaExpression() {
            if (type == Type.Code) {
                return "(" + value + ")";
            } else {
                return "\"" + value + "\"";
            }
        }

        enum Type {
            Text,
            Code,
        }
    }
}
