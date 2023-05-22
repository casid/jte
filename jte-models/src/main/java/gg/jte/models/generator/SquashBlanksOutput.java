package gg.jte.models.generator;

import gg.jte.TemplateOutput;

import java.io.Writer;

public class SquashBlanksOutput implements TemplateOutput {
    private final TemplateOutput delegate;

    public SquashBlanksOutput(TemplateOutput delegate) {
        this.delegate = delegate;
    }

    @Override
    public void writeContent(String value) {
        if (value.contains("\n") && value.trim().isEmpty()) {
            return;
        }
        delegate.writeContent(value);
    }

    @Override
    public void writeContent(String value, int beginIndex, int endIndex) {
        String substring = value.substring(beginIndex, endIndex);
        if (substring.contains("\n") && value.trim().isEmpty()) {
            return;
        }
        delegate.writeContent(value, beginIndex, endIndex);
    }
}
