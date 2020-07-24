package org.jusecase.jte.html;

import org.jusecase.jte.Content;
import org.jusecase.jte.TemplateOutput;

public interface HtmlContent extends Content {
    void writeTo(HtmlTemplateOutput output);

    @Override
    default void writeTo(TemplateOutput output) {
        writeTo((HtmlTemplateOutput)output);
    }
}
