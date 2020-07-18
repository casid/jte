package org.jusecase.jte.output;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.TemplateOutput;

public abstract class AbstractTemplateOutputTest {
    TemplateOutput output = createTemplateOutput();

    abstract TemplateOutput createTemplateOutput();

    abstract void thenOutputIs(String expected);

    @Test
    void writeBoolean() {
        output.writeContent(true);
        output.writeContent(false);

        thenOutputIs("truefalse");
    }

    @Test
    void writeByte() {
        output.writeContent((byte) 50);
        output.writeContent((byte) 32);

        thenOutputIs("5032");
    }

    @Test
    void writeShort() {
        output.writeContent((short) 1250);
        output.writeContent((short) 320);

        thenOutputIs("1250320");
    }

    @Test
    void writeInt() {
        output.writeContent(1250);
        output.writeContent(-320);

        thenOutputIs("1250-320");
    }

    @Test
    void writeLong() {
        output.writeContent(1250L);
        output.writeContent(-320L);

        thenOutputIs("1250-320");
    }

    @Test
    void writeFloat() {
        output.writeContent(1250.0f);
        output.writeContent(-320.0f);

        thenOutputIs("1250.0-320.0");
    }

    @Test
    void writeDouble() {
        output.writeContent(1250.0);
        output.writeContent(-320.0);

        thenOutputIs("1250.0-320.0");
    }

    @Test
    void writeChar() {
        output.writeContent('a');
        output.writeContent('b');

        thenOutputIs("ab");
    }
}
