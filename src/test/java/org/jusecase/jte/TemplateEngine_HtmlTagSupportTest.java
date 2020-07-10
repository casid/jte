package org.jusecase.jte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.jte.output.StringOutput;
import org.jusecase.jte.support.HtmlTagSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateEngine_HtmlTagSupportTest {

    StringOutput output = new StringOutput();
    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver);
    Controller controller = new Controller();
    MySampleFrameworkTagSupport htmlTagSupport = new MySampleFrameworkTagSupport();

    @BeforeEach
    void setUp() {
        templateEngine.setHtmlTags("form", "input", "select", "option");
        templateEngine.setHtmlTagSupport(htmlTagSupport);
    }

    @Test
    void noFields_additionalFieldWritten() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\">\n<input name=\"__fp\" value=\"a:hello.htm, p:\"></form>");
    }

    @Test
    void input() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"param1\">\n" +
                "<input name=\"param2\">\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\">\n" +
                "<input name=\"param1\" value=\"?\">\n" +
                "<input name=\"param2\" value=\"?\">\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\"></form>");
    }

    @Test
    void input_closed1() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"param1\"/>\n" +
                "<input name=\"param2\"/>\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\">\n" +
                "<input name=\"param1\" value=\"?\"/>\n" +
                "<input name=\"param2\" value=\"?\"/>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\"></form>");
    }

    @Test
    void input_closed2() {
        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "<input name=\"param1\"></input>\n" +
                "<input name=\"param2\"></input>\n" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\">\n" +
                "<input name=\"param1\" value=\"?\"></input>\n" +
                "<input name=\"param2\" value=\"?\"></input>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\"></form>");
    }

    @Test
    void select() {
        dummyCodeResolver.givenCode("page.jte", "@param org.jusecase.jte.TemplateEngine_HtmlTagSupportTest.Controller controller\n" +
                "<form action=\"${controller.getUrl()}\">\n" +
                "<select name=\"foodOption\">\n" +
                "@for(var foodOption : controller.getFoodOptions())" +
                "<option value=\"${foodOption}\">Mmmh, ${foodOption}</option>\n" +
                "@endfor" +
                "</select>\n" +
                "</form>");

        controller.setFoodOption("Onion");
        templateEngine.render("page.jte", controller, output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\">\n" +
                "<select name=\"foodOption\">\n" +
                "<option value=\"Cheese\">Mmmh, Cheese</option>\n" +
                "<option value=\"Onion\" selected>Mmmh, Onion</option>\n" +
                "<option value=\"Chili\">Mmmh, Chili</option>\n" +
                "</select>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:foodOption\"></form>");
    }

    @Test
    void tag() {
        dummyCodeResolver.givenCode("tag/formContent.jte",
                "<input name=\"param1\"></input>\n" +
                "<input name=\"param2\"></input>\n");

        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "<form action=\"${url}\">\n" +
                "@tag.formContent()" +
                "</form>");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\">\n" +
                "<input name=\"param1\" value=\"?\"></input>\n" +
                "<input name=\"param2\" value=\"?\"></input>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\"></form>");
    }

    @Test
    void layout() {
        dummyCodeResolver.givenCode("layout/formContent.jte",
                        "@param String url\n" +
                        "<form action=\"${url}\">\n" +
                        "@render(content)" +
                        "</form>");

        dummyCodeResolver.givenCode("page.jte", "@param String url\n" +
                "@layout.formContent(url)\n" +
                "@define(content)" +
                "<input name=\"param1\"></input>\n" +
                "<input name=\"param2\"></input>\n" +
                "@enddefine" +
                "@endlayout");

        templateEngine.render("page.jte", "hello.htm", output);

        assertThat(output.toString()).isEqualTo("<form action=\"hello.htm\">\n" +
                "<input name=\"param1\" value=\"?\"></input>\n" +
                "<input name=\"param2\" value=\"?\"></input>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\"></form>");
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
            return List.of("Cheese", "Onion", "Chili");
        }
    }

    public class MySampleFrameworkTagSupport implements HtmlTagSupport {

        private String action;
        private final List<String> fieldNames = new ArrayList<>();

        @Override
        public void onHtmlTagOpened(String name, Map<String, String> attributes, TemplateOutput output) {
            if ("form".equals(name)) {
                action = attributes.get("action");
            } else if ("input".equals(name)) {
                fieldNames.add(attributes.get("name"));
                output.writeStaticContent(" value=\"?\"");
            } else if ("select".equals(name)) {
                fieldNames.add(attributes.get("name"));
            } else if ("option".equals(name)) {
                String value = attributes.get("value");
                if (value != null && value.equals(controller.getFoodOption())) {
                    output.writeStaticContent(" selected");
                }
            }
        }

        @Override
        public void onHtmlTagClosed(String name, TemplateOutput output) {
            if ("form".equals(name)) {
                output.writeStaticContent("<input name=\"__fp\" value=\"a:" + action + ", p:" + String.join(",", fieldNames) + "\">");
            }
        }
    }
}