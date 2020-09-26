package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpCustomTag;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JspSetConverter implements CustomTagConverter {

    @Override
    public void convert(JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        output.append("!{var ").append(tag.getAttribute("var")).append(" = ");

        var value = tag.getAttribute("value");

        if (value != null) {
            output.append(convertAttributeValue(value));
        } else {
            output.append("@`");
        }

        bodyConverter.convert();

        if (value != null) {
            output.append(";}");
        } else {
            output.append("`;}");
        }
    }
}
