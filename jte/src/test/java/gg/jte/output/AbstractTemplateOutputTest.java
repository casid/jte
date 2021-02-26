package gg.jte.output;

import gg.jte.TemplateOutput;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public abstract class AbstractTemplateOutputTest<T extends TemplateOutput> {
    T output = createTemplateOutput();

    abstract T createTemplateOutput();

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

    @Test
    void writer_charArray() throws IOException {
        char[] chars = "The quick brown fox...".toCharArray();
        output.getWriter().write(chars);

        thenOutputIs("The quick brown fox...");
    }

    @Test
    void writer_charArrayWithOffset() throws IOException {
        char[] chars = "The quick brown fox...".toCharArray();
        output.getWriter().write(chars, 4, 5);
        output.getWriter().write(chars, 16, 3);

        thenOutputIs("quickfox");
    }

    @Test
    void writer_char() throws IOException {
        output.getWriter().write('f');
        output.getWriter().write('o');
        output.getWriter().write('x');

        thenOutputIs("fox");
    }

    @Test
    void writer_string() throws IOException {
        output.getWriter().write("The quick brown fox...");
        thenOutputIs("The quick brown fox...");
    }

    @Test
    void writer_stringWithOffset() throws IOException {
        String s = "The quick brown fox...";
        output.getWriter().write(s, 4, 5);
        output.getWriter().write(s, 16, 3);

        thenOutputIs("quickfox");
    }

    @Test
    void writer_unusedMethodsAreNoops() throws IOException {
        output.getWriter().flush();
        output.getWriter().close();
    }
}
