package gg.jte.html;

import gg.jte.Content;
import gg.jte.TemplateOutput;
import gg.jte.html.escape.Escape;
import gg.jte.runtime.StringUtils;
import gg.jte.output.StringOutput;

/**
 * See <a href="https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html">OWASP Cross Site Prevention Cheat Sheet</a>
 */
public class OwaspHtmlTemplateOutput implements HtmlTemplateOutput {
    private final TemplateOutput templateOutput;

    private String tagName;
    private String attributeName;

    public OwaspHtmlTemplateOutput(TemplateOutput templateOutput) {
        this.templateOutput = templateOutput;
    }

    protected OutputForAttributeContent createOutputForAttributeContent() {
        return new OutputForAttributeContent();
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
            } else {
                writeTagBodyUserContent(value);
            }
        }
    }

    @Override
    public void writeUserContent(Content content) {
        if (content != null) {
            if (tagName != null && attributeName != null) {
                OutputForAttributeContent output = createOutputForAttributeContent();
                content.writeTo(output);

                writeTagAttributeUserContent(output.toString());
            } else {
                content.writeTo(this);
            }
        }
    }

    private void writeTagBodyUserContent(String value) {
        if ("script".equals(tagName)) {
            Escape.javaScriptBlock(value, templateOutput);
        } else {
            Escape.htmlContent(value, templateOutput);
        }
    }

    private void writeTagAttributeUserContent(String value) {
        if ("a".equals(tagName) && "href".equals(attributeName) && StringUtils.startsWithIgnoringCaseAndWhitespaces(value, "javascript:")) {
            return;
        }

        if (attributeName.startsWith("on")) {
            Escape.javaScriptAttribute(value, templateOutput);
        } else {
            Escape.htmlAttribute(value, templateOutput);
        }
    }

    @Override
    public void writeContent(String value) {
        templateOutput.writeContent(value);
    }

    @Override
    public void writeContent(String value, int beginIndex, int endIndex) {
        templateOutput.writeContent(value, beginIndex, endIndex);
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

    protected static class OutputForAttributeContent extends StringOutput implements HtmlTemplateOutput {

        public OutputForAttributeContent() {
            super(1024);
        }

        @Override
        public void setContext( String tagName, String attributeName ) {
            // ignored
        }
    }
}
