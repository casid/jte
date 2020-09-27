package gg.jte.convert.jsp.converter;

import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.ConverterOutput;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspChooseConverter implements CustomTagConverter {

    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        output.incrementSkipIndent();
        bodyConverter.convert();
        output.append("@endif");
        output.decrementSkipIndent();
    }

    @Override
    public boolean isTrimWhitespace() {
        return true;
    }
}
