package gg.jte.output;

import gg.jte.Content;
import gg.jte.ContentType;
import gg.jte.TemplateOutput;
import org.junit.jupiter.api.Test;

public abstract class AbstractTemplateOutputTest<T extends TemplateOutput> {
    T output = createTemplateOutput();

    abstract T createTemplateOutput();

    abstract void thenOutputIs(String expected);

    @Test
    void writeString() {
        output.writeContent("foo");
        output.writeContent("bar");

        thenOutputIs("foobar");
    }

    @Test
    void writeSubstring() {
        output.writeContent("foobar", 0, 3);
        output.writeContent("foobar", 3, 6);

        thenOutputIs("foobar");
    }

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

    @Test
    void writeEnum() {
        output.writeUserContent(ContentType.Html);
        thenOutputIs("Html");
    }

    @Test
    void writeEnum_nameIsUsed() {
        output.writeUserContent(EnumWithToStringOverride.Volvo);
        thenOutputIs("Volvo");
    }

    @Test
    void writeNull() {
        output.writeUserContent((String) null);
        output.writeUserContent((Content) null);
        output.writeUserContent((ContentType) null);
        output.writeUserContent((Boolean) null);
        output.writeUserContent((Number) null);
        output.writeUserContent((Character) null);
        thenOutputIs("");
    }

    public enum EnumWithToStringOverride {
        Volvo,
        Saab,
        Fiat,
        Audi,
        ;

        @Override
        public String toString() {
            return name() + " is a fine car!";
        }
    }
}
