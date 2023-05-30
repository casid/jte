package gg.jte.output;

import gg.jte.TemplateOutput;

public class StringOutput implements TemplateOutput {
    private final StringBuilder stringBuilder;

    public StringOutput() {
        this(8 * 1024);
    }

    public StringOutput(int capacity) {
        stringBuilder = new StringBuilder(capacity);
    }

    @Override
    public void writeContent(String value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeContent(String value, int beginIndex, int endIndex) {
        stringBuilder.append(value, beginIndex, endIndex);
    }

    @Override
    public void writeUserContent(boolean value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUserContent(byte value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUserContent(short value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUserContent(int value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUserContent(long value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUserContent(float value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUserContent(double value) {
        stringBuilder.append(value);
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    public void reset() {
        stringBuilder.setLength(0);
    }
}
