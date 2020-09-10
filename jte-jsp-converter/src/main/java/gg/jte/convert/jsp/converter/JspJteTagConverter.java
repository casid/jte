package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

import java.util.Map;

public class JspJteTagConverter implements Converter {
    private final String jteTag;

    private String jteTagPath;
    private Map<String, String> params;

    public JspJteTagConverter(String jteTag) {
        this.jteTag = jteTag;
    }

    public boolean canConvert(Parser parser) {
        return parser.startsWith(jteTag);
    }

    public boolean advance(Parser parser) {
        XmlAttributesParser xmlAttributes = parser.parseXmlAttributes(jteTag.length());

        Map<String, String> attributes = xmlAttributes.getAttributes();

        jteTagPath = attributes.remove("jte");
        params = attributes;

        return true;
    }

    @Override
    public void convert(StringBuilder result) {
        String pathWithoutExtension = jteTagPath.substring(0, jteTagPath.length() - 4);
        String tagCall = pathWithoutExtension.replace('/', '.');

        result.append('@').append(tagCall).append('(');
        boolean first = true;
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(param.getKey()).append(" = ").append(JspExpressionConverter.convertAttributeValue(param.getValue()));
        }
        result.append(')');
    }

    public Converter newInstance() {
        return new JspJteTagConverter(jteTag);
    }
}
