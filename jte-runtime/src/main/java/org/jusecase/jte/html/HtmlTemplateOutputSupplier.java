package org.jusecase.jte.html;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.output.TemplateOutputSupplier;

public interface HtmlTemplateOutputSupplier extends TemplateOutputSupplier {
    void writeContent(HtmlTemplateOutput output);

    void writeTagBodyUserContent(HtmlTemplateOutput output, String tagName);

    void writeTagAttributeUserContent(HtmlTemplateOutput output, String tagName, String attributeName);

    @Override
    default void writeContent(TemplateOutput output) {
        writeContent((HtmlTemplateOutput)output);
    }
}
