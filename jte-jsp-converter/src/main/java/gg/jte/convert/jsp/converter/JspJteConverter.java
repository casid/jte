package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JspJteConverter implements CustomTagConverter {

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
        var jteTagPath = tag.getAttribute("jte");
        var attributes = tag.getAttributes();

        String pathWithoutExtension = jteTagPath.substring(0, jteTagPath.length() - 4);
        String tagCall = pathWithoutExtension.replace('/', '.');

        output.append("@").append(tagCall).append("(");

        boolean first = true;
        for (int i = 0; i < attributes.getLength(); i++) {
            if (first) {
                first = false;
            } else {
                output.append(", ");
            }

            output.append(attributes.getLocalName(i)).append(" = ").append(convertAttributeValue(attributes.getValue(i)));
        }

        output.append(")");
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        // nothing to do
    }
}
