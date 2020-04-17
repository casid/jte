package org.jusecase.jte;

public class DummyTemplateOutput implements TemplateOutput {
    StringBuilder result = new StringBuilder();

    @Override
    public void write(Object value) {
        result.append(value);
    }

    @Override
    public void write(int value) {
        result.append(value);
    }

    public String toString() {
        return result.toString();
    }
}
