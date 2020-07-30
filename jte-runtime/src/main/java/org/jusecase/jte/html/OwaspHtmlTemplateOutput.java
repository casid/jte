package org.jusecase.jte.html;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.internal.StringUtils;
import org.owasp.encoder.Encode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * See https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html
 */
public class OwaspHtmlTemplateOutput implements HtmlTemplateOutput {
    private final TemplateOutput templateOutput;

    private String tagName;
    private String attributeName;

    public OwaspHtmlTemplateOutput(TemplateOutput templateOutput) {
        this.templateOutput = templateOutput;
    }

    @Override
    public void setContext(String tagName, String attributeName) {
        this.tagName = tagName;
        this.attributeName = attributeName;
    }

    @Override
    public void writeUserContent(String value) {
        if (value != null) {
            if (tagName != null && attributeName != null) {
                writeTagAttributeUserContent(value);
            } else if (tagName != null) {
                writeTagBodyUserContent(value);
            } else {
                writeContent(value);
            }
        }
    }

    private void writeTagBodyUserContent(String value) {
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

    private void writeTagAttributeUserContent(String value) {
        if ("a".equals(tagName) && "href".equals(attributeName) && StringUtils.startsWithIgnoringCaseAndWhitespaces(value, "javascript:")) {
            return;
        }

        try {
            if (attributeName.startsWith("on")) {
                Encode.forJavaScriptAttribute(getWriter(), value);
            } else {
                Encode.forHtmlAttribute(getWriter(), value);
            }
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
