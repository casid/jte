package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;
import gg.jte.runtime.StringUtils;

public class PreventUppercaseTagsAndAttributes implements HtmlPolicy {

    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        if (StringUtils.isAllUpperCase(htmlTag.getName())) {
            throw new HtmlPolicyException("HTML tags are expected to be lowercase: " + htmlTag.getName());
        }
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        if (StringUtils.isAllUpperCase(htmlAttribute.getName())) {
            throw new HtmlPolicyException("HTML attributes are expected to be lowercase: " + htmlAttribute.getName());
        }
    }
}
