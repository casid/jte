package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JstlFmtParamConverter implements CustomTagConverter {

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
        output.append(", ");

        var value = tag.getAttribute("value");
        if (value != null) {
            output.append(convertAttributeValue(value));
        }
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        // nothing to do
    }

    @Override
    public boolean isTrimWhitespace() {
        return true;
    }
}
