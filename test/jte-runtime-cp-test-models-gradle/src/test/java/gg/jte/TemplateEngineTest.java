package gg.jte;

import gg.jte.generated.precompiled.DynamicHtmlTemplates;
import gg.jte.generated.precompiled.HtmlTemplates;
import gg.jte.generated.precompiled.StaticHtmlTemplates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import test.Model;
import test.Requires;
import test.Secondary;
import test.Singleton;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * To run these tests, you first need to run the jte-maven-plugin (jte:precompile)
 */
public class TemplateEngineTest {

    private static TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);

    private static final gg.jte.generated.precompiled.HtmlTemplates staticTemplates = new gg.jte.generated.precompiled.StaticHtmlTemplates();
    private static final gg.jte.generated.precompiled.HtmlTemplates dynamicTemplates = new gg.jte.generated.precompiled.DynamicHtmlTemplates(templateEngine);
    Model model = new Model();

    public static Stream<Arguments> templates() {
        return Stream.of(
                Arguments.of(staticTemplates),
                Arguments.of(dynamicTemplates)
        );
    }

    @BeforeEach
    void setUp() {
        model.hello = "Hello";
        model.x = 42;
    }

    @ParameterizedTest
    @MethodSource("templates")
    void helloWorld(gg.jte.generated.precompiled.HtmlTemplates templates) {
        String output = templates.helloWorld(model).render();
        assertThat(output).isEqualTo("Hello World");
    }

    @ParameterizedTest
    @MethodSource("templates")
    void exceptionLineNumber1(gg.jte.generated.precompiled.HtmlTemplates templates) {
        Throwable cause = catchThrowable(() -> templates.exceptionLineNumber1(model).render());
        assertThat(cause).isInstanceOf(TemplateException.class)
            .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render exceptionLineNumber1.jte, error at exceptionLineNumber1.jte:5");
    }

    @ParameterizedTest
    @MethodSource("templates")
    void unusedTag(gg.jte.generated.precompiled.HtmlTemplates templates) {
        String output = templates.tagUnused("One", "Two").render();
        assertThat(output).isEqualTo("One is One, two is Two.");
    }

    @ParameterizedTest
    @MethodSource("templates")
    void normalContent(HtmlTemplates templates) {
        String output = templates.main().render();
        assertThat(output).containsIgnoringWhitespaces("Header", "Main", "Footer");
    }

    @ParameterizedTest
    @MethodSource("templates")
    void excludedTemplates(gg.jte.generated.precompiled.HtmlTemplates templates){
        assertThat(templates.getClass().getMethods()).noneMatch(m -> m.getName().contains("Exclude"));
    }

    @ParameterizedTest
    @MethodSource("templates")
    void nestedContent(HtmlTemplates templates) {
        String output = templates.layout(templates.helloWorld(model)).render();
        assertThat(output).containsIgnoringWhitespaces("Header", "Hello World", "Footer");
    }

    @ParameterizedTest
    @MethodSource("templates")
    void hasAnnotation(HtmlTemplates templates) {
        Class<? extends HtmlTemplates> clazz = templates.getClass();
        Annotation[] annotations = clazz.getDeclaredAnnotations();
        assertThat(annotations).anyMatch(a -> a instanceof Singleton);
        if (templates instanceof StaticHtmlTemplates) {
            assertThat(annotations).anyMatch(a -> a instanceof Secondary);
        }
        if (templates instanceof DynamicHtmlTemplates) {
            assertThat(annotations).anyMatch(a -> a instanceof Requires);
        }
    }
}
