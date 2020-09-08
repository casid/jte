package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;

public class JspTaglibConverter implements Converter {
    public boolean canConvert(Parser parser) {
        if (!parser.startsWith("<%@")) {
            return false;
        }

        return parser.hasNextToken("taglib", 3);
    }

    public boolean advance(Parser parser) {
        return parser.endsWith("%>");
    }

    @Override
    public void convert(StringBuilder result) {
        // Nothing to convert
    }

    public Converter newInstance() {
        return new JspTaglibConverter();
    }
}
