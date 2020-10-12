package gg.jte.html;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CssClassesTest {
    CssClasses classes;

    @Test
    void empty() {
        classes();
        thenOutputIs("");
    }

    @Test
    void one() {
        classes().add("bold");
        thenOutputIs("bold");
    }

    @Test
    void two() {
        classes().add("bold").add("ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneNull() {
        classes().add("bold").add(null).add("ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneEmpty() {
        classes().add("bold").add("").add("ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneBlank() {
        classes().add("bold").add(" ").add("ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void condition() {
        classes().addIf(false, "bold").addIf(true, "mb-3");
        thenOutputIs("mb-3");
    }

    private CssClasses classes() {
        classes = Css.classes();
        return classes;
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput(1024);
        classes.writeTo(output);
        assertThat(output.toString()).isEqualTo(expected);
    }
}