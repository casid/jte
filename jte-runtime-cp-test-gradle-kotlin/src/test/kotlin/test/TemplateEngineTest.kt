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

/**
 * To run these tests, you first need to run the jte-gradle-plugin (jte:precompile)
 */
class TemplateEngineTest {

    companion object {
        val templateEngine = TemplateEngine.createPrecompiled(ContentType.Html)
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
        templateEngine.renderTag("tag/unused.kte", TemplateUtils.toMap("param1", "One", "param2", "Two"), output)
        thenOutputIs("One is One, two is Two.")
    }

    @Test
    internal fun params() {
        val exception = assertThrows<TemplateException> { templateEngine.getParamInfo("tag/unused.kte") }
        assertEquals("No parameter information is available for tag/unused.kte, compile templates with -parameters flag, to use this method.", exception.message)
    }

    @Test
    internal fun paramWithWrongType() {
        val exception = assertThrows<TemplateException> { templateEngine.render("helloWorld.kte", TemplateUtils.toMap("model", "string"), output) }
        assertTrue(exception.message!!.contains("Failed to render helloWorld.kte, error at helloWorld.kte:0"))
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