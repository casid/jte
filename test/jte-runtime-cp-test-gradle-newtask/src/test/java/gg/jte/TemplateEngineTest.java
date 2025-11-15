package gg.jte;

import gg.jte.output.StringOutput;
import gg.jte.runtime.TemplateUtils;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * To run these tests, you first need to run the jte-maven-plugin (jte:precompile)
 */
public class TemplateEngineTest {

    private static TemplateEngine templateEngine;
    StringOutput output = new StringOutput();
    Model model = new Model();

    @BeforeAll
    static void beforeAll() {
        templateEngine = TemplateEngine.createPrecompiled(null, ContentType.Html, null, "gg.jte.additionalgeneratetask");
    }

    @BeforeEach
    void setUp() {
        model.hello = "Hello";
        model.x = 42;
    }

    @Test
    void helloWorld() {
        whenTemplateIsRendered("helloWorld.jte");
        thenOutputIs("Hello World");
    }

    @Test
    void templateNotFound() {
        thenRenderingFailsWithException("unknown.jte").hasMessage("Failed to load unknown.jte");
    }

    @Test
    void exceptionLineNumber1() {
        thenRenderingFailsWithException("exceptionLineNumber1.jte")
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render exceptionLineNumber1.jte, error at exceptionLineNumber1.jte:5");
    }

    @Test
    void unusedTag() {
        templateEngine.render("tag/unused.jte", TemplateUtils.toMap("param1", "One", "param2", "Two"), output);
        thenOutputIs("One is One, two is Two.");
    }

    @Test
    void params() {
        Throwable throwable = catchThrowable(() -> templateEngine.getParamInfo("tag/unused.jte"));
        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("No parameter information is available for tag/unused.jte, compile templates with -parameters flag, to use this method.");
    }

    @Test
    void paramWithWrongType() {
        Throwable throwable = catchThrowable(() -> templateEngine.render("helloWorld.jte", TemplateUtils.toMap("model", "string"), output));
        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessageContaining("Failed to render helloWorld.jte, error at helloWorld.jte:1");
    }

    private void whenTemplateIsRendered(String templateName) {
        templateEngine.render(templateName, model, output);
    }

    private AbstractThrowableAssert<?, ? extends Throwable> thenRenderingFailsWithException(String templateName) {
        Throwable throwable = catchThrowable(() -> whenTemplateIsRendered(templateName));
        return assertThat(throwable).isInstanceOf(TemplateException.class);
    }

    private void thenOutputIs(String expected) {
        assertThat(output.toString()).isEqualTo(expected);
    }
}
