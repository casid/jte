package org.jusecase.jte.output;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.TemplateOutput;

public abstract class AbstractTemplateOutputTest {
    TemplateOutput output = createTemplateOutput();

    abstract TemplateOutput createTemplateOutput();

    abstract void thenOutputIs(String expected);

    @Test
    void writeBoolean() {
        output.writeSafe(true);
        output.writeUnsafe(false);

        thenOutputIs("truefalse");
    }

    @Test
    void writeByte() {
        output.writeSafe((byte) 50);
        output.writeUnsafe((byte) 32);

        thenOutputIs("5032");
    }

    @Test
    void writeShort() {
        output.writeSafe((short) 1250);
        output.writeUnsafe((short) 320);

        thenOutputIs("1250320");
    }

    @Test
    void writeInt() {
        output.writeSafe(1250);
        output.writeUnsafe(-320);

        thenOutputIs("1250-320");
    }

    @Test
    void writeLong() {
        output.writeSafe(1250L);
        output.writeUnsafe(-320L);

        thenOutputIs("1250-320");
    }

    @Test
    void writeFloat() {
        output.writeSafe(1250.0f);
        output.writeUnsafe(-320.0f);

        thenOutputIs("1250.0-320.0");
    }

    @Test
    void writeDouble() {
        output.writeSafe(1250.0);
        output.writeUnsafe(-320.0);

        thenOutputIs("1250.0-320.0");
    }

    @Test
    void writeChar() {
        output.writeSafe('a');
        output.writeUnsafe('b');

        thenOutputIs("ab");
    }
}
