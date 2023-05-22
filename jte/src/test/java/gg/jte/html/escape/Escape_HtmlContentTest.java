package gg.jte.html.escape;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Escape_HtmlContentTest {

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
    void lt() {
        assertThat(escape("<")).isEqualTo("&lt;");
    }

    @Test
    void gt() {
        assertThat(escape(">")).isEqualTo("&gt;");
    }

    @Test
    void script() {
        assertThat(escape("<script>")).isEqualTo("&lt;script&gt;");
    }

    @Test
    void many() {
        assertThat(escape("<<<<&&&&>>>>:-)")).isEqualTo("&lt;&lt;&lt;&lt;&amp;&amp;&amp;&amp;&gt;&gt;&gt;&gt;:-)");
    }

    private String escape(String value) {
        StringOutput output = new StringOutput();
        Escape.htmlContent(value, output);
        return output.toString();
    }
}
