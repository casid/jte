package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspVariableConverter extends AbstractJspTagConverter {

    private String var;
    private String value;

    public JspVariableConverter() {
        super("c:set");
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        var = attributes.get("var");
        value = attributes.get("value");
    }

    @Override
    public void convertTag(Parser parser, StringBuilder result) {
        if (value != null) {
            value = JspExpressionConverter.convertAttributeValue(value);
        } else {
            // TODO content block
        }

        result.append("!{var ").append(var).append(" = ").append(value).append(";}");
    }

    @Override
    public void convertBody(Parser parser, StringBuilder result) {
        result.append("@endif");
    }

    public Converter newInstance() {
        return new JspVariableConverter();
    }
}
