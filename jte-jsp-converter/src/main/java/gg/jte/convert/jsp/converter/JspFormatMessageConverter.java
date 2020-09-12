package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspFormatMessageConverter extends AbstractJspTagConverter {

    private boolean nested;
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
            nested = parser.getParentConverter() instanceof JspFormatParamConverter;
            if (!nested) {
                result.append("${");
            }
            result.append("localize(").append(key);
        } else {
            result.append("!{var ").append(var).append(" = ").append("localize(").append(key);
        }
    }

    @Override
    public void convertTagEnd(Parser parser, StringBuilder result) {
        if (var == null) {
            if (!nested) {
                result.append(")}");
            } else {
                result.append(")");
            }
        } else {
            result.append(");}");
        }
    }

    @Override
    protected void onBodyDetected(Parser parser) {
        parser.advanceIndexAfter('\n');
        parser.markLastContentIndexAfterTag(true);
    }

    @Override
    protected boolean dropOpeningTagLine() {
        return nested;
    }

    @Override
    protected boolean dropClosingTagLine() {
        return nested;
    }

    @Override
    protected boolean dropLineBreakAfterTag() {
        return nested;
    }

    public Converter newInstance() {
        return new JspFormatMessageConverter();
    }
}
