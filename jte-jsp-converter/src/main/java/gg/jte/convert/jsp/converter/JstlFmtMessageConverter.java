package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.jsp.BodyConverter;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.JtpCustomTag;

import java.util.ArrayDeque;
import java.util.Deque;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JstlFmtMessageConverter implements CustomTagConverter {

    private final Deque<Boolean> previousInsideScript = new ArrayDeque<>();

    @Override
    public void convert(JtpCustomTag tag, ConverterOutput output, BodyConverter bodyConverter) throws JasperException {
        var key = convertAttributeValue(tag.getAttribute("key"));
        var var = tag.getAttribute("var");

        if (var != null) {
            output.append("!{var ").append(var).append(" = ");
        } else if (!tag.hasParent(JtpCustomTag.byTagName("fmt:message"))) {
            output.append("${");
        }

        output.append("localize(").append(key);

        previousInsideScript.push(output.isInsideScript());
        output.setInsideScript(true);

        bodyConverter.convert();

        output.append(")");

        if (var != null) {
            output.append(";}");
        } else if (!tag.hasParent(JtpCustomTag.byTagName("fmt:message"))) {
            output.append("}");
        }

        output.setInsideScript(previousInsideScript.pop());
    }

    @Override
    public boolean isTrimWhitespace() {
        return true;
    }
}
