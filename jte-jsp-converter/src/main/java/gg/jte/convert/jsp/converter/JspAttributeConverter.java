package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;

public class JspAttributeConverter implements Converter {
    private String type;
    private String name;
    private boolean required;

    public boolean canConvert(Parser parser) {
        if (!parser.startsWith("<%@")) {
            return false;
        }

        return parser.hasNextToken("attribute", 3);
    }

    public boolean advance(Parser parser) {
        parser.parseXmlAttribute("type", v -> type = v);
        parser.parseXmlAttribute("name", v -> name = v);
        parser.parseXmlAttributeAsBoolean("required", v -> required = v);

        return parser.endsWith("%>");
    }

    @Override
    public void convert(StringBuilder result) {
        if (type == null) {
            type = "Object";
        } else if (type.startsWith("java.lang.")) {
            type = type.substring("java.lang.".length());
        }

        result.append("@param ").append(type).append(" ").append(name);

        if (!required) {
            result.append(" = ");
            result.append("null"); // TODO depending on type
        }
    }

    public Converter newInstance() {
        return new JspAttributeConverter();
    }
}
