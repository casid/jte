package org.jusecase.jte;

@SuppressWarnings("unused") // Methods are called by generated templates
public interface TemplateOutput {
    void writeContent(String value);

    default void writeStaticContent(String value, byte[] bytes) {
        writeContent(value);
    }

    default void writeUserContent(String value) {
        writeContent(value);
    }

    default void writeSafe(Object value) {
        if (value != null) {
            writeUserContent(value.toString());
        }
    }

    default void writeSafe(boolean value) {
        writeContent(String.valueOf(value));
    }

    default void writeSafe(byte value) {
        writeContent(String.valueOf(value));
    }

    default void writeSafe(short value) {
        writeContent(String.valueOf(value));
    }

    default void writeSafe(int value) {
        writeContent(String.valueOf(value));
    }

    default void writeSafe(long value) {
        writeContent(String.valueOf(value));
    }

    default void writeSafe(float value) {
        writeContent(String.valueOf(value));
    }

    default void writeSafe(double value) {
        writeContent(String.valueOf(value));
    }

    default void writeUnsafe(Object value) {
        if (value != null) {
            writeContent(value.toString());
        }
    }

    default void writeUnsafe(boolean value) {
        writeContent(String.valueOf(value));
    }

    default void writeUnsafe(byte value) {
        writeContent(String.valueOf(value));
    }

    default void writeUnsafe(short value) {
        writeContent(String.valueOf(value));
    }

    default void writeUnsafe(int value) {
        writeContent(String.valueOf(value));
    }

    default void writeUnsafe(long value) {
        writeContent(String.valueOf(value));
    }

    default void writeUnsafe(float value) {
        writeContent(String.valueOf(value));
    }

    default void writeUnsafe(double value) {
        writeContent(String.valueOf(value));
    }
}
