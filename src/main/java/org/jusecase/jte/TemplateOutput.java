package org.jusecase.jte;

public interface TemplateOutput {
    void write(Object value);

    default void write(boolean value) {
        write(String.valueOf(value));
    }

    default void write(byte value) {
        write(String.valueOf(value));
    }

    default void write(short value) {
        write(String.valueOf(value));
    }

    default void write(int value) {
        write(String.valueOf(value));
    }

    default void write(long value) {
        write(String.valueOf(value));
    }

    default void write(float value) {
        write(String.valueOf(value));
    }

    default void write(double value) {
        write(String.valueOf(value));
    }
}
