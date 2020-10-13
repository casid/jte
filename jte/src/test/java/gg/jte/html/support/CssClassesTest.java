package gg.jte.html.support;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CssClassesTest {
    CssClasses classes;

    @Test
    void empty() {
        addClass(false, "foo");
        thenOutputIs("");
    }

    @Test
    void one() {
        addClass(true, "bold");
        thenOutputIs("bold");
    }

    @Test
    void two() {
        addClass(true, "bold").addClass(true, "ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneNull() {
        addClass(true, "bold").addClass(true, null).addClass(true, "ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneEmpty() {
        addClass(true, "bold").addClass(true, "").addClass(true, "ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneBlank() {
        addClass(true, "bold").addClass(true, " ").addClass(true, "ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void addClass() {
        addClass("two");
        thenOutputIs("two");
    }

    @Test
    void addClass_null() {
        addClass(null);
        thenOutputIs("");
    }

    @Test
    void condition() {
        addClass("first").addClass(false, "bold").addClass(true, "mb-3");
        thenOutputIs("first mb-3");
    }

    private CssClasses addClass(String cssClass) {
        classes = HtmlSupport.addClass(cssClass);
        return classes;
    }

    private CssClasses addClass(boolean condition, String cssClass) {
        classes = HtmlSupport.addClass(condition, cssClass);
        return classes;
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput(1024);
        classes.writeTo(output);
        assertThat(output.toString()).isEqualTo(expected);
    }
}