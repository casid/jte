package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspChooseConverter extends AbstractJspTagConverter {

    private int whenCount;

    public JspChooseConverter() {
        super("c:choose");
    }

    @Override
    protected boolean dropOpeningTagLine() {
        return true;
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        // has none
    }

    @Override
    public void convertTagBegin(Parser parser, StringBuilder result) {
        // nothing to write to the jte file
    }

    @Override
    public void convertTagEnd(Parser parser, StringBuilder result) {
        result.append("@endif");
    }

    @Override
    protected void appendContentBeforeClosingTag(Parser parser) {
        parser.decrementSkipIndentations();
        super.appendContentBeforeClosingTag(parser);
        parser.incrementSkipIndentations();
    }

    public void incrementWhenCount() {
        ++whenCount;
    }

    public int getWhenCount() {
        return whenCount;
    }

    @Override
    public void onPushed(Parser parser) {
        parser.incrementSkipIndentations();
    }

    @Override
    public void onPopped(Parser parser) {
        parser.decrementSkipIndentations();
    }

    public Converter newInstance() {
        return new JspChooseConverter();
    }
}
