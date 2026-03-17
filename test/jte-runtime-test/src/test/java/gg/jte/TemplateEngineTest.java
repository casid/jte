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

import static org.assertj.core.api.Assertions.*;

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
        templateEngine = TemplateEngine.createPrecompiled(targetDirectory, ContentType.Html, null, "gg.jte.custom");
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
    void sourceFilesAreDeleted() {
        Path precompiledResult = targetDirectory.resolve("gg").resolve("jte").resolve("custom");
        assertThat(precompiledResult).isDirectoryContaining (p -> p.toString().endsWith(".class"));
        assertThat(precompiledResult).isDirectoryNotContaining (p -> p.toString().endsWith(".java"));
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
        Map<String, Class<?>> params = templateEngine.getParamInfo("tag/unused.jte");
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
                public long getLastModified(String name) {
                    return 0;
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
