package gg.jte.html;

import gg.jte.html.policy.*;

public class OwaspHtmlPolicy extends PolicyGroup {
    public OwaspHtmlPolicy() {
        addPolicy(new PreventUppercaseTagsAndAttributes());
        addPolicy(new PreventOutputInTagsAndAttributes());
        addPolicy(new PreventUnquotedAttributes());
        addPolicy(new PreventInvalidAttributeNames());
    }
}
