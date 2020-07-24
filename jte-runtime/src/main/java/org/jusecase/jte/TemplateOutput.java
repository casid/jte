package org.jusecase.jte;

import java.io.Writer;

@SuppressWarnings("unused") // Methods are called by generated templates
public interface TemplateOutput {
    Writer getWriter();

    void writeContent(String value);

    default void writeUserContent(String value) {
        if (value != null) {
            writeContent(value);
        }
    }

    default void writeUserContent(Enum<?> value) {
        if (value != null) {
            writeContent(value.toString());
        }
    }

    default void writeUserContent(Content content) {
        if (content != null) {
            content.writeTo(this);
        }
    }

    default void writeUserContent(boolean value) {
        writeContent(String.valueOf(value));
    }

    default void writeUserContent(byte value) {
        writeContent(String.valueOf(value));
    }

    default void writeUserContent(short value) {
        writeContent(String.valueOf(value));
    }

    default void writeUserContent(int value) {
        writeContent(String.valueOf(value));
    }

    default void writeUserContent(long value) {
        writeContent(String.valueOf(value));
    }

    default void writeUserContent(float value) {
        writeContent(String.valueOf(value));
    }

    default void writeUserContent(double value) {
        writeContent(String.valueOf(value));
    }

    default void writeUserContent(char value) {
        writeUserContent(String.valueOf(value));
    }
}
