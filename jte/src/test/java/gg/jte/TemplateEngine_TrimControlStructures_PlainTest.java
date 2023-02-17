package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for mode {@link TemplateEngine#setTrimControlStructures(boolean)}, with {@link ContentType#Plain}
 */
@SuppressWarnings("SameParameterValue")
public class TemplateEngine_TrimControlStructures_PlainTest {
    String templateName = "test/template.jte";

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver, ContentType.Plain);
    Map<String, Object> params = new HashMap<>();

    @BeforeEach
    void setUp() {
        templateEngine.setTrimControlStructures(true);
    }

    @Test
    void whitespacesAtTemplateBegin() {
        params.put("name", "klaus");
        givenTemplate("\n\n@import java.lang.String\n\n\n@param String name\nHello ${name}!");
        thenOutputIs("Hello klaus!");
    }

    @Test
    void ifStatement() {
        givenTemplate(
                "@if(true)\n" +
                "    Yay,\n" +
                "    it's true!\n" +
                "@endif\n");
        thenOutputIs("Yay,\nit's true!\n");
    }

    @Test
    void ifStatement_oneLineWithSpace() {
        givenTemplate("@if(true) @endifYay");
        thenOutputIs(" Yay");
    }

    @Test
    void ifStatement_varyingIndentations1() {
        givenTemplate(
                "@if(true)\n" +
                "    Yay,\n" +
                "  it's true!\n" +
                "@endif\n");
        thenOutputIs("Yay,\nit's true!\n");
    }

    @Test
    void ifStatement_varyingIndentations2() {
        givenTemplate(
                "@if(true)\n" +
                "  Yay,\n" +
                "    it's true!\n" +
                "@endif\n");
        thenOutputIs("Yay,\n  it's true!\n");
    }

    @Test
    void ifStatementNested() {
        givenTemplate(
                "@if(true)\n" +
                "    @if(true)\n" +
                "        Yay,\n" +
                "        it's double true!\n" +
                "    @endif\n" +
                "@endif\n");
        thenOutputIs("Yay,\nit's double true!\n");
    }

    @Test
    void ifStatementNestedDiv() {
        givenTemplate(
                "<div>\n" +
                "    @if(true)\n" +
                "        @if(true)\n" +
                "            Yay,\n" +
                "            it's double true!\n" +
                "        @endif\n" +
                "    @endif\n" +
                "</div>\n");

        thenOutputIs(
                "<div>\n" +
                "    Yay,\n" +
                "    it's double true!\n" +
                "</div>\n");
    }

    @Test
    void comment() {
        givenTemplate("@param String hello = \"hello\"\n${hello}\n<%-- comment --%>\nworld!");
        thenOutputIs("hello\nworld!");
    }

    @Test
    void raw() {
        givenTemplate("@if(true)\n" +
                    "  @raw\n" +
                    "    @template(foo, bar) => ${something}\n" +
                    "  @endraw\n" +
                    "@endif");
        thenOutputIs("@template(foo, bar) => ${something}\n");
    }

    @Test
    void raw_oneLine() {
        givenTemplate("@rawfoo@endraw");
        thenOutputIs("foo");
    }

    @Test
    void variable() {
        givenTemplate(
                "!{int x = 1;}\n" +
                "!{int y = 2;}\n" +
                "${x + y}\n" +
                "done..\n"
        );
        thenOutputIs("3\ndone..\n");
    }

    @Test
    void template() {
        givenTag("my.jte", "hello..");
        givenTemplate(
                "@if(true)\n" +
                "    @template.tag.my()\n" +
                "@endif\n" +
                "Next line");
        thenOutputIs("hello..\nNext line");
    }

    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name, code);
    }

    private void givenTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(templateName, params, output);

        assertThat(output.toString()).isEqualTo(expected);
    }
}