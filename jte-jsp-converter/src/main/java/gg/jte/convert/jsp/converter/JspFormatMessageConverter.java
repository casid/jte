package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspFormatMessageConverter extends AbstractJspTagConverter {

    private String var;
    private String key;

    public JspFormatMessageConverter() {
        super("fmt:message");
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        var = attributes.get("var");
        key = attributes.get("key");
    }

    @Override
    public void convertTagBegin(Parser parser, StringBuilder result) {
        key = JspExpressionConverter.convertAttributeValue(key);

        if (var == null) {
            result.append("${localize(").append(key);
        } else {
            result.append("!{var ").append(var).append(" = ").append("localize(").append(key);
        }

        /*
        result.append("!{var ").append(var).append(" = ");

        if (value != null) {
            value = JspExpressionConverter.convertAttributeValue(value);
            result.append(value).append(";}");
        } else {
            result.append("@`");
        }*/
    }

    @Override
    public void convertTagEnd(Parser parser, StringBuilder result) {
        if (var == null) {
            result.append(")}");
        } else {
            result.append(");}");
        }
    }

    @Override
    protected void onBodyDetected(Parser parser) {
        parser.advanceIndexAfter('\n');
        parser.markLastContentIndexAfterTag(true);
    }

    public Converter newInstance() {
        return new JspFormatMessageConverter();
    }
}
