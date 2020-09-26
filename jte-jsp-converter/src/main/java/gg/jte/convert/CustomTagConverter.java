package gg.jte.convert;

import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpCustomTag;

public interface CustomTagConverter {
    void convert(JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException;

    default boolean isTrimWhitespace() {
        return false;
    }
}
