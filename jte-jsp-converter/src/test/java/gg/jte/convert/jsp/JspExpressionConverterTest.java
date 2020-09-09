package gg.jte.convert.jsp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JspExpressionConverterTest {

    @Test
    void empty() {
        assertConversion("${empty foo}", "foo == null");
    }

    @Test
    void notEmpty() {
        assertConversion("${not empty foo}", "foo != null");
    }

    @Test
    void notEmptyOr() {
        assertConversion("${not empty foo or true}", "foo != null || true");
    }

    @Test
    void notEmptyAnd() {
        assertConversion("${not empty foo and false}", "foo != null && false");
    }

    @Test
    void parenthesis() {
        assertConversion("${(foo or bar) and (x == 1)}", "(foo || bar) && (x == 1)");
    }

    @Test
    void parenthesis2() {
        assertConversion("${foo or (bar and (x == 1))}", "foo || (bar && (x == 1))");
    }

    @Test
    void methodCall() {
        assertConversion("${page.getDescription(locale)}", "page.getDescription(locale)");
    }

    @Test
    void methodCall2() {
        assertConversion("${page.getDescription(a, b, c)}", "page.getDescription(a, b, c)");
    }

    @Test
    void methodCall3() {
        assertConversion("${page.doStuff()}", "page.doStuff()");
    }

    @Test
    void eq() {
        assertConversion("${data.quantity eq 42}", "data.quantity == 42");
    }

    @Test
    void ternary() {
        assertConversion("${data.quantity == 42 ? 'Yay' : 'Nay'}", "data.quantity == 42 ? \"Yay\" : \"Nay\"");
    }

    @Test
    void not() {
        assertConversion("${!true}", "!true");
    }

    @Test
    void eqEnum() {
        // TODO maybe we can use type information to make this better
        assertConversion("${data.quality eq 'Good'}", "data.quality == \"Good\"");
    }

    private void assertConversion(String el, String java) {
        assertThat(new JspExpressionConverter(el).getJavaCode()).isEqualTo(java);
    }
}