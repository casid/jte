package gg.jte.html.support;

import gg.jte.Content;
import gg.jte.TemplateOutput;

import java.util.ArrayList;
import java.util.List;

public final class CssClasses implements Content {

    private List<String> cssClasses;

    public CssClasses addClass(boolean condition, String cssClass) {
        if (condition) {
            addClass(cssClass);
        }
        return this;
    }

    public CssClasses addClass(String cssClass) {
        if (cssClass != null && !cssClass.isBlank()) {
            if (cssClasses == null) {
                cssClasses = new ArrayList<>();
            }
            cssClasses.add(cssClass);
        }
        return this;
    }

    @Override
    public void writeTo(TemplateOutput output) {
        if (cssClasses == null) {
            return;
        }

        boolean first = true;
        for (String cssClass : cssClasses) {
            if (first) {
                first = false;
            } else {
                output.writeContent(" ");
            }
            output.writeUserContent(cssClass);
        }
    }

    @Override
    public boolean isEmpty() {
        return cssClasses == null;
    }
}
