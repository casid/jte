package gg.jte.convert.jsp.converter;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import org.apache.jasper.compiler.JtpCustomTag;

public class JspWhenConverter implements CustomTagConverter {

    private boolean isFirst(JtpCustomTag tag) {
        JtpCustomTag parent = tag.parent(JtpCustomTag.byTagName("c:choose"));

        return parent.indexOf(tag) == 0;
    }

    @Override
    public void before(JtpCustomTag tag, ConverterOutput output) {
        if (isFirst(tag)) {
            output.append("@if(");
        } else {
            output.append("@elseif(");
        }

        output.append(JspExpressionConverter.convertAttributeValue(tag.getAttribute("test"))).append(")");
    }

    @Override
    public void after(JtpCustomTag tag, ConverterOutput output) {
        // written in before or JspChooseConverter.after
    }
}
