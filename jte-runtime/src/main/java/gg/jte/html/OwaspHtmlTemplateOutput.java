package gg.jte.html;

import gg.jte.Content;
import gg.jte.TemplateOutput;
import gg.jte.runtime.StringUtils;
import gg.jte.output.StringOutput;
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

    /**
     * Override in case of subclassing.
     */
    protected OwaspHtmlTemplateOutput newInstance(TemplateOutput output) {
        return new OwaspHtmlTemplateOutput(output);
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

    @Override
    public void writeUserContent(Content content) {
        if (content != null) {
            if (tagName != null && attributeName != null) {
                StringOutput output = new StringOutput(1024);
                content.writeTo(newInstance(output));

                writeTagAttributeUserContent(output.toString());
            } else {
                content.writeTo(this);
            }
        }
    }

    private void writeTagBodyUserContent(String value) {
        try {
            if ("script".equals(tagName)) {
                Encode.forJavaScriptBlock(getWriter(), value);
            } else {
                Encode.forHtmlContent(getWriter(), value);
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

    @Override
    public void writeBinaryContent(byte[] value) {
        templateOutput.writeBinaryContent(value);
    }

    @Override
    public void writeUserContent(boolean value) {
        templateOutput.writeUserContent(value);
    }

    @Override
    public void writeUserContent(byte value) {
        templateOutput.writeUserContent(value);
    }

    @Override
    public void writeUserContent(short value) {
        templateOutput.writeUserContent(value);
    }

    @Override
    public void writeUserContent(int value) {
        templateOutput.writeUserContent(value);
    }

    @Override
    public void writeUserContent(long value) {
        templateOutput.writeUserContent(value);
    }

    @Override
    public void writeUserContent(float value) {
        templateOutput.writeUserContent(value);
    }

    @Override
    public void writeUserContent(double value) {
        templateOutput.writeUserContent(value);
    }
}
