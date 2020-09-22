package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspChooseConverter implements CustomTagConverter {

    public CustomTagConverter newInstance() {
        return new JspChooseConverter();
    }

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {

    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        output.append("@endif");
    }
}
