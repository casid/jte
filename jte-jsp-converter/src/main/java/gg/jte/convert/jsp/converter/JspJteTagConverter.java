package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.jsp.JspExpressionConverter;
import gg.jte.convert.xml.XmlAttributesParser;

import java.util.Map;

public class JspJteTagConverter extends AbstractJspTagConverter {
    private final String jteTag;

    private String jteTagPath;
    private Map<String, String> params;

    public JspJteTagConverter(String jteTag) {
        super(jteTag);
        this.jteTag = jteTag;
    }

    @Override
    protected void parseAttributes(XmlAttributesParser attributes) {
        params = attributes.getAttributes();
        jteTagPath = params.remove("jte");
    }

    @Override
    public void convertTag(Parser parser, StringBuilder result) {
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

    @Override
    public void convertBody(Parser parser, StringBuilder result) {
        // Not required
    }

    @Override
    public Converter newInstance() {
        return new JspJteTagConverter(jteTag);
    }
}
