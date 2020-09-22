package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspIfConverter implements CustomTagConverter {

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
        output.append("@if(").append(JspExpressionConverter.convertAttributeValue(tag.getAttribute("test"))).append(")");
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        output.append("@endif");
    }
}
