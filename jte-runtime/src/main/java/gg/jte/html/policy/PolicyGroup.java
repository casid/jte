package gg.jte.html.policy;

import gg.jte.html.HtmlAttribute;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlPolicyException;
import gg.jte.html.HtmlTag;

import java.util.ArrayList;
import java.util.List;

public class PolicyGroup implements HtmlPolicy {
    private final List<HtmlPolicy> policies = new ArrayList<>();

    public void addPolicy(HtmlPolicy policy) {
        policies.add(policy);
    }

    @Override
    public void validateHtmlTag(HtmlTag htmlTag) throws HtmlPolicyException {
        for (HtmlPolicy policy : policies) {
            policy.validateHtmlTag(htmlTag);
        }
    }

    @Override
    public void validateHtmlAttribute(HtmlTag htmlTag, HtmlAttribute htmlAttribute) throws HtmlPolicyException {
        for (HtmlPolicy policy : policies) {
            policy.validateHtmlAttribute(htmlTag, htmlAttribute);
        }
    }
}
