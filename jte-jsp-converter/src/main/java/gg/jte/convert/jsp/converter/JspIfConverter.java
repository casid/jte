package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;

public class JspIfConverter implements Converter {
    private String test;

    public boolean canConvert(Parser parser) {
        if (!parser.startsWith("<c:if")) {
            return false;
        }

        return parser.hasNextToken("test", 5);
    }

    public boolean advance(Parser parser) {
        parser.parseXmlAttribute("test", v -> test = v);

        return parser.endsWith(">");
    }

    @Override
    public void convert(StringBuilder result) {
        if (test != null) {
            test = new JspExpressionConverter(test).getJavaCode();
        }

        result.append("@if(").append(test).append(")");
    }

    public Converter newInstance() {
        return new JspIfConverter();
    }
}
