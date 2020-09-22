package gg.jte.convert.jsp;

import gg.jte.convert.CustomTagConverter;

public interface Converter {
    void register(String tagName, CustomTagConverter converter);

    String convert();

    void setPrefix(String prefix);
}
