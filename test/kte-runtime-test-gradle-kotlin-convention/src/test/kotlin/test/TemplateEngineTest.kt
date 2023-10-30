package test

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.TemplateException
import gg.jte.output.StringOutput
import gg.jte.runtime.TemplateUtils
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.NullPointerException
import java.nio.file.Path

/**
 * To run these tests, you first need to run the jte-gradle-plugin (jte:precompile)
 */
class TemplateEngineTest {

    companion object {
        val templateEngine = TemplateEngine.createPrecompiled(Path.of("jte-classes"), ContentType.Html)
    }

    val output = StringOutput()
    val model = Model()

    @BeforeEach
    internal fun setUp() {
        model.hello = "Hello"
    }

    @Test
    internal fun helloWorld() {
        whenTemplateIsRendered("helloWorld.kte")
        thenOutputIs("Hello World")
    }

    @Test
    internal fun helloInlineMethod() {
        whenTemplateIsRendered("helloInlineMethod.kte") // Inline method requires JDK-17
        thenOutputIs("inline World")
    }

    @Test
    internal fun helloValueClass() {
        val exception:TemplateException = assertThrows { templateEngine.render("helloValueClass.kte", ValueClass("Hello valued"), output) }
        assertEquals("Failed to render helloValueClass.kte, type mismatch for parameter: Expected java.lang.String, got test.ValueClass\n" +
                "It looks like you're rendering a template with a Kotlin value class parameter. To make this work, pass the value class parameter in a map.\n" +
                "Example: templateEngine.render(\"helloValueClass.kte\", mapOf(\"myValue\" to MyValueClass(), output)", exception.message)
    }

    @Test
    internal fun helloValueClass_map() {
        templateEngine.render("helloValueClass.kte", mapOf("model" to ValueClass("Hello valued")), output)
        thenOutputIs("Hello valued World")
    }

    @Test
    internal fun helloValueClassTemplateCall_map() {
        templateEngine.render("helloValueClassTemplateCall.kte", mapOf("model" to ValueClass("Hello valued")), output)
        thenOutputIs("Calling template.. you passed Hello valued")
    }

    @Test
    internal fun helloDataClass() {
        templateEngine.render("helloDataClass.kte", DataClass(42, "data"), output)
        thenOutputIs("Hello data, your id is 42.")
    }

    @Test
    internal fun helloDataClass_map() {
        templateEngine.render("helloDataClass.kte", mapOf("model" to DataClass(42, "data")), output)
        thenOutputIs("Hello data, your id is 42.")
    }

    @Test
    internal fun templateNotFound() {
        val exception = thenRenderingFailsWithException("unknown.kte")
        assertEquals("Failed to load unknown.kte", exception.message)
    }

    @Test
    internal fun exceptionLineNumber1() {
        val exception = thenRenderingFailsWithException("exceptionLineNumber1.kte")
        assertTrue(exception.cause is NullPointerException)
        assertEquals("Failed to render exceptionLineNumber1.kte, error at exceptionLineNumber1.kte:5", exception.message)
    }

    @Test
    internal fun unusedTag() {
        templateEngine.render("tag/unused.kte", TemplateUtils.toMap("param1", "One", "param2", "Two"), output)
        thenOutputIs("One is One, two is Two.")
    }

    @Test
    internal fun params() {
        val paramInfo = templateEngine.getParamInfo("tag/unused.kte")
        assertEquals(2, paramInfo.size)
    }

    @Test
    internal fun paramWithWrongType() {
        val exception = assertThrows<TemplateException> { templateEngine.render("helloWorld.kte", TemplateUtils.toMap("model", "string"), output) }
        assertTrue(exception.message!!.contains("Failed to render helloWorld.kte, error at helloWorld.kte:1"))
    }

    private fun whenTemplateIsRendered(templateName: String) {
        templateEngine.render(templateName, model, output)
    }

    private fun thenRenderingFailsWithException(templateName: String): TemplateException {
        return assertThrows { whenTemplateIsRendered(templateName) }
    }

    private fun thenOutputIs(expected: String) {
        assertEquals(expected, output.toString())
    }
}