package gg.jte;

import gg.jte.output.StringOutput;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.Model;

import java.nio.file.Paths;

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
        templateEngine = TemplateEngine.createPrecompiled(Paths.get("jte-classes"), ContentType.Plain); // That's not the content type we precompiled with.
    }

    @BeforeEach
    void setUp() {
        model.hello = "Hello";
    }

    @Test
    void wrongContentType() {
        thenRenderingFailsWithException("helloWorld.jte")
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasMessage("The template helloWorld.jte was compiled with ContentType.Html, but the template engine was initialized with ContentType.Plain. Please initialize the template engine with ContentType.Html.");
    }

    private void whenTemplateIsRendered(String templateName) {
        templateEngine.render(templateName, model, output);
    }

    @SuppressWarnings("SameParameterValue")
    private AbstractThrowableAssert<?, ? extends Throwable> thenRenderingFailsWithException(String templateName) {
        Throwable throwable = catchThrowable(() -> whenTemplateIsRendered(templateName));
        return assertThat(throwable).isInstanceOf(TemplateException.class);
    }
}
