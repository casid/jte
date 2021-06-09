package gg.jte.compiler;

import java.util.List;

public interface CodeGenerator extends TemplateParserVisitor {
    String getCode();

    List<byte[]> getBinaryTextParts();

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
                    code.append("\"").append(attribute.value).append("\"");
                }
            }
        }
        code.append(")");
    }

    static String extractTemplateExpression(String value) {
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
}
