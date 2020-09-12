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

            boolean writeAsOutput = isWriteAsOutput(parser);

            if (writeAsOutput) {
                parser.getResult().append("${");
            }

            parser.getResult().append(javaCode);

            if (writeAsOutput) {
                parser.getResult().append('}');
            }

            parser.markLastContentIndex();
            parser.advanceIndex(-1);
            return true;
        }

        return false;
    }

    private boolean isWriteAsOutput(Parser parser) {
        if (parser.getParentConverter() instanceof JspFormatParamConverter) {
            return false;
        }
        return true;
    }

    @Override
    public Converter newInstance() {
        return new JspOutputConverter();
    }
}
