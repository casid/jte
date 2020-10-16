package gg.jte.convert.jsp.converter;

import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.ConverterOutput;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpConverter;
import org.apache.jasper.compiler.JtpCustomTag;

import java.util.ArrayDeque;
import java.util.Deque;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JstlFmtMessageConverter implements CustomTagConverter {

    @Override
    public void convert(JtpConverter converter, JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        var key = convertAttributeValue(tag.getAttribute("key"));
        var var = tag.getAttribute("var");

        if (var != null) {
            output.append("!{var ").append(var).append(" = ");
        } else if (!tag.hasParent(JtpCustomTag.byTagName("fmt:message"))) {
            output.append("${");
        }

        output.append("localize(").append(key);

        output.pushInsideScript(true);

        bodyConverter.convert();

        output.append(")");

        if (var != null) {
            output.append(";}");
        } else if (!tag.hasParent(JtpCustomTag.byTagName("fmt:message"))) {
            output.append("}");
        }

        output.popInsideScript();
    }

    @Override
    public boolean isTrimWhitespace() {
        return true;
    }
}
