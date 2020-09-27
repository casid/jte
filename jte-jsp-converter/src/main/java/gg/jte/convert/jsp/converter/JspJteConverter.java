package gg.jte.convert.jsp.converter;

import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.ConverterOutput;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JspJteConverter implements CustomTagConverter {

    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        var jteTagPath = tag.getAttribute("jte");
        var attributes = tag.getAttributes();

        String pathWithoutExtension = jteTagPath.substring(0, jteTagPath.length() - 4);
        String tagCall = pathWithoutExtension.replace('/', '.');

        output.append("@").append(tagCall).append("(");

        boolean first = true;
        for (int i = 0; i < attributes.getLength(); i++) {
            String localName = attributes.getLocalName(i);
            if ("jte".equals(localName)) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                output.append(", ");
            }

            output.append(localName).append(" = ").append(convertAttributeValue(attributes.getValue(i)));
        }

        output.append(")");
    }
}
