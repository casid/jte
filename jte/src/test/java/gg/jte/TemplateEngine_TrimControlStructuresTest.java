package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for experimental mode {@link TemplateEngine#setTrimControlStructures(boolean)}
 * Still missing before actually useful:
 * TODO write tests to ensure {@link gg.jte.html.HtmlInterceptor} works in this mode
 * TODO add parameter for maven plugin
 */
@SuppressWarnings("SameParameterValue")
public class TemplateEngine_TrimControlStructuresTest {
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
    void ifStatementNestedDiv_tabs() {
        givenTemplate(
                "<div>\n" +
                "\t@if(true)\n" +
                "\t\t@if(true)\n" +
                "\t\t\tYay,\n" +
                "\t\t\tit's double true!\n" +
                "\t\t@endif\n" +
                "\t@endif\n" +
                "</div>\n");

        thenOutputIs(
                "<div>\n" +
                "\tYay,\n" +
                "\tit's double true!\n" +
                "</div>\n");
    }

    @Test
    void ifStatementNestedDiv_tabs_windowsLineEndings() {
        givenTemplate(
                "<div>\r\n" +
                "\t@if(true)\r\n" +
                "\t\t@if(true)\r\n" +
                "\t\t\tYay,\r\n" +
                "\t\t\tit's double true!\r\n" +
                "\t\t@endif\r\n" +
                "\t@endif\r\n" +
                "</div>\r\n");

        thenOutputIs(
                "<div>\r\n" +
                "\tYay,\r\n" +
                "\tit's double true!\r\n" +
                "</div>\r\n");
    }

    @Test
    void ifElseStatementNestedDiv() {
        givenTemplate(
                "<div>\n" +
                "    @if(true)\n" +
                "        @if(false)\n" +
                "            Yay,\n" +
                "            it's double true!\n" +
                "        @else\n" +
                "            <p>\n" +
                "                It's not ${true}, that's for sure\n" +
                "            </p>\n" +
                "        @endif\n" +
                "    @endif\n" +
                "</div>\n");

        thenOutputIs(
                "<div>\n" +
                "    <p>\n" +
                "        It's not true, that's for sure\n" +
                "    </p>\n" +
                "</div>\n");
    }

    @Test
    void ifElseIfStatementNestedDiv() {
        givenTemplate(
                "<div>\n" +
                "    @if(true)\n" +
                "        @if(false)\n" +
                "            Yay,\n" +
                "            it's double true!\n" +
                "        @elseif(true)\n" +
                "            <p>\n" +
                "                It's not ${true}, that's for sure\n" +
                "            </p>\n" +
                "        @endif\n" +
                "    @endif\n" +
                "</div>\n");

        thenOutputIs(
                "<div>\n" +
                "    <p>\n" +
                "        It's not true, that's for sure\n" +
                "    </p>\n" +
                "</div>\n");
    }

    @Test
    void forEachNestedDiv() {
        givenTemplate(
                "<div>\n" +
                "    @if(true)\n" +
                "        @for(int i = 0; i < 3; ++i)\n" +
                "            @if(false)\n" +
                "                Yay,\n" +
                "                it's double true!\n" +
                "            @elseif(true)\n" +
                "                <p>\n" +
                "                    It's not ${true}, that's for sure\n" +
                "                </p>\n" +
                "            @endif\n" +
                "        @endfor\n" +
                "    @endif\n" +
                "</div>\n");

        thenOutputIs(
                "<div>\n" +
                "    <p>\n" +
                "        It's not true, that's for sure\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        It's not true, that's for sure\n" +
                "    </p>\n" +
                "    <p>\n" +
                "        It's not true, that's for sure\n" +
                "    </p>\n" +
                "</div>\n");
    }

    @Test
    void comment() {
        givenTemplate("@param String hello = \"hello\"\n${hello}\n<%-- comment --%>\nworld!");
        thenOutputIs("hello\nworld!");
    }

    @Test
    void variable() {
        givenTemplate(
                "!{var x = 1;}\n" +
                "!{var y = 2;}\n" +
                "${x + y}\n" +
                "done..\n"
        );
        thenOutputIs("3\ndone..\n");
    }

    @Test
    void variable_unsafe() {
        givenTemplate(
                "!{var x = 1;}\n" +
                "!{var y = 2;}\n" +
                "$unsafe{x + y}\n" +
                "done..\n"
        );
        thenOutputIs("3\ndone..\n");
    }

    @Test
    void tag() {
        givenTag("my.jte", "hello..");
        givenTemplate(
                "@if(true)\n" +
                "    @tag.my()\n" +
                "@endif\n" +
                "Next line");
        thenOutputIs("hello..\nNext line");
    }

    @Test
    void layout() {
        givenLayout("my.jte",
                "@param gg.jte.Content data\n" +
                "@if(data != null)\n" +
                "    <div>\n" +
                "        ${data}\n" +
                "    </div>\n" +
                "@endif\n"
        );
        givenTemplate(
                "@if(true)\n" +
                "    @layout.my(@`Here is some data: ${42}`)\n" +
                "@endif\n" +
                "Next line");

        thenOutputIs(
                "<div>\n" +
                "    Here is some data: 42\n" +
                "</div>\n" +
                "\n" +
                "Next line");
    }

    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name, code);
    }

    private void givenTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void givenLayout(String name, String code) {
        dummyCodeResolver.givenCode("layout/" + name, code);
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(templateName, params, output);

        assertThat(output.toString()).isEqualTo(expected);
    }
}