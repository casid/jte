package gg.jte.convert;

import org.apache.jasper.compiler.JtpCustomTag;

public interface CustomTagConverter {
    void before(JtpCustomTag tag, ConverterOutput output);

    void after(JtpCustomTag tag, ConverterOutput output);
}
