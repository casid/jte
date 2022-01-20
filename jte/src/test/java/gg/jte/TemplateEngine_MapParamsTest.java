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
        givenTemplate("page.jte", "@param String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");

        params.put("firstParam", "Hello");
        params.put("secondParam", 42);

        whenTemplateIsRendered("page.jte");

        thenOutputIs("One: Hello, two: 42");
    }

    @Test
    void tag() {
        givenTag("card", "@param String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");

        params.put("firstParam", "Hello");
        params.put("secondParam", 42);

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: Hello, two: 42");
    }

    @Test
    void tag_defaultParamBoolean() {
        givenTag("card", "@param boolean firstParam = false\n" +
                "@param Boolean secondParam = true\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: false, two: true");
    }

    @Test
    void tag_defaultParamInt() {
        givenTag("card", "@param int firstParam = 1\n" +
                "@param Integer secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    void tag_defaultParamLong() {
        givenTag("card", "@param long firstParam = 1L\n" +
                "@param Long secondParam = 3L\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    void tag_defaultParamFloat() {
        givenTag("card", "@param float firstParam = 1.0f\n" +
                "@param Float secondParam = 3.0f\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    void tag_defaultParamDouble() {
        givenTag("card", "@param double firstParam = 1.0\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    void tag_defaultParamTypeNeedsCast() {
        givenTag("card", "@param byte firstParam = (byte)1\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3.0");
    }

    @Test
    void tag_defaultParamIsExpression() {
        givenTag("card", "@param int firstParam = Integer.MIN_VALUE\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: -2147483648, two: 3.0");
    }

    @Test
    void tag_defaultParamString() {
        givenTag("card", "@param java.lang.String firstParam = \"test\"\n" +
                "@param int secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTemplateIsRendered("tag/card.jte");

        thenOutputIs("One: test, two: 3");
    }

    @Test
    void tag_defaultParamNull() {
        givenTag("card", "@param String firstParam = null\n" +
                "@param int secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

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
        givenLayout("page", "@param gg.jte.Content content\n" +
                "Hello ${content}!!");
        params.put("content", (Content) output -> output.writeContent("<p>world</p>"));

        whenTemplateIsRendered("layout/page.jte");

        thenOutputIs("Hello <p>world</p>!!");
    }

    @Test
    void layout_oneParamAndTwoDefinitions() {
        givenLayout("page", "@param String name\n" +
                        "@param gg.jte.Content content\n" +
                        "@param gg.jte.Content footer\n" +
                "Hello ${name} ${content}, ${footer}");
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
