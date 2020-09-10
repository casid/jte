package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;

public class JspOutputConverter implements Converter {
    int startIndex = -1;

    @Override
    public boolean canConvert(Parser parser) {
        return parser.startsWith("${");
    }

    @Override
    public boolean advance(Parser parser) {
        if (startIndex == -1) {
            startIndex = parser.getIndex();
        }

        if (parser.endsWith("}")) {
            String expression = parser.substring(startIndex, parser.getIndex());
            String javaCode = new JspExpressionConverter(expression).getJavaCode();

            parser.getResult().append("${").append(javaCode).append('}');
            parser.markLastContentIndex();
            return true;
        }

        return false;
    }

    @Override
    public Converter newInstance() {
        return new JspOutputConverter();
    }
}
