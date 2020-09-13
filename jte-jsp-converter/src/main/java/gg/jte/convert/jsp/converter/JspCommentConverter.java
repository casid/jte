package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;

public class JspCommentConverter implements Converter {
    int startIndex = -1;

    @Override
    public boolean canConvert(Parser parser) {
        return parser.startsWith("<%--") && parser.getCurrentConverter() == null;
    }

    @Override
    public boolean advance(Parser parser) {
        if (startIndex == -1) {
            startIndex = parser.getIndex();
        }

        if (parser.endsWith("--%>")) {
            String comment = parser.substring(startIndex, parser.getIndex());

            parser.getResult().append(comment);
            parser.markLastContentIndex();
            return true;
        }

        return false;
    }

    @Override
    public Converter newInstance() {
        return new JspCommentConverter();
    }
}
