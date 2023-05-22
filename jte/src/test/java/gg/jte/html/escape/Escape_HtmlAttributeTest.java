package gg.jte.html.escape;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Escape_HtmlAttributeTest {

    @Test
    void empty() {
        assertThat(escape("")).isEqualTo("");
    }

    @Test
    void nothingToEscape() {
        assertThat(escape("script")).isEqualTo("script");
    }

    @Test
    void amp() {
        assertThat(escape("&")).isEqualTo("&amp;");
    }

    @Test
    void quote() {
        assertThat(escape("\"")).isEqualTo("&#34;");
    }

    @Test
    void singleQuote() {
        assertThat(escape("'")).isEqualTo("&#39;");
    }

    @Test
    void many() {
        assertThat(escape("''''\"\"\"\":-)")).isEqualTo("&#39;&#39;&#39;&#39;&#34;&#34;&#34;&#34;:-)");
    }

    private String escape(String value) {
        StringOutput output = new StringOutput();
        Escape.htmlAttribute(value, output);
        return output.toString();
    }
}
