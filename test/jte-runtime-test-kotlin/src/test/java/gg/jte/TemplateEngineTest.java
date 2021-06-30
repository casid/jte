package gg.jte;

import gg.jte.output.StringOutput;
import gg.jte.runtime.TemplateUtils;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.Model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * To run these tests, you first need to run the jte-maven-plugin (jte:precompile)
 */
public class TemplateEngineTest {

    private static TemplateEngine templateEngine;
    private static Path targetDirectory;

    StringOutput output = new StringOutput();
    Model model = new Model();

    @BeforeAll
    static void beforeAll() {
        targetDirectory = Paths.get("jte-classes");
        templateEngine = TemplateEngine.createPrecompiled(targetDirectory, ContentType.Html);
    }

    @BeforeEach
    void setUp() {
        model.hello = "Hello";
        model.x = 42;
    }

    @Test
    void helloWorld() {
        whenTemplateIsRendered("helloWorld.kte");
        thenOutputIs("Hello World");
    }

    @Test
    void templateNotFound() {
        thenRenderingFailsWithException("unknown.kte").hasMessage("Failed to load unknown.kte");
    }

    @Test
    void sourceFilesAreDeleted() {
        Path precompiledResult = targetDirectory.resolve("gg").resolve("jte").resolve("generated").resolve("precompiled");
        assertThat(precompiledResult).isDirectoryContaining (p -> p.toString().endsWith(".class"));
        assertThat(precompiledResult).isDirectoryNotContaining (p -> p.toString().endsWith(".kt"));
    }

    @Test
    void exceptionLineNumber1() {
        thenRenderingFailsWithException("exceptionLineNumber1.kte")
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render exceptionLineNumber1.kte, error at exceptionLineNumber1.kte:5");
    }

    @Test
    void unusedTag() {
        templateEngine.renderTag("tag/unused.kte", TemplateUtils.toMap("param1", "One", "param2", "Two"), output);
        thenOutputIs("One is One, two is Two.");
    }

    @Test
    void params() {
        Map<String, Class<?>> params = templateEngine.getParamInfo("tag/unused.kte");
        assertThat(params).hasSize(2);
        assertThat(params).containsEntry("param1", String.class);
        assertThat(params).containsEntry("param2", String.class);
    }

    @Test
    void onDemandIsNotWorking() {
        Throwable throwable = catchThrowable(() ->
            TemplateEngine.create(new CodeResolver() {
                @Override
                public String resolve(String name) {
                    return "hello";
                }

                @Override
                public boolean hasChanged(String name) {
                    return false;
                }
            }, ContentType.Plain)
        );

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("TemplateCompiler could not be located. Maybe jte isn't on your classpath?");
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
