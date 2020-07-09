package org.jusecase.jte.internal;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.support.HtmlTagSupport;

public interface Template<Model> {
    void render(TemplateOutput output, HtmlTagSupport htmlTagSupport, Model model);
}
