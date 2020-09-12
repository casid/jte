package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;


public class JspForEachConverter extends AbstractJspTagConverter {

    private String var;
    private String items;

    public JspForEachConverter() {
        super("c:forEach");
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        var = attributes.get("var");
        items = attributes.get("items");
    }

    @Override
    public void convertTagBegin(Parser parser, StringBuilder result) {
        items = JspExpressionConverter.convertAttributeValue(items);
        result.append("@for(var ").append(var).append(" : ").append(items).append(")");
    }

    @Override
    public void convertTagEnd(Parser parser, StringBuilder result) {
        result.append("@endfor");
    }

    public Converter newInstance() {
        return new JspForEachConverter();
    }
}
