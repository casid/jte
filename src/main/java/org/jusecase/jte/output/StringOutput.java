package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;

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
    public void writeSafe(boolean value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeSafe(byte value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeSafe(short value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeSafe(int value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeSafe(long value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeSafe(float value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeSafe(double value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeSafe(char value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(boolean value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(byte value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(short value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(int value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(long value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(float value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(double value) {
        stringBuilder.append(value);
    }

    @Override
    public void writeUnsafe(char value) {
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
