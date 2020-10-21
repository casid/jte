package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;

public class PreventInlineEventHandlers implements HtmlPolicy {
    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        // Unused
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if (htmlAttribute.getName().startsWith("on")) {
            throw new HtmlPolicyException("Inline event handlers are not allowed: " + htmlAttribute.getName());
        }
    }
}
