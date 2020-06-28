package org.jusecase.jte.internal;

import org.jusecase.jte.TemplateOutput;

public interface Template<Model> {
    void render(TemplateOutput output, Model model);
}
