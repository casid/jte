package gg.jte.convert.jsp.converter;

import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.ConverterOutput;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspForEachConverter implements CustomTagConverter {

    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        String items = JspExpressionConverter.convertAttributeValue(tag.getAttribute("items"));

        String var = tag.getAttribute("var");
        String varStatus = tag.getAttribute("varStatus");

        if (varStatus == null) {
            output.append("@for(var ").append(var).append(" : ").append(items).append(")");
        } else {
            converter.addImport("gg.jte.support.ForSupport");
            output.append("@for(var ").append(varStatus).append(" : ForSupport.of(").append(items).append("))");
            output.newLine().indent(1);
            output.append("!{var ").append(var).append(" = ").append(varStatus).append(".get()").append(";}");
        }

        bodyConverter.convert();

        output.append("@endfor");
    }
}
