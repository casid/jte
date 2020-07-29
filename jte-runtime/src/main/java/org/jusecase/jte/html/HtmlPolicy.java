package org.jusecase.jte.html;

public interface HtmlPolicy {
    void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException;
    void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException;
}
