package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspForEachConverter implements CustomTagConverter {

    @Override
    public void convert(JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        var items = JspExpressionConverter.convertAttributeValue(tag.getAttribute("items"));

        output.append("@for(var ").append(tag.getAttribute("var")).append(" : ").append(items).append(")");

        bodyConverter.convert();

        output.append("@endfor");
    }
}
