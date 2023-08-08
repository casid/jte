package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;

public class PreventInvalidAttributeNames implements HtmlPolicy {

    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if ( htmlAttribute.getName().contains(";") ) {
            throw new HtmlPolicyException("Invalid HTML attribute name " + htmlAttribute.getName() + "!");
        }
    }
}
