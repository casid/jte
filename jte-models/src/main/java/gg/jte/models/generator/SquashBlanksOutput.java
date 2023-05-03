package gg.jte.models.generator;

import gg.jte.TemplateOutput;

import java.io.Writer;

public class SquashBlanksOutput implements TemplateOutput {
    private final TemplateOutput delegate;

    public SquashBlanksOutput(TemplateOutput delegate) {
        this.delegate = delegate;
    }

    @Override
    public Writer getWriter() {
        return delegate.getWriter();
    }

    @Override
    public void writeContent(String value) {
        if (value.contains("\n") && value.trim().isEmpty()) {
            return;
        }
        delegate.writeContent(value);
    }
}
