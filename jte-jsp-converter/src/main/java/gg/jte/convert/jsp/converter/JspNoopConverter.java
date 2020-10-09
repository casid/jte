package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspNoopConverter implements CustomTagConverter {
    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        output.append("<").append(tag.getTagName());
        for (int i = 0; i < tag.getAttributes().getLength(); i++) {
            output.append(" ");

            output.append(tag.getAttributes().getLocalName(i));
            output.append("=\"");
            output.append(tag.getAttributes().getValue(i));
            output.append("\"");
        }
        output.append(">");
        bodyConverter.convert();
        output.append("</").append(tag.getTagName()).append(">");
    }
}
