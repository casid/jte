package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspFormatParamConverter extends AbstractJspTagConverter {

    private String value;

    public JspFormatParamConverter() {
        super("fmt:param");
    }

    @Override
    public boolean canConvert(Parser parser) {
        return super.canConvert(parser) && parser.getCurrentConverter() instanceof JspFormatMessageConverter;
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        value = attributes.get("value");
    }

    @Override
    public void convertTagBegin(Parser parser, StringBuilder result) {
        parser.removeLeadingSpaces();
        parser.advanceIndexAfter('\n');

        value = JspExpressionConverter.convertAttributeValue(value);

        result.append(", ").append(value);
    }

    @Override
    public void convertTagEnd(Parser parser, StringBuilder result) {
        // Nothing to do
    }

    @Override
    protected boolean dropClosingTagLine() {
        return true;
    }

    public Converter newInstance() {
        return new JspFormatParamConverter();
    }
}
