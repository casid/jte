package gg.jte.models.generator;

import gg.jte.TemplateOutput;

public class SquashBlanksOutput implements TemplateOutput {
    private final TemplateOutput delegate;

    public SquashBlanksOutput(TemplateOutput delegate) {
        this.delegate = delegate;
    }

    @Override
    public void writeContent(String value) {
        if (value.contains("\n") && value.isBlank()) {
            return;
        }
        delegate.writeContent(value);
    }

    @Override
    public void writeContent(String value, int beginIndex, int endIndex) {
        writeContent(value.substring(beginIndex, endIndex));
    }
}
