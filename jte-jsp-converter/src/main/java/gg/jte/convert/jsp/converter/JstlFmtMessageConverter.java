package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

import java.util.ArrayDeque;
import java.util.Deque;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JstlFmtMessageConverter implements CustomTagConverter {

    private final Deque<Boolean> previousInsideScript = new ArrayDeque<>();

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
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
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        var var = tag.getAttribute("var");

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
