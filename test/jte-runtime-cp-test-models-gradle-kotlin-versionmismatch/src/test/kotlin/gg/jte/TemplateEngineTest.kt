package gg.jte

import gg.jte.generated.precompiled.Templates
import gg.jte.generated.precompiled.DynamicTemplates
import gg.jte.generated.precompiled.StaticTemplates

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert

import java.util.stream.Stream
import java.util.function.Predicate

/**
 * To run these tests, you first need to run the gradle precompile task:
 */
class TemplateEngineTest {
    val model: test.Model = test.Model()

    @ParameterizedTest
    @MethodSource("templates")
    fun helloWorld(templates: Templates) {
        val output: String = templates.helloWorld(model).render()
        Assertions.assertThat(output).isEqualTo("Hello World")
    }

    @ParameterizedTest
    @MethodSource("templates")
    fun exceptionLineNumber1(templates: Templates) {
        val cause: Throwable =
            Assertions.catchThrowable(ThrowableAssert.ThrowingCallable {
                templates.exceptionLineNumber1(
                    model
                ).render()
            })
        Assertions.assertThat<Throwable>(cause).isInstanceOf(TemplateException::class.java)
            .hasCauseInstanceOf(NullPointerException::class.java)
            .hasMessage("Failed to render exceptionLineNumber1.kte, error at exceptionLineNumber1.kte:3")
    }

    @ParameterizedTest
    @MethodSource("templates")
    fun unusedTag(templates: Templates) {
        val output: String = templates.tagUnused("One", "Two").render()
        Assertions.assertThat(output).isEqualTo("One is One, two is Two.")
    }

    @ParameterizedTest
    @MethodSource("templates")
    fun normalContent(templates: Templates) {
        val output: String = templates.main().render()
        Assertions.assertThat(output).containsIgnoringWhitespaces("Header", "Main", "Footer")
    }

    @ParameterizedTest
    @MethodSource("templates")
    fun excludedTemplates(templates: Templates) {
        Assertions.assertThat(templates::class.java.getMethods()).noneMatch { m -> m.getName().contains("Exclude") }
    }

    @ParameterizedTest
    @MethodSource("templates")
    fun nestedContent(templates: Templates) {
        val output: String = templates.layout(templates.helloWorld(model)).render()
        Assertions.assertThat(output)
            .containsIgnoringWhitespaces("Header", "Hello World", "Footer")
    }

    @ParameterizedTest
    @MethodSource("templates")
    fun hasAnnotation(templates: Templates) {
        val clazz: Class<out Templates?> = templates::class.java
        val annotations: Array<Annotation> = clazz.getDeclaredAnnotations()
        Assertions.assertThat<Annotation>(annotations)
            .anyMatch(Predicate<Annotation> { a: Annotation? -> a is test.Dummy })
    }

    companion object {
        private val templateEngine: TemplateEngine = TemplateEngine.createPrecompiled(ContentType.Html)

        private val staticTemplates: Templates = StaticTemplates()
        private val dynamicTemplates: Templates = DynamicTemplates(templateEngine)
        @JvmStatic
        fun templates(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(staticTemplates),
                Arguments.of(dynamicTemplates)
            )
        }
    }
}