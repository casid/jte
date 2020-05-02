package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;

public class StringOutput implements TemplateOutput {
    private final StringBuilder stringBuilder;

    public StringOutput() {
        this(1024);
    }

    public StringOutput(int capacity) {
        stringBuilder = new StringBuilder(capacity);
    }

    @Override
    public void write(Object value) {
        stringBuilder.append(value);
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
