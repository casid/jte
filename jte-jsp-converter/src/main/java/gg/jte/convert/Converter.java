package gg.jte.convert;

public interface Converter {
    boolean canConvert(Parser parser);
    boolean advance(Parser parser);
    void convert(StringBuilder result);
    Converter newInstance();
}
