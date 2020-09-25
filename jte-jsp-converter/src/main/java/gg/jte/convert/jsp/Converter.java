package gg.jte.convert.jsp;

import gg.jte.convert.CustomTagConverter;

public interface Converter {
    void register(String tagName, CustomTagConverter converter);

    void addInlinedInclude(String path);

    String convert();

    void setPrefix(String prefix);
}
