package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;

public class JspEndIfConverter implements Converter {

    public boolean canConvert(Parser parser) {
        return parser.startsWith("</c:if>");
    }

    public boolean advance(Parser parser) {
        return parser.endsWith(">");
    }

    @Override
    public void convert(StringBuilder result) {
        result.append("@endif");
    }

    public Converter newInstance() {
        return new JspEndIfConverter();
    }
}
