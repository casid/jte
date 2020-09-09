package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;

public class JspOutputConverter implements Converter {
    int startIndex = -1;
    String expression;

    @Override
    public boolean canConvert(Parser parser) {
        return parser.startsWith("${");
    }

    @Override
    public boolean advance(Parser parser) {
        if (startIndex == -1) {
            startIndex = parser.getIndex();
        }

        boolean done = parser.endsWith("}");
        if (done) {
            expression = parser.substring(startIndex, parser.getIndex());
        }

        return done;
    }

    @Override
    public void convert(StringBuilder result) {
        String javaCode = new JspExpressionConverter(expression).getJavaCode();
        result.append("${").append(javaCode).append('}');
    }

    @Override
    public Converter newInstance() {
        return new JspOutputConverter();
    }
}
