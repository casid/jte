package org.jusecase.jte.html;

import org.jusecase.jte.TemplateOutput;
import org.owasp.encoder.Encode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * See https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html
 */
public class OwaspHtmlTemplateOutput implements HtmlTemplateOutput {
    private final TemplateOutput templateOutput;

    public OwaspHtmlTemplateOutput(TemplateOutput templateOutput) {
        this.templateOutput = templateOutput;
    }

    @Override
    public void writeTagBodyUserContent(String value, String tagName) {
        if (value == null) {
            return;
        }

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
        if (value == null) {
            return;
        }

        if ("a".equals(tagName) && "href".equals(attributeName) && value.contains("javascript:")) {
            return;
        }

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
