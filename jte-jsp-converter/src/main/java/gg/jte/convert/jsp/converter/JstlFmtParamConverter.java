package gg.jte.convert.jsp.converter;

import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.ConverterOutput;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JstlFmtParamConverter implements CustomTagConverter {

    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        output.append(", ");

        var value = tag.getAttribute("value");
        if (value != null) {
            output.append(convertAttributeValue(value));
        }

        bodyConverter.convert();
    }

    @Override
    public boolean isTrimWhitespace() {
        return true;
    }
}
