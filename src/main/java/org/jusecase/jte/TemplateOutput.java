package org.jusecase.jte;

public interface TemplateOutput {
    void writeSafeContent(String value);

    default void writeUnsafeContent(String value) {
        writeSafeContent(value);
    }

    default void write(Object value) {
        if (value == null) {
            writeSafeContent("null");
        } else {
            writeUnsafeContent(value.toString());
        }
    }

    default void write(boolean value) {
        writeSafeContent(String.valueOf(value));
    }

    default void write(byte value) {
        writeSafeContent(String.valueOf(value));
    }

    default void write(short value) {
        writeSafeContent(String.valueOf(value));
    }

    default void write(int value) {
        writeSafeContent(String.valueOf(value));
    }

    default void write(long value) {
        writeSafeContent(String.valueOf(value));
    }

    default void write(float value) {
        writeSafeContent(String.valueOf(value));
    }

    default void write(double value) {
        writeSafeContent(String.valueOf(value));
    }
}
