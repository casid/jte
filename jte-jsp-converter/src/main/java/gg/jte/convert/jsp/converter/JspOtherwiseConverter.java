package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspOtherwiseConverter extends AbstractJspTagConverter {

    public JspOtherwiseConverter() {
        super("c:otherwise");
    }

    @Override
    public boolean canConvert(Parser parser) {
        return super.canConvert(parser) && parser.getCurrentConverter() instanceof JspChooseConverter;
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        // has none
    }

    @Override
    public void convertTag(Parser parser, StringBuilder result) {
        result.append("@else");
    }

    @Override
    public void convertBody(Parser parser, StringBuilder result) {
        // Nothing to do
    }

    @Override
    protected boolean dropClosingTagLine() {
        return true;
    }

    public Converter newInstance() {
        return new JspOtherwiseConverter();
    }
}
