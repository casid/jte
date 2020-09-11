package gg.jte.convert.jsp;

import gg.jte.convert.Parser;
import gg.jte.convert.jsp.converter.*;

public class JspParser extends Parser {
    public JspParser(String jteTag) {
        register(new JspTaglibConverter());
        register(new JspAttributeConverter());
        register(new JspIfConverter());
        register(new JspChooseConverter());
        register(new JspWhenConverter());
        register(new JspOtherwiseConverter());
        register(new JspOutputConverter());
        register(new JspJteTagConverter(jteTag));
        register(new JspCommentConverter());
        register(new JspVariableConverter());
    }
}
