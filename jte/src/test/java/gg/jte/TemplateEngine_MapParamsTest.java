package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateEngine_MapParamsTest {
    StringOutput output = new StringOutput();
    Map<String, Object> params = new HashMap<>();
    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver, ContentType.Plain);

    @Test
    void template() {
        givenTemplate("page.jte", """
                @param String firstParam
                @param int secondParam
                One: ${firstParam}, two: ${secondParam}""");

        params.put("firstParam", "Hello");
        params.put("secondParam", 42);

        whenTemplateIsRendered("page.jte");

        thenOutputIs("One: Hello, two: 42");
    }

    @Test
    void tag() {
        givenTag("card", """
                @param String firstParam
                @param int secondParam
                One: ${firstParam}, two: ${secondParam}""");

        params.put("firstParam", "Hello");
        params.put("secondParam", 42);

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: Hello, two: 42");
    }

    @Test
    void tag_defaultParamBoolean() {
        givenTag("card", """
                @param boolean firstParam = false
                @param Boolean secondParam = true
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: false, two: true");
    }

    @Test
    void tag_defaultParamInt() {
        givenTag("card", """
                @param int firstParam = 1
                @param Integer secondParam = 3
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    void tag_defaultParamLong() {
        givenTag("card", """
                @param long firstParam = 1L
                @param Long secondParam = 3L
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    void tag_defaultParamFloat() {
        givenTag("card", """
                @param float firstParam = 1.0f
                @param Float secondParam = 3.0f
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    void tag_defaultParamDouble() {
        givenTag("card", """
                @param double firstParam = 1.0
                @param Double secondParam = 3.0
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    void tag_defaultParamTypeNeedsCast() {
        givenTag("card", """
                @param byte firstParam = (byte)1
                @param Double secondParam = 3.0
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3.0");
    }

    @Test
    void tag_defaultParamIsExpression() {
        givenTag("card", """
                @param int firstParam = Integer.MIN_VALUE
                @param Double secondParam = 3.0
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: -2147483648, two: 3.0");
    }

    @Test
    void tag_defaultParamString() {
        givenTag("card", """
                @param java.lang.String firstParam = "test"
                @param int secondParam = 3
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: test, two: 3");
    }

    @Test
    void tag_defaultParamNull() {
        givenTag("card", """
                @param String firstParam = null
                @param int secondParam = 3
                One: ${firstParam}, two: ${secondParam}""");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: , two: 3");
    }

    @Test
    void layout_noParamsAndDefinitions() {
        givenLayout("page", "Hello !!");
        whenTemplateIsRendered("layout/page.jte");
        thenOutputIs("Hello !!");
    }

    @Test
    void layout_noParamsAndOneDefinition() {
        givenLayout("page", """
                @param gg.jte.Content content
                Hello ${content}!!\
                """);
        params.put("content", (Content) output -> output.writeContent("<p>world</p>"));

        whenTemplateIsRendered("layout/page.jte");

        thenOutputIs("Hello <p>world</p>!!");
    }

    @Test
    void layout_oneParamAndTwoDefinitions() {
        givenLayout("page", """
                @param String name
                @param gg.jte.Content content
                @param gg.jte.Content footer
                Hello ${name} ${content}, ${footer}""");
        params.put("name", "jte");
        params.put("content", (Content) output -> output.writeContent("<p>content</p>"));
        params.put("footer", (Content) output -> output.writeContent("<p>footer</p>"));

        whenTemplateIsRendered("layout/page.jte");

        thenOutputIs("Hello jte <p>content</p>, <p>footer</p>");
    }

    @SuppressWarnings("SameParameterValue")
    private void givenTemplate(String name, String code) {
        dummyCodeResolver.givenCode(name, code);
    }

    @SuppressWarnings("SameParameterValue")
    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name + ".jte", code);
    }

    @SuppressWarnings("SameParameterValue")
    private void givenLayout(String name, String code) {
        dummyCodeResolver.givenCode("layout/" + name + ".jte", code);
    }

    private void whenTemplateIsRendered(String name) {
        templateEngine.render(name, params, output);
    }

    private void thenOutputIs(String expected) {
        assertThat(output.toString()).isEqualTo(expected);
    }
}
