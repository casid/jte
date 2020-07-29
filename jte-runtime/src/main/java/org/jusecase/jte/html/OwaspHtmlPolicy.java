package org.jusecase.jte.html;

public class OwaspHtmlPolicy implements HtmlPolicy {
    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        if ( htmlTag.getName().contains("${") ) {
            throw new HtmlPolicyException("Illegal tag name " + htmlTag.getName() + "! Expressions in tag names are not allowed.");
        }
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if ( htmlAttribute.getName().contains("${") ) {
            throw new HtmlPolicyException("Illegal attribute name " + htmlAttribute.getName() + "! Expressions in attribute names are not allowed.");
        }
        if (!htmlAttribute.isBoolean() && htmlAttribute.getQuotes() != '\"' && htmlAttribute.getQuotes() != '\'') {
            throw new HtmlPolicyException("Unquoted attribute values are not allowed.");
        }
    }
}
