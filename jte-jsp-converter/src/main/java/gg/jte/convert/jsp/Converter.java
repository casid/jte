package gg.jte.convert.jsp;

import gg.jte.convert.CustomTagConverter;

import java.util.EnumSet;

public interface Converter {
    void register(String tagName, CustomTagConverter converter);

    void addInlinedInclude(String path);

    void addSuppressions(String path, EnumSet<JspElementType> suppressions);

    void setLineSeparator(String lineSeparator);

    void setIndentationCount(int indentationCount);

    void setIndentationChar(char indentationChar);

    void setPutParametersOnSeparateLines(boolean putParametersOnSeparateLines);

    String convert();

    void setPrefix(String prefix);
}
