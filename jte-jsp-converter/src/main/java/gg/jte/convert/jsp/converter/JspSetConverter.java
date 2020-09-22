package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JspSetConverter implements CustomTagConverter {

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
        output.append("!{var ").append(tag.getAttribute("var")).append(" = ");

        var value = tag.getAttribute("value");

        if (value != null) {
            output.append(convertAttributeValue(value));
        } else {
            output.append("@`");
        }
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        var value = tag.getAttribute("value");

        if (value != null) {
            output.append(";}");
        } else {
            output.append("`;}");
        }
    }
}
