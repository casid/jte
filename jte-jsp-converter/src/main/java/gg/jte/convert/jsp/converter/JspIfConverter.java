package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspIfConverter extends AbstractJspTagConverter {

    private String test;

    public JspIfConverter() {
        super("c:if");
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        test = attributes.get("test");
    }

    @Override
    public void convertTagBegin(Parser parser, StringBuilder result) {
        if (test != null) {
            test = new JspExpressionConverter(test).getJavaCode();
        } else {
            test = "???";
        }

        result.append("@if(").append(test).append(")");
    }

    @Override
    public void convertTagEnd(Parser parser, StringBuilder result) {
        result.append("@endif");
    }

    public Converter newInstance() {
        return new JspIfConverter();
    }
}
