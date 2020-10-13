package gg.jte.html.support;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CssClassesTest {
    CssClasses classes;

    @Test
    void empty() {
        addClassIf(false, "foo");
        thenOutputIs("");
    }

    @Test
    void one() {
        addClassIf(true, "bold");
        thenOutputIs("bold");
    }

    @Test
    void two() {
        addClassIf(true, "bold").addClassIf(true, "ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneNull() {
        addClassIf(true, "bold").addClassIf(true, null).addClassIf(true, "ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneEmpty() {
        addClassIf(true, "bold").addClassIf(true, "").addClassIf(true, "ml-3");
        thenOutputIs("bold ml-3");
    }

    @Test
    void three_oneBlank() {
        addClassIf(true, "bold").addClassIf(true, " ").addClassIf(true, "ml-3");
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
        addClass("first").addClassIf(false, "bold").addClassIf(true, "mb-3");
        thenOutputIs("first mb-3");
    }

    private CssClasses addClass(String cssClass) {
        classes = HtmlSupport.addClass(cssClass);
        return classes;
    }

    private CssClasses addClassIf(boolean condition, String cssClass) {
        classes = HtmlSupport.addClassIf(condition, cssClass);
        return classes;
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput(1024);
        classes.writeTo(output);
        assertThat(output.toString()).isEqualTo(expected);
    }
}