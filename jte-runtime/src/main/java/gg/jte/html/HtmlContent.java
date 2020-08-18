package gg.jte.html;

import gg.jte.TemplateOutput;
import gg.jte.Content;

public interface HtmlContent extends Content {
    void writeTo(HtmlTemplateOutput output);

    @Override
    default void writeTo(TemplateOutput output) {
        writeTo((HtmlTemplateOutput)output);
    }
}
