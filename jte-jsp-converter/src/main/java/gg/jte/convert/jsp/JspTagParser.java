package gg.jte.convert.jsp;

import gg.jte.convert.Parser;
import gg.jte.convert.jsp.converter.JspAttributeConverter;
import gg.jte.convert.jsp.converter.JspEndIfConverter;
import gg.jte.convert.jsp.converter.JspIfConverter;
import gg.jte.convert.jsp.converter.JspTaglibConverter;

public class JspTagParser extends Parser {
    public JspTagParser() {
        register(new JspTaglibConverter());
        register(new JspAttributeConverter());
        register(new JspIfConverter());
        register(new JspEndIfConverter());
    }
}
