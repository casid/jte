package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;

public class PreventUnquotedAttributes implements HtmlPolicy {

    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        // Unused
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if (!htmlAttribute.isEmpty() && htmlAttribute.getQuotes() != '\"' && htmlAttribute.getQuotes() != '\'') {
            throw new HtmlPolicyException("Unquoted HTML attribute values are not allowed: " + htmlAttribute.getName());
        }
    }
}
