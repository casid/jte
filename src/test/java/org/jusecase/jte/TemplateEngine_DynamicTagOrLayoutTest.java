package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.internal.TemplateCompiler;
import org.jusecase.jte.output.StringOutput;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngine_DynamicTagOrLayoutTest {
    StringOutput output = new StringOutput();
    Map<String, Object> params = new HashMap<>();
    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver);

    @Test
    void tagRenderedWithoutTemplate() {
        givenTag("card", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");

        params.put("firstParam", "Hello");
        params.put("secondParam", 42);

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: Hello, two: 42");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamBoolean() {
        givenTag("card", "@param boolean firstParam = false\n" +
                "@param Boolean secondParam = true\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: false, two: true");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamInt() {
        givenTag("card", "@param int firstParam = 1\n" +
                "@param Integer secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamLong() {
        givenTag("card", "@param long firstParam = 1\n" +
                "@param Long secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1, two: 3");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamFloat() {
        givenTag("card", "@param float firstParam = 1.0f\n" +
                "@param Float secondParam = 3.0f\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamDouble() {
        givenTag("card", "@param double firstParam = 1.0\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: 1.0, two: 3.0");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamTypeNotSupported() {
        givenTag("card", "@param byte firstParam = 1\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        Throwable throwable = catchThrowable(() -> whenTagIsRendered("tag/card.jte"));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasRootCauseMessage("Unsupported default value '1' (byte) for dynamic tag invocation.");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamNotSupported() {
        givenTag("card", "@param int firstParam = Integer.MIN_VALUE\n" +
                "@param Double secondParam = 3.0\n" +
                "One: ${firstParam}, two: ${secondParam}");

        Throwable throwable = catchThrowable(() -> whenTagIsRendered("tag/card.jte"));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasRootCauseMessage("Unsupported default value 'Integer.MIN_VALUE' (int) for dynamic tag invocation.");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamString() {
        givenTag("card", "@param java.lang.String firstParam = \"test\"\n" +
                "@param int secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: test, two: 3");
    }

    @Test
    void tagRenderedWithoutTemplate_defaultParamNull() {
        givenTag("card", "@param String firstParam = null\n" +
                "@param int secondParam = 3\n" +
                "One: ${firstParam}, two: ${secondParam}");

        whenTagIsRendered("tag/card.jte");

        thenOutputIs("One: , two: 3");
    }

    @SuppressWarnings("SameParameterValue")
    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name + TemplateCompiler.TAG_EXTENSION, code);
    }

    @SuppressWarnings("SameParameterValue")
    private void whenTagIsRendered(String name) {
        templateEngine.render(name, params, output);
    }

    private void thenOutputIs(String expected) {
        assertThat(output.toString()).isEqualTo(expected);
    }
}
