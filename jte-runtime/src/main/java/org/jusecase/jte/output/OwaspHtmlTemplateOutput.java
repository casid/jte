package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;
import org.owasp.encoder.Encode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

public class OwaspHtmlTemplateOutput implements HtmlTemplateOutput {
    private final TemplateOutput templateOutput;

    public OwaspHtmlTemplateOutput(TemplateOutput templateOutput) {
        this.templateOutput = templateOutput;
    }

    @Override
    public void writeTagBodyUserContent(String value, String tagName) {
        try {
            if ("script".equals(tagName)) {
                Encode.forJavaScriptBlock(getWriter(), value);
            } else {
                Encode.forHtml(getWriter(), value);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeTagAttributeUserContent(String value, String tagName, String attributeName) {
        try {
            Encode.forHtmlAttribute(getWriter(), value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Writer getWriter() {
        return templateOutput.getWriter();
    }

    @Override
    public void writeContent(String value) {
        templateOutput.writeContent(value);
    }
}
