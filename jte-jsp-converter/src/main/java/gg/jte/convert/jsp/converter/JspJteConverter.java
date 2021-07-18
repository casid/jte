package gg.jte.convert.jsp.converter;

import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.ConverterOutput;
import gg.jte.convert.jsp.BodyConverter;
import gg.jte.runtime.StringUtils;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;
import org.xml.sax.Attributes;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JspJteConverter implements CustomTagConverter {

    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        String jteTagPath = tag.getAttribute("jte");
        Attributes attributes = tag.getAttributes();

        String pathWithoutExtension = jteTagPath.substring(0, jteTagPath.length() - 4);
        String tagCall = pathWithoutExtension.replace('/', '.');
        String tagStartIndent = null;
        if (converter.isPutParametersOnSeparateLines()) {
            int tagStartPos = output.getCurrentLineCharCount();
            tagStartIndent = StringUtils.repeat(output.getIndentationChar(), tagStartPos);
        }
        output.append("@").append(tagCall).append("(");

        boolean first = true;
        for (int i = 0; i < attributes.getLength(); i++) {
            String localName = attributes.getLocalName(i);
            if ("jte".equals(localName)) {
                continue;
            }

            if (converter.isPutParametersOnSeparateLines()) {
                if (!first) {
                    output.append(",");
                }
                output.newLine(tagStartIndent);
                output.indent(1);
            } else if (!first) {
                output.append(", ");
            }

            first = false;

            output.append(localName).append(" = ").append(convertAttributeValue(attributes.getValue(i)));
        }

        if (tag.hasBody()) {
            if (!first) {
                if (converter.isPutParametersOnSeparateLines()) {
                    output.append(",");
                    output.newLine();
                } else {
                    output.append(", ");
                }
            }
            output.append("bodyContent = @`");
            bodyConverter.convert();
            output.append("`");
            first = false;
        }

        if (!first && converter.isPutParametersOnSeparateLines()) {
            output.newLine(tagStartIndent);
        }
        output.append(")");
    }
}
