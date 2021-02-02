package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;

public class PreventSingleQuotedAttributes implements HtmlPolicy {
    
    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        // Unused
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if (!htmlAttribute.isEmpty() && htmlAttribute.getQuotes() != '\"') {
            throw new HtmlPolicyException("HTML attribute values must be double quoted: " + htmlAttribute.getName());
        }
    }
}
