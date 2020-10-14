package gg.jte.html;

import gg.jte.html.policy.PolicyGroup;
import gg.jte.html.policy.PreventOutputInTagsAndAttributes;
import gg.jte.html.policy.PreventUnquotedAttributes;
import gg.jte.html.policy.PreventUppercaseTagsAndAttributes;

public class OwaspHtmlPolicy extends PolicyGroup {
    public OwaspHtmlPolicy() {
        addPolicy(new PreventUppercaseTagsAndAttributes());
        addPolicy(new PreventOutputInTagsAndAttributes());
        addPolicy(new PreventUnquotedAttributes());
    }
}
