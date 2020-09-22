package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspForEachConverter implements CustomTagConverter {

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
        var items = JspExpressionConverter.convertAttributeValue(tag.getAttribute("items"));

        output.append("@for(var ").append(tag.getAttribute("var")).append(" : ").append(items).append(")");
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        output.append("@endfor");
    }
}
