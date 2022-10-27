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
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n<input name=\"__fp\" value=\"a:hello.htm, p:\">\n</form>");
    }

    @Test
    void formInIf() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "@if(true)\n" +
                "    <form action=\"${url}\">\n" +
                "        <input name=\"x\"/>\n" +
                "    </form>" +
                "@endif");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "    <input name=\"x\" value=\"?\"/>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:x\">\n" +
                "</form>");
    }

    @Test
    void input() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"param1\">\n" +
                "<input name=\"param2\">\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"param1\" value=\"?\">\n" +
                "<input name=\"param2\" value=\"?\">\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\">\n" +
                "</form>");
    }

    @Test
    void input_int() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"age\" value=\"${23}\">\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"age\" value=\"23\">\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:age\">\n" +
                "</form>");
    }

    @Test
    void input_closed1() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"param1\"/>\n" +
                "<input name=\"param2\"/>\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"param1\" value=\"?\"/>\n" +
                "<input name=\"param2\" value=\"?\"/>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\">\n" +
                "</form>");
    }

    @Test
    void input_closedWrongly() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"param1\"></input>\n" +
                "<input name=\"param2\"></input>\n" +
                "</form>");

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
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"param1\" disabled>\n" +
                "<input name=\"param2\">\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"param1\" disabled value=\"?\">\n" +
                "<input name=\"param2\" value=\"?\">\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param2\">\n" + // No param1 here
                "</form>");
    }

    @Test
    void select() {
        dummyCodeResolver.givenCode("page.jte", "@param gg.jte.TemplateEngine_HtmlInterceptorTest.Controller controller\n" +
                "<form action=\"${controller.getUrl()}\">\n" +
                "<select name=\"foodOption\">\n" +
                "@for(String foodOption : controller.getFoodOptions())" +
                "<option value=\"${foodOption}\">Mmmh, ${foodOption}</option>\n" +
                "@endfor" +
                "</select>\n" +
                "</form>");

        controller.setFoodOption("Onion");
        templateEngine.render("page.jte", controller, output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<select name=\"foodOption\">\n" +
                "<option value=\"Cheese\">Mmmh, Cheese</option>\n" +
                "<option value=\"Onion\" selected>Mmmh, Onion</option>\n" +
                "<option value=\"Chili\">Mmmh, Chili</option>\n" +
                "</select>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:foodOption\">\n" +
                "</form>");
    }

    @Test
    void tag() {
        dummyCodeResolver.givenCode("tag/formContent.jte",
                "<input name=\"param1\"></input>\n" +
                "<input name=\"param2\"></input>\n");

        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "@template.tag.formContent()" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"param1\" value=\"?\"></input>\n" +
                "<input name=\"param2\" value=\"?\"></input>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\">\n" +
                "</form>");
    }

    @Test
    void layout() {
        dummyCodeResolver.givenCode("layout/formContent.jte",
                        "@param String url\n" +
                        "@param gg.jte.Content content\n" +
                        "<form action=\"${url}\">\n" +
                        "${content}" +
                        "</form>");

        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "@template.layout.formContent(url, content = @`" +
                "<input name=\"param1\"></input>\n" +
                "<input name=\"param2\"></input>\n" +
                "`)");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"param1\" value=\"?\"></input>\n" +
                "<input name=\"param2\" value=\"?\"></input>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\">\n" +
                "</form>");
    }

    @Test
    void form() {
        dummyCodeResolver.givenCode("page.jte", "@param gg.jte.TemplateEngine_HtmlInterceptorTest.Controller controller\n"
              + "<body>\n"
              + "   <h1>Hello</h1>\n"
              + "\n"
              + "   <form action=\"${controller.getUrl()}\" method=\"POST\">\n"
              + "\n"
              + "      <label>\n"
              + "         Food option:\n"
              + "         <select name=\"foodOption\">\n"
              + "            <option value=\"\">-</option>\n"
              + "            @for(String foodOption : controller.getFoodOptions())\n"
              + "               <option value=\"${foodOption}\">${foodOption}</option>\n"
              + "            @endfor\n"
              + "         </select>\n"
              + "      </label>\n"
              + "\n"
              + "      <button type=\"submit\">Submit</button>\n"
              + "   </form>\n"
              + "</body>");

        templateEngine.render("page.jte", controller, output);

        assertThat(output.toString()).isEqualTo("<body>\n"
              + "   <h1>Hello</h1>\n"
              + "\n"
              + "   <form action=\"hello.htm\" method=\"POST\" data-form=\"x\">\n"
              + "\n"
              + "      <label>\n"
              + "         Food option:\n"
              + "         <select name=\"foodOption\">\n"
              + "            <option value=\"\">-</option>\n"
              + "            <option value=\"Cheese\">Cheese</option>\n"
              + "            <option value=\"Onion\">Onion</option>\n"
              + "            <option value=\"Chili\">Chili</option>\n"
              + "         </select>\n"
              + "      </label>\n"
              + "\n"
              + "      <button type=\"submit\">Submit</button>\n"
              + "   <input name=\"__fp\" value=\"a:hello.htm, p:foodOption\">\n"
              + "</form>\n"
              + "</body>");
    }

    @Test
    void errorData() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"error\" class=\"foo\">\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"error\" class=\"foo\" value=\"?\" data-error=\"1\">\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:error\">\n" +
                "</form>");
    }

    @Test
    void errorData_indentation() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "@if(true)\n" +
                "    <input name=\"error\" class=\"foo\">\n" +
                "@endif\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\" data-form=\"x\">\n" +
                "<input name=\"error\" class=\"foo\" value=\"?\" data-error=\"1\">\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:error\">\n" +
                "</form>");
    }

    @Test
    void raw_entireForm() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "@raw\n" +
                "    <form action=\"${url}\">\n" +
                "        <input name=\"x\"/>\n" +
                "    </form>" +
                "@endraw");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo(
                "<form action=\"${url}\">\n" +
                "    <input name=\"x\"/>\n" +
                "</form>");
    }

    @Test
    void raw_partOfForm() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "    @raw\n" +
                "        <input name=\"${x}\"/>\n" +
                "    @endraw" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo(
                "<form action=\"hello.htm\" data-form=\"x\">\n" +
                        "    <input name=\"${x}\"/>\n" +
                        "<input name=\"__fp\" value=\"a:hello.htm, p:\">\n" +
                        "</form>");
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
    }

    @SuppressWarnings("unused")
    public static class Controller {
        private String foodOption;

        public String getUrl() {
            return "hello.htm";
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

    public class MyAttributeTrackingInterceptor implements HtmlInterceptor {

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