package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for experimental mode {@link TemplateEngine#setTrimControlStructures(boolean)}
 */
@SuppressWarnings("SameParameterValue")
public class TemplateEngine_TrimControlStructuresTest {
    String templateName = "test/template.jte";

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver, ContentType.Html);
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
    void whitespacesAtTemplateBegin_variable() {
        givenTemplate(
                "@import java.time.Instant\n" +
                "@import java.time.temporal.ChronoUnit\n" +
                "@param String name\n" +
                "!{\n" +
                "    String ts = \"ts\";\n" +
                "    String message = \"Hello \" + name;\n" +
                "}\n" +
                "<!DOCTYPE cXML SYSTEM \"http://example.com/example.dtd\">\n" +
                "<Issue xml:lang=\"en-US\" timestamp=\"${ts}\" someID=\"${name}@internal.example.com\">\n" +
                "  <Response><Status code=\"foo\" text=\"bar\">${message}</Status></Response>\n" +
                "</Issue>");

        thenOutputIs("<!DOCTYPE cXML SYSTEM \"http://example.com/example.dtd\">\n" +
                "<Issue xml:lang=\"en-US\" timestamp=\"ts\" someID=\"@internal.example.com\">\n" +
                "  <Response><Status code=\"foo\" text=\"bar\">Hello null</Status></Response>\n" +
                "</Issue>");
    }

    @Test
    void attributes() {
        givenTemplate("@if(true)\n" +
                "    <a href=\"${\"url\"}\" class=\"nav-item@if(true) is-active@endif\">foo</a>\n" +
                "@endif");
        thenOutputIs("<a href=\"url\" class=\"nav-item is-active\">foo</a>\n");
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
    void formWithLoop() {
        givenTemplate("@param gg.jte.TemplateEngine_HtmlInterceptorTest.Controller controller\n" +
                "<body>\n" +
                "   <h1>Hello</h1>\n" +
                "\n" +
                "   <form action=\"${controller.getUrl()}\" method=\"POST\">\n" +
                "\n" +
                "      <label>\n" +
                "         Food option:\n" +
                "         <select name=\"foodOption\">\n" +
                "            <option value=\"\">-</option>\n" +
                "            @for(String foodOption : controller.getFoodOptions())\n" +
                "               <option value=\"${foodOption}\">${foodOption}</option>\n" +
                "            @endfor\n" +
                "         </select>\n" +
                "      </label>\n" +
                "\n" +
                "      <button type=\"submit\">Submit</button>\n" +
                "   </form>\n" +
                "</body>");

        params.put("controller", new TemplateEngine_HtmlInterceptorTest.Controller());

        thenOutputIs("<body>\n" +
                "   <h1>Hello</h1>\n" +
                "\n" +
                "   <form action=\"hello.htm\" method=\"POST\">\n" +
                "\n" +
                "      <label>\n" +
                "         Food option:\n" +
                "         <select name=\"foodOption\">\n" +
                "            <option value=\"\">-</option>\n" +
                "            <option value=\"Cheese\">Cheese</option>\n" +
                "            <option value=\"Onion\">Onion</option>\n" +
                "            <option value=\"Chili\">Chili</option>\n" +
                "         </select>\n" +
                "      </label>\n" +
                "\n" +
                "      <button type=\"submit\">Submit</button>\n" +
                "   </form>\n" +
                "</body>");
    }

    @Test
    void comment() {
        givenTemplate("@param String hello = \"hello\"\n${hello}\n<%-- comment --%>\nworld!");
        thenOutputIs("hello\nworld!");
    }

    @Test
    void comment2() {
        givenTemplate("hello\n<%-- comment --%>\nworld!");
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
    void raw_divs() {
        givenTemplate("@if(true)\n" +
                "  @raw\n" +
                "    <div>\n" +
                "      <b>foo</b>\n" +
                "    </div>\n" +
                "  @endraw\n" +
                "@endif");
        thenOutputIs("<div>\n" +
                "  <b>foo</b>\n" +
                "</div>\n");
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
    void variable_unsafe() {
        givenTemplate(
                "!{String x = \"1\";}\n" +
                "!{String y = \"2\";}\n" +
                "$unsafe{x + y}\n" +
                "done..\n"
        );
        thenOutputIs("12\ndone..\n");
    }

    @Test
    void tag() {
        givenTag("my.jte", "hello..");
        givenTemplate(
                "@if(true)\n" +
                "    @template.tag.my()\n" +
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
                "    @template.layout.my(@`Here is some data: ${42} that's nice.`)\n" +
                "@endif\n" +
                "Next line");

        thenOutputIs(
                "<div>\n" +
                "    Here is some data: 42 that's nice.\n" +
                "</div>\n" +
                "\n" +
                "Next line");
    }

    @Test
    void indentationsArePopped() {
        givenTemplate(
                "<head>\n" +
                "    @if(true)\n" +
                "        <span>foo</span>\n" +
                "        foo@endif\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "</head>");
        thenOutputIs(
                "<head>\n" +
                "    <span>foo</span>\n" +
                "    foo\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "</head>");
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