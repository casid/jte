package gg.jte.html.escape;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Escape_JavaScriptAttributeTest {

    @Test
    void empty() {
        assertThat(escape("")).isEqualTo("");
    }

    @Test
    void nothingToEscape() {
        assertThat(escape("script")).isEqualTo("script");
    }

    @Test
    void quote() {
        assertThat(escape("\"")).isEqualTo("\\x22");
    }

    @Test
    void singleQuote() {
        assertThat(escape("'")).isEqualTo("\\x27");
    }

    @Test
    void backslash() {
        assertThat(escape("\\")).isEqualTo("\\\\");
    }

    @Test
    void linebreak() {
        assertThat(escape("\n")).isEqualTo("\\n");
    }

    @Test
    void linebreak_windows() {
        assertThat(escape("\r")).isEqualTo("\\r");
    }

    @Test
    void tab() {
        assertThat(escape("\t")).isEqualTo("\\t");
    }

    @Test
    void formFeed() {
        assertThat(escape("\f")).isEqualTo("\\f");
    }

    @Test
    void backspace() {
        assertThat(escape("\b")).isEqualTo("\\b");
    }

    @Test
    void many() {
        assertThat(escape("'''':-)")).isEqualTo("\\x27\\x27\\x27\\x27:-)");
    }

    private String escape(String value) {
        StringOutput output = new StringOutput();
        Escape.javaScriptAttribute(value, output);
        return output.toString();
    }
}
