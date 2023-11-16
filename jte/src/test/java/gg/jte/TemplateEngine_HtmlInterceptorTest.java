package gg.jte;

import gg.jte.html.HtmlInterceptor;
import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngine_HtmlInterceptorTest {

    StringOutput output = new StringOutput();
    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver, ContentType.Html);
    Controller controller = new Controller();
    MySampleFrameworkInterceptor htmlInterceptor = new MySampleFrameworkInterceptor();

    @BeforeEach
    void setUp() {
        templateEngine.setHtmlTags("form", "input", "select", "option");
        templateEngine.setHtmlInterceptor(htmlInterceptor);
        templateEngine.setTrimControlStructures(true);
    }

    @Test
    void noFields_additionalFieldWritten() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                </form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n<input name=\"__fp\" value=\"a:hello.htm, p:\">\n</form>");
    }

    @Test
    void formInIf() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                @if(true)
                    <form action="${url}">
                        <input name="x"/>
                    </form>@endif""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                    <input name="x" value="?"/>
                <input name="__fp" value="a:hello.htm, p:x">
                </form>""");
    }

    @Test
    void input() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                <input name="param1">
                <input name="param2">
                </form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <input name="param1" value="?">
                <input name="param2" value="?">
                <input name="__fp" value="a:hello.htm, p:param1,param2">
                </form>""");
    }

    @Test
    void input_int() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                <input name="age" value="${23}">
                </form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <input name="age" value="23">
                <input name="__fp" value="a:hello.htm, p:age">
                </form>""");
    }

    @Test
    void input_closed1() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                <input name="param1"/>
                <input name="param2"/>
                </form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <input name="param1" value="?"/>
                <input name="param2" value="?"/>
                <input name="__fp" value="a:hello.htm, p:param1,param2">
                </form>""");
    }

    @Test
    void input_closedWrongly() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                <input name="param1"></input>
                <input name="param2"></input>
                </form>""");

        Throwable throwable = catchThrowable(() -> templateEngine.render("page.jte", "hello.htm", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile page.jte, error at line 3: Unclosed tag <form>, expected </form>, got </input>.");
    }

    @Test
    void input_noAttributes() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<input>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<input>");
    }

    @Test
    void input_withRegex() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<input required pattern=\"\\w+\">");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<input required pattern=\"\\w+\">");
    }

    @Test
    void input_nullAttribute() {
        dummyCodeResolver.givenCode("page.jte", "@param String css\n" +
                "<input type=\"checkbox\" class=\"${css}\">");

        templateEngine.render("page.jte", (String)null, output);

        assertThat(output.toString()).isEqualTo("<input type=\"checkbox\">");
    }

    @Test
    void input_disabled() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                <input name="param1" disabled>
                <input name="param2">
                </form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"param1\" disabled value=\"?\">\n" +
                "<input name=\"param2\" value=\"?\">\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param2\">\n" + // No param1 here
                "</form>");
    }

    @Test
    void select() {
        dummyCodeResolver.givenCode("page.jte", """
                @param gg.jte.TemplateEngine_HtmlInterceptorTest.Controller controller
                <form action="${controller.getUrl()}">
                <select name="foodOption">
                @for(String foodOption : controller.getFoodOptions())<option value="${foodOption}">Mmmh, ${foodOption}</option>
                @endfor</select>
                </form>""");

        controller.setFoodOption("Onion");
        templateEngine.render("page.jte", controller, output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <select name="foodOption">
                <option value="Cheese">Mmmh, Cheese</option>
                <option value="Onion" selected>Mmmh, Onion</option>
                <option value="Chili">Mmmh, Chili</option>
                </select>
                <input name="__fp" value="a:hello.htm, p:foodOption">
                </form>""");
    }

    @Test
    void tag() {
        dummyCodeResolver.givenCode("tag/formContent.jte",
                """
                        <input name="param1"></input>
                        <input name="param2"></input>
                        """);

        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                @template.tag.formContent()</form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <input name="param1" value="?"></input>
                <input name="param2" value="?"></input>
                <input name="__fp" value="a:hello.htm, p:param1,param2">
                </form>""");
    }

    @Test
    void layout() {
        dummyCodeResolver.givenCode("layout/formContent.jte",
                """
                        @param String url
                        @param gg.jte.Content content
                        <form action="${url}">
                        ${content}</form>""");

        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                @template.layout.formContent(url, content = @`<input name="param1"></input>
                <input name="param2"></input>
                `)""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <input name="param1" value="?"></input>
                <input name="param2" value="?"></input>
                <input name="__fp" value="a:hello.htm, p:param1,param2">
                </form>""");
    }

    @Test
    void form() {
        dummyCodeResolver.givenCode("page.jte", """
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

        templateEngine.render("page.jte", controller, output);

        assertThat(output.toString()).isEqualTo("""
                <body>
                   <h1>Hello</h1>

                   <form action="hello.htm" method="POST" data-form="x">

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
                   <input name="__fp" value="a:hello.htm, p:foodOption">
                </form>
                </body>""");
    }

    @Test
    void errorData() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                <input name="error" class="foo">
                </form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <input name="error" class="foo" value="?" data-error="1">
                <input name="__fp" value="a:hello.htm, p:error">
                </form>""");
    }

    @Test
    void errorData_indentation() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                @if(true)
                    <input name="error" class="foo">
                @endif
                </form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("""
                <form action="hello.htm" data-form="x">
                <input name="error" class="foo" value="?" data-error="1">
                <input name="__fp" value="a:hello.htm, p:error">
                </form>""");
    }

    @Test
    void raw_entireForm() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                @raw
                    <form action="${url}">
                        <input name="x"/>
                    </form>@endraw""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo(
                """
                        <form action="${url}">
                            <input name="x"/>
                        </form>""");
    }

    @Test
    void raw_partOfForm() {
        dummyCodeResolver.givenCode("page.jte", """
                @param String url
                <form action="${url}">
                    @raw
                        <input name="${x}"/>
                    @endraw</form>""");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo(
                """
                        <form action="hello.htm" data-form="x">
                            <input name="${x}"/>
                        <input name="__fp" value="a:hello.htm, p:">
                        </form>""");
    }

    @Test
    void getterWithSideEffect() {
        dummyCodeResolver.givenCode("page.jte", """
                @param gg.jte.TemplateEngine_HtmlInterceptorTest.Controller controller
                <form action="${controller.getterWithSideEffect()}">
                </form>""");

        templateEngine.render("page.jte", controller, output);

        assertThat(output.toString()).isEqualTo("""
                <form action="1" data-form="x">
                <input name="__fp" value="a:1, p:">
                </form>""");

        assertThat(controller.getterWithSideEffectCount).isEqualTo(1); // Method is called only once.
    }

    @Nested
    class InterpolationInInterceptedName {

        MyAttributeTrackingInterceptor interceptor = new MyAttributeTrackingInterceptor();

        @BeforeEach
        void setUp() {
            templateEngine.setHtmlInterceptor(interceptor);
        }

        @Test
        void case1() {
            dummyCodeResolver.givenCode("page.jte", "<input type=\"checkbox\" name=\"check-${1}\"/>");

            templateEngine.render("page.jte", null, output);

            assertThat(output.toString()).isEqualTo("<input type=\"checkbox\" name=\"check-1\"/>");
            assertThat(interceptor.lastAttributes.get("name")).isEqualTo("check-1");
        }

        @Test
        void case2() {
            dummyCodeResolver.givenCode("page.jte", "<input type=\"checkbox\" name=\"${1}${2}\"/>");

            templateEngine.render("page.jte", null, output);

            assertThat(output.toString()).isEqualTo("<input type=\"checkbox\" name=\"12\"/>");
            assertThat(interceptor.lastAttributes.get("name")).isEqualTo("12");
        }

        @Test
        void case3() {
            dummyCodeResolver.givenCode("page.jte", "<input type=\"checkbox\" name=\"${1}+${2}+${1+2}\"/>");

            templateEngine.render("page.jte", null, output);

            assertThat(output.toString()).isEqualTo("<input type=\"checkbox\" name=\"1+2+3\"/>");
            assertThat(interceptor.lastAttributes.get("name")).isEqualTo("1+2+3");
        }

        @Test
        void case4() {
            dummyCodeResolver.givenCode("page.jte", "<input type=\"checkbox\" name=\"${\"${112}\"}\"/>");

            templateEngine.render("page.jte", null, output);

            assertThat(output.toString()).isEqualTo("<input type=\"checkbox\" name=\"${112}\"/>");
            assertThat(interceptor.lastAttributes.get("name")).isEqualTo("${112}");
        }

        @Test
        void case5() {
            dummyCodeResolver.givenCode("page.jte", "@param String name\n<input type=\"checkbox\" name=\"check-${name}\"/>");

            templateEngine.render("page.jte", "\"<script>", output);

            assertThat(output.toString()).isEqualTo("<input type=\"checkbox\" name=\"check-&#34;&lt;script>\"/>");
            assertThat(interceptor.lastAttributes.get("name")).isEqualTo("check-\"<script>");
        }

        @Test
        void case6() {
            dummyCodeResolver.givenCode("page.jte", "@param String name\n<input type=\"checkbox\" name=\"check\\${name}\"/>");

            templateEngine.render("page.jte", "foo", output);

            assertThat(output.toString()).isEqualTo("<input type=\"checkbox\" name=\"check\\foo\"/>");
            assertThat(interceptor.lastAttributes.get("name")).isEqualTo("check\\foo");
        }

        @Test
        void case7() {
            dummyCodeResolver.givenCode("page.jte", "@param int index\n<input type=\"checkbox\" name=\"${\"name-\" + index}\"/>");

            templateEngine.render("page.jte", 42, output);

            assertThat(output.toString()).isEqualTo("<input type=\"checkbox\" name=\"name-42\"/>");
            assertThat(interceptor.lastAttributes.get("name")).isEqualTo("name-42");
        }
    }

    @SuppressWarnings("unused")
    public static class Controller {
        private String foodOption;
        private int getterWithSideEffectCount;

        public String getUrl() {
            return "hello.htm";
        }

        public String getterWithSideEffect() {
            ++getterWithSideEffectCount;
            return "" + getterWithSideEffectCount;
        }

        public String getFoodOption() {
            return foodOption;
        }

        public void setFoodOption(String foodOption) {
            this.foodOption = foodOption;
        }

        public List<String> getFoodOptions() {
            return Arrays.asList("Cheese", "Onion", "Chili");
        }
    }

    public class MySampleFrameworkInterceptor implements HtmlInterceptor {

        private String action;
        private final List<String> fieldNames = new ArrayList<>();

        @Override
        public void onHtmlTagOpened(String name, Map<String, Object> attributes, TemplateOutput output) {
            if ("form".equals(name)) {
                action = (String)attributes.get("action");
                output.writeContent(" data-form=\"x\"");
            } else if ("input".equals(name)) {
                if (!Boolean.TRUE.equals(attributes.get("disabled"))) {
                    fieldNames.add((String) attributes.get("name"));
                }
                if (attributes.containsKey("name") && !attributes.containsKey("value")) {
                    output.writeContent(" value=\"?\"");
                }
            } else if ("select".equals(name)) {
                fieldNames.add((String)attributes.get("name"));
            } else if ("option".equals(name)) {
                String value = (String)attributes.get("value");
                if (value != null && value.equals(controller.getFoodOption())) {
                    output.writeContent(" selected");
                }
            }

            if ("error".equals(attributes.get("name"))) {
                output.writeContent(" data-error=\"1\"");
            }
        }

        @Override
        public void onHtmlTagClosed(String name, TemplateOutput output) {
            if ("form".equals(name)) {
                output.writeContent("<input name=\"__fp\" value=\"a:" + action + ", p:" + String.join(",", fieldNames) + "\">\n");
            }
        }
    }

    public static class MyAttributeTrackingInterceptor implements HtmlInterceptor {

        Map<String, Object> lastAttributes;

        @Override
        public void onHtmlTagOpened(String name, Map<String, Object> attributes, TemplateOutput output) {
            lastAttributes = attributes;
        }

        @Override
        public void onHtmlTagClosed(String name, TemplateOutput output) {

        }
    }
}