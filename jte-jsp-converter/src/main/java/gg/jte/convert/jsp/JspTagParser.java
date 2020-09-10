package gg.jte.convert.jsp;

import gg.jte.convert.Parser;
import gg.jte.convert.jsp.converter.*;

public class JspTagParser extends Parser {
    public JspTagParser(String jteTag) {
        register(new JspTaglibConverter());
        register(new JspAttributeConverter());
        register(new JspIfConverter());
        register(new JspEndIfConverter());
        register(new JspOutputConverter());
        register(new JspJteTagConverter(jteTag));
    }
}
