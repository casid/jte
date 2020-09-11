package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspWhenConverter extends AbstractJspTagConverter {

    private String test;

    public JspWhenConverter() {
        super("c:when");
    }

    @Override
    public boolean canConvert(Parser parser) {
        return super.canConvert(parser) && parser.getCurrentConverter() instanceof JspChooseConverter;
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        test = attributes.get("test");
    }

    @Override
    public void convertTagBegin(Parser parser, StringBuilder result) {
        if (test != null) {
            test = new JspExpressionConverter(test).getJavaCode();
        } else {
            test = "???";
        }

        if (isFirst(parser)) {
            result.append("@if(").append(test).append(")");
        } else {
            result.append("@elseif(").append(test).append(")");
        }
    }

    private boolean isFirst(Parser parser) {
        boolean first = false;

        Converter parentConverter = parser.getParentConverter();
        if (parentConverter instanceof JspChooseConverter) {
            JspChooseConverter chooseConverter = (JspChooseConverter)parentConverter;
            first = chooseConverter.getWhenCount() == 0;
            chooseConverter.incrementWhenCount();
        }

        return first;
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
        return new JspWhenConverter();
    }
}
