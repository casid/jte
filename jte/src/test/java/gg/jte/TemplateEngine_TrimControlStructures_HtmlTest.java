package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for mode {@link TemplateEngine#setTrimControlStructures(boolean)} with {@link ContentType#Html}
 */
@SuppressWarnings("SameParameterValue")
public class TemplateEngine_TrimControlStructures_HtmlTest {
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
                """
                        @import java.time.Instant
                        @import java.time.temporal.ChronoUnit
                        @param String name
                        !{
                            String ts = "ts";
                            String message = "Hello " + name;
                        }
                        <!DOCTYPE cXML SYSTEM "https://example.com/example.dtd">
                        <Issue xml:lang="en-US" timestamp="${ts}" someID="${name}@internal.example.com">
                          <Response><Status code="foo" text="bar">${message}</Status></Response>
                        </Issue>""");

        thenOutputIs("""
                <!DOCTYPE cXML SYSTEM "https://example.com/example.dtd">
                <Issue xml:lang="en-US" timestamp="ts" someID="@internal.example.com">
                  <Response><Status code="foo" text="bar">Hello null</Status></Response>
                </Issue>""");
    }

    @Test
    void attributes() {
        givenTemplate("""
                @if(true)
                    <a href="${"url"}" class="nav-item@if(true) is-active@endif">foo</a>
                @endif""");
        thenOutputIs("<a href=\"url\" class=\"nav-item is-active\">foo</a>\n");
    }

    @Test
    void ifStatement() {
        givenTemplate(
                """
                        @if(true)
                            Yay,
                            it's true!
                        @endif
                        """);
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
                """
                        @if(true)
                            Yay,
                          it's true!
                        @endif
                        """);
        thenOutputIs("Yay,\nit's true!\n");
    }

    @Test
    void ifStatement_varyingIndentations2() {
        givenTemplate(
                """
                        @if(true)
                          Yay,
                            it's true!
                        @endif
                        """);
        thenOutputIs("Yay,\n  it's true!\n");
    }

    @Test
    void ifStatementNested() {
        givenTemplate(
                """
                        @if(true)
                            @if(true)
                                Yay,
                                it's double true!
                            @endif
                        @endif
                        """);
        thenOutputIs("Yay,\nit's double true!\n");
    }

    @Test
    void ifStatementNestedDiv() {
        givenTemplate(
                """
                        <div>
                            @if(true)
                                @if(true)
                                    Yay,
                                    it's double true!
                                @endif
                            @endif
                        </div>
                        """);

        thenOutputIs(
                """
                        <div>
                            Yay,
                            it's double true!
                        </div>
                        """);
    }

    @Test
    void ifStatementNestedDiv_tabs() {
        givenTemplate(
                """
                        <div>
                        \t@if(true)
                        \t\t@if(true)
                        \t\t\tYay,
                        \t\t\tit's double true!
                        \t\t@endif
                        \t@endif
                        </div>
                        """);

        thenOutputIs(
                """
                        <div>
                        \tYay,
                        \tit's double true!
                        </div>
                        """);
    }

    @Test
    void ifStatementNestedDiv_tabs_windowsLineEndings() {
        givenTemplate(
                """
                        <div>\r
                        \t@if(true)\r
                        \t\t@if(true)\r
                        \t\t\tYay,\r
                        \t\t\tit's double true!\r
                        \t\t@endif\r
                        \t@endif\r
                        </div>\r
                        """);

        thenOutputIs(
                """
                        <div>\r
                        \tYay,\r
                        \tit's double true!\r
                        </div>\r
                        """);
    }

    @Test
    void ifElseStatementNestedDiv() {
        givenTemplate(
                """
                        <div>
                            @if(true)
                                @if(false)
                                    Yay,
                                    it's double true!
                                @else
                                    <p>
                                        It's not ${true}, that's for sure
                                    </p>
                                @endif
                            @endif
                        </div>
                        """);

        thenOutputIs(
                """
                        <div>
                            <p>
                                It's not true, that's for sure
                            </p>
                        </div>
                        """);
    }

    @Test
    void ifElseIfStatementNestedDiv() {
        givenTemplate(
                """
                        <div>
                            @if(true)
                                @if(false)
                                    Yay,
                                    it's double true!
                                @elseif(true)
                                    <p>
                                        It's not ${true}, that's for sure
                                    </p>
                                @endif
                            @endif
                        </div>
                        """);

        thenOutputIs(
                """
                        <div>
                            <p>
                                It's not true, that's for sure
                            </p>
                        </div>
                        """);
    }

    @Test
    void forEachNestedDiv() {
        givenTemplate(
                """
                        <div>
                            @if(true)
                                @for(int i = 0; i < 3; ++i)
                                    @if(false)
                                        Yay,
                                        it's double true!
                                    @elseif(true)
                                        <p>
                                            It's not ${true}, that's for sure
                                        </p>
                                    @endif
                                @endfor
                            @endif
                        </div>
                        """);

        thenOutputIs(
                """
                        <div>
                            <p>
                                It's not true, that's for sure
                            </p>
                            <p>
                                It's not true, that's for sure
                            </p>
                            <p>
                                It's not true, that's for sure
                            </p>
                        </div>
                        """);
    }

    @Test
    void formWithLoop() {
        givenTemplate("""
                @param gg.jte.TemplateEngine_HtmlInterceptorTest.Controller controller
                <body>
                   <h1>Hello</h1>

                   <form action="${controller.getUrl()}" method="POST">

                      <label>
                         Food option:
                         <select name="foodOption">
                            <option value="">-</option>
                            @for(String foodOption : controller.getFoodOptions())
                               <option value="${foodOption}">${foodOption}</option>
                            @endfor
                         </select>
                      </label>

                      <button type="submit">Submit</button>
                   </form>
                </body>""");

        params.put("controller", new TemplateEngine_HtmlInterceptorTest.Controller());

        thenOutputIs("""
                <body>
                   <h1>Hello</h1>

                   <form action="hello.htm" method="POST">

                      <label>
                         Food option:
                         <select name="foodOption">
                            <option value="">-</option>
                            <option value="Cheese">Cheese</option>
                            <option value="Onion">Onion</option>
                            <option value="Chili">Chili</option>
                         </select>
                      </label>

                      <button type="submit">Submit</button>
                   </form>
                </body>""");
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
        givenTemplate("""
                @if(true)
                  @raw
                    @template(foo, bar) => ${something}
                  @endraw
                @endif""");
        thenOutputIs("@template(foo, bar) => ${something}\n");
    }

    @Test
    void raw_divs() {
        givenTemplate("""
                @if(true)
                  @raw
                    <div>
                      <b>foo</b>
                    </div>
                  @endraw
                @endif""");
        thenOutputIs("""
                <div>
                  <b>foo</b>
                </div>
                """);
    }

    @Test
    void raw_oneLine() {
        givenTemplate("@rawfoo@endraw");
        thenOutputIs("foo");
    }

    @Test
    void variable() {
        givenTemplate(
                """
                        !{int x = 1;}
                        !{int y = 2;}
                        ${x + y}
                        done..
                        """
        );
        thenOutputIs("3\ndone..\n");
    }

    @Test
    void variable_unsafe() {
        givenTemplate(
                """
                        !{String x = "1";}
                        !{String y = "2";}
                        $unsafe{x + y}
                        done..
                        """
        );
        thenOutputIs("12\ndone..\n");
    }

    @Test
    void tag() {
        givenTag("my.jte", "hello..");
        givenTemplate(
                """
                        @if(true)
                            @template.tag.my()
                        @endif
                        Next line""");
        thenOutputIs("hello..\nNext line");
    }

    @Test
    void layout() {
        givenLayout("my.jte",
                """
                        @param gg.jte.Content data
                        @if(data != null)
                            <div>
                                ${data}
                            </div>
                        @endif
                        """
        );
        givenTemplate(
                """
                        @if(true)
                            @template.layout.my(@`Here is some data: ${42} that's nice.`)
                        @endif
                        Next line""");

        thenOutputIs(
                """
                        <div>
                            Here is some data: 42 that's nice.
                        </div>

                        Next line""");
    }

    @Test
    void indentationsArePopped() {
        givenTemplate(
                """
                        <head>
                            @if(true)
                                <span>foo</span>
                                foo@endif
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                        </head>""");
        thenOutputIs(
                """
                        <head>
                            <span>foo</span>
                            foo
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                        </head>""");
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