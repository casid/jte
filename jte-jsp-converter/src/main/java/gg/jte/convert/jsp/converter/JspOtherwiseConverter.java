package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspOtherwiseConverter implements CustomTagConverter {

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
        output.append("@else");
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        // written in before or JspChooseConverter.after
    }
}
