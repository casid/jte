package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import org.apache.jasper.compiler.JtpAttribute;
import org.apache.jasper.compiler.JtpConverter;

public class JspAttributeConverter {
    public void convert(JtpConverter converter, JtpAttribute attribute, ConverterOutput output) {
        String type = attribute.getType();

        if (type == null) {
            type = "Object";
        } else if (type.equals("java.lang.Boolean")) {
            type = "boolean";
        } else if (type.equals("java.lang.Byte")) {
            type = "byte";
        } else if (type.equals("java.lang.Short")) {
            type = "short";
        } else if (type.equals("java.lang.Integer")) {
            type = "int";
        } else if (type.equals("java.lang.Long")) {
            type = "long";
        } else if (type.equals("java.lang.Float")) {
            type = "float";
        } else if (type.equals("java.lang.Double")) {
            type = "double";
        } else if (type.startsWith("java.lang.")) {
            type = type.substring("java.lang.".length());
        } else {
            converter.addImport(type);
            type = getSimpleType(type);
        }

        output.append("@param ").append(type).append(" ").append(attribute.getName());

        if (!Boolean.parseBoolean(attribute.getAttribute("required"))) {
            output.append(" = ");
            output.append("CHOOSE_DEFAULT_VALUE");
        }
    }

    private String getSimpleType(String type) {
        int i = type.lastIndexOf('.');
        if (i == -1) {
            return type;
        }

        return type.substring(i + 1);
    }
}