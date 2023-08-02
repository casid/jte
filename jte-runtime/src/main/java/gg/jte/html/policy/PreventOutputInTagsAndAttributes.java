package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;

public class PreventOutputInTagsAndAttributes implements HtmlPolicy {

    private final boolean preventExpressionsInAttributes;

    public PreventOutputInTagsAndAttributes() {
        this(true);
    }

    public PreventOutputInTagsAndAttributes(boolean preventExpressionsInAttributes) {
        this.preventExpressionsInAttributes = preventExpressionsInAttributes;
    }

    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        if ( htmlTag.getName().contains("${") ) {
            throw new HtmlPolicyException("Illegal HTML tag name " + htmlTag.getName() + "! Expressions in HTML tag names are not allowed.");
        }
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if ( preventExpressionsInAttributes && htmlAttribute.getName().contains("${") ) {
            throw new HtmlPolicyException("Illegal HTML attribute name " + htmlAttribute.getName() + "! Expressions in HTML attribute names are not allowed.");
        }

        if (htmlAttribute.getName().contains("@if")) {
            throw new HtmlPolicyException("Illegal HTML attribute name " + htmlAttribute.getName() + "! @if expressions in HTML attribute names are not allowed. In case you're trying to optimize the generated output, smart attributes will do just that: https://github.com/casid/jte/blob/master/DOCUMENTATION.md#smart-attributes");
        }

        if (htmlAttribute.getName().contains("@for")) {
            throw new HtmlPolicyException("Illegal HTML attribute name " + htmlAttribute.getName() + "! @for loops in HTML attribute names are not allowed.");
        }

        if (htmlAttribute.getName().contains("@template.")) {
            throw new HtmlPolicyException("Illegal HTML attribute name " + htmlAttribute.getName() + "! Template calls in HTML attribute names are not allowed.");
        }

        if (htmlAttribute.getName().contains("@`")) {
            throw new HtmlPolicyException("Illegal HTML attribute name " + htmlAttribute.getName() + "! Content blocks in HTML attribute names are not allowed.");
        }
    }
}
