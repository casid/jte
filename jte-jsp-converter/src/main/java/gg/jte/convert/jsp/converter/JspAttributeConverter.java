package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspAttributeConverter extends AbstractJspDirectiveConverter {

    private String type;
    private String name;
    private boolean required;

    public JspAttributeConverter() {
        super("attribute");
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        type = attributes.get("type");
        name = attributes.get("name");
        required = attributes.getBoolean("required");
    }

    @Override
    public void convertDirective(Parser parser, StringBuilder result) {
        if (type == null) {
            type = "Object";
        } else if (type.equals("java.lang.Boolean")) {
            type = "boolean";
        } else if (type.startsWith("java.lang.")) {
            type = type.substring("java.lang.".length());
        } else {
            parser.addImportStatement("@import " + type);
            type = getSimpleType(type);
        }

        result.append("@param ").append(type).append(" ").append(name);

        if (!required) {
            result.append(" = ");
            result.append("CHOOSE_DEFAULT_VALUE");
        }
    }

    private String getSimpleType(String type) {
        int i = type.lastIndexOf('.');
        if (i == -1) {
            return type;
        }

        return type.substring(i + 1);
    }

    public Converter newInstance() {
        return new JspAttributeConverter();
    }
}
