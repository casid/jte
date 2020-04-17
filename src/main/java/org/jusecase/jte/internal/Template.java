package org.jusecase.jte.internal;

import org.jusecase.jte.TemplateOutput;

public interface Template<Model> {
    void render(Model model, TemplateOutput output);
}
