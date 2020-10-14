package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;

public class PreventOutputInTagsAndAttributes implements HtmlPolicy {

    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        if ( htmlTag.getName().contains("${") ) {
            throw new HtmlPolicyException("Illegal HTML tag name " + htmlTag.getName() + "! Expressions in HTML tag names are not allowed.");
        }
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if ( htmlAttribute.getName().contains("${") ) {
            throw new HtmlPolicyException("Illegal HTML attribute name " + htmlAttribute.getName() + "! Expressions in HTML attribute names are not allowed.");
        }
        if (htmlAttribute.getName().contains("@")) {
            throw new HtmlPolicyException("Illegal HTML attribute name " + htmlAttribute.getName() + "! Expressions in HTML attribute names are not allowed.");
        }
    }
}
