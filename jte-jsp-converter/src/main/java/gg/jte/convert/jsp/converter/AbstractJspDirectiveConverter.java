package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.xml.XmlAttributesParser;

public abstract class AbstractJspDirectiveConverter implements Converter {

    private final String directive;
    private XmlAttributesParser attributes;

    public AbstractJspDirectiveConverter(String directive) {
        this.directive = directive;
    }

    public boolean canConvert(Parser parser) {
        if (parser.getCurrentConverter() != null) {
            return false;
        }

        if (!parser.startsWith("<%@")) {
            return false;
        }

        return parser.hasNextToken(directive, 3);
    }

    @Override
    public final boolean advance(Parser parser) {
        if (attributes == null) {
            attributes = parser.parseXmlAttributes(4 + directive.length());
            parseAttributes(attributes);
        } else if (parser.endsWith("%>")) {
            convertDirective(parser, parser.getResult());
            parser.markLastContentIndex();
            return true;
        }

        return false;
    }

    protected abstract void parseAttributes(XmlAttributesParser attributes);

    public abstract void convertDirective(Parser parser, StringBuilder result);
}
