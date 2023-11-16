package gg.jte.html.escape;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Notes from OWASP Java Encoder:
 * <br>
 * in <script> blocks, we need to prevent the browser from seeing
 * "</anything>" and "<!--". To do so we escape "/" as "\/" and
 * escape "-" as "\-".  Both could be solved with a hex encoding
 * on "<" but we figure "<" appears often in script strings and
 * the backslash encoding is more readable than a hex encoding.
 * (And note, a backslash encoding would not prevent the exploits
 * on "</...>" and "<!--".
 * In short "</script>" is escaped as "<\/script>" and "<!--" is
 * escaped as "<!\-\-".
 */
public class Escape_JavaScriptBlockTest {

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
        assertThat(escape("\"")).isEqualTo("\\\"");
    }

    @Test
    void singleQuote() {
        assertThat(escape("'")).isEqualTo("\\'");
    }

    @Test
    void slash() {
        assertThat(escape("/")).isEqualTo("\\/");
    }

    @Test
    void minus() {
        assertThat(escape("-")).isEqualTo("\\-");
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
        assertThat(escape("'''':-)")).isEqualTo("\\'\\'\\'\\':\\-)");
    }

    private String escape(String value) {
        StringOutput output = new StringOutput();
        Escape.javaScriptBlock(value, output);
        return output.toString();
    }
}
