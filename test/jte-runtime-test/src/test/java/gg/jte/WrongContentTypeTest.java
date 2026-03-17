package gg.jte;

import gg.jte.output.StringOutput;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.Model;

import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * To run these tests, you first need to run the jte-maven-plugin (jte:precompile)
 */
public class WrongContentTypeTest {

    private static TemplateEngine templateEngine;

    StringOutput output = new StringOutput();
    Model model = new Model();

    @BeforeAll
    static void beforeAll() {
        templateEngine = TemplateEngine.createPrecompiled(Paths.get("jte-classes"), ContentType.Plain, null, "gg.jte.custom"); // That's not the content type we precompiled with.
    }

    @BeforeEach
    void setUp() {
        model.hello = "Hello";
    }

    @AfterEach
    void cleanUp() {
        output.reset();
    }

    @Test
    void wrongContentTypeInRender() {
        thenRenderingFailsBecauseOfWrongContentType(() -> templateEngine.render("helloWorld.jte", model, output));
    }

    @Test
    void wrongContentTypeInRenderMap() {
        thenRenderingFailsBecauseOfWrongContentType(() -> templateEngine.render("helloWorld.jte", Map.of("model", model), output));
    }

    @Test
    void illegalArgumentExceptionIsPropagated() {
        thenRenderingFails(() -> templateEngine.render("illegalArgumentException.jte", Map.of("model", model), output))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Oops");
    }

    @SuppressWarnings("SameParameterValue")
    private void thenRenderingFailsBecauseOfWrongContentType(Runnable whenTemplateIsRendered) {
        thenRenderingFails(whenTemplateIsRendered)
                .isInstanceOf(TemplateException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessage("The template helloWorld.jte was compiled with ContentType.Html, but the template engine was initialized with ContentType.Plain. Please initialize the template engine with ContentType.Html.");
    }

    private AbstractThrowableAssert<?, Throwable> thenRenderingFails(Runnable whenTemplateIsRendered) {
        Throwable throwable = catchThrowable(whenTemplateIsRendered::run);
        return assertThat(throwable)
            .isNotNull();
    }
}
