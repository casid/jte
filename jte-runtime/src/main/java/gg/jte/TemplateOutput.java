package gg.jte;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused") // Methods are called by generated templates
public interface TemplateOutput {

    void writeContent(String value);

    void writeContent(String value, int beginIndex, int endIndex);

    default void writeBinaryContent(byte[] value) {
        writeContent(new String(value, StandardCharsets.UTF_8));
    }

    default void writeUserContent(String value) {
        if (value != null) {
            writeContent(value);
        }
    }

    default void writeUserContent(Enum<?> value) {
        if (value != null) {
            writeContent(value.name());
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

    default void writeUserContent(Boolean value) {
        if (value != null) {
            writeUserContent(value.booleanValue());
        }
    }

    default void writeUserContent(Number value) {
        if (value != null) {
            writeUserContent(value.toString());
        }
    }

    default void writeUserContent(Character value) {
        if (value != null) {
            writeUserContent(value.charValue());
        }
    }

    default void writeUnsafeContent(String value) {
        if (value != null) {
            writeContent(value);
        }
    }
}
