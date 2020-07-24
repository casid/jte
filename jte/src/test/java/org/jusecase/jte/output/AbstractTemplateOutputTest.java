package org.jusecase.jte.output;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.TemplateOutput;

public abstract class AbstractTemplateOutputTest {
    TemplateOutput output = createTemplateOutput();

    abstract TemplateOutput createTemplateOutput();

    abstract void thenOutputIs(String expected);

    @Test
    void writeBoolean() {
        output.writeUserContent(true);
        output.writeUserContent(false);

        thenOutputIs("truefalse");
    }

    @Test
    void writeByte() {
        output.writeUserContent((byte) 50);
        output.writeUserContent((byte) 32);

        thenOutputIs("5032");
    }

    @Test
    void writeShort() {
        output.writeUserContent((short) 1250);
        output.writeUserContent((short) 320);

        thenOutputIs("1250320");
    }

    @Test
    void writeInt() {
        output.writeUserContent(1250);
        output.writeUserContent(-320);

        thenOutputIs("1250-320");
    }

    @Test
    void writeLong() {
        output.writeUserContent(1250L);
        output.writeUserContent(-320L);

        thenOutputIs("1250-320");
    }

    @Test
    void writeFloat() {
        output.writeUserContent(1250.0f);
        output.writeUserContent(-320.0f);

        thenOutputIs("1250.0-320.0");
    }

    @Test
    void writeDouble() {
        output.writeUserContent(1250.0);
        output.writeUserContent(-320.0);

        thenOutputIs("1250.0-320.0");
    }

    @Test
    void writeChar() {
        output.writeUserContent('a');
        output.writeUserContent('b');

        thenOutputIs("ab");
    }
}
