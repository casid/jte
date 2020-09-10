package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.xml.XmlAttributesParser;

public class JspTaglibConverter extends AbstractJspDirectiveConverter {

    public JspTaglibConverter() {
        super("taglib");
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        // we're not interested in those.
    }

    @Override
    public void convertDirective(Parser parser, StringBuilder result) {
        // this directive is ignored in the jte output.
    }

    public Converter newInstance() {
        return new JspTaglibConverter();
    }
}
