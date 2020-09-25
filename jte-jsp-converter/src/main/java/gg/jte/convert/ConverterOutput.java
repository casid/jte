package gg.jte.convert;

public interface ConverterOutput {
    ConverterOutput append(String s);

    ConverterOutput prepend(String s);

    void setTrimWhitespace(boolean value);

    boolean isTrimWhitespace();

    void setInsideScript(boolean value);

    boolean isInsideScript();
}
