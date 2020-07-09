package org.jusecase.jte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.jte.output.StringOutput;
import org.jusecase.jte.support.HtmlTagSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngine_Html_TagSupportTest {

    StringOutput output = new StringOutput();
    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver);
    MySampleFrameworkTagSupport htmlTagSupport;

    @BeforeEach
    void setUp() {
        htmlTagSupport = new MySampleFrameworkTagSupport();
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
                "<input name=\"param1\">\n" +
                "<input name=\"param2\">\n" +
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
                "<input name=\"param1\"/>\n" +
                "<input name=\"param2\"/>\n" +
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
                "<input name=\"param1\"></input>\n" +
                "<input name=\"param2\"></input>\n" +
                "<input name=\"__fp\" value=\"a:hello.htm, p:param1,param2\"></form>");
    }

    @Test
    void select() {
        // TODO support for <select> and <option>
    }

    @Test
    void tag() {
        // TODO ensure htmlTagSupport is passed to tags
    }

    @Test
    void layout() {
        // TODO ensure htmlTagSupport is passed to layouts
    }

    private static class MySampleFrameworkTagSupport implements HtmlTagSupport {

        private String action;
        private final List<String> fieldNames = new ArrayList<>();

        @Override
        public void onHtmlTagOpened(String name, Map<String, String> attributes, TemplateOutput output) {
            if ("form".equals(name)) {
                action = attributes.get("action");
            }
            if ("input".equals(name)) {
                fieldNames.add(attributes.get("name"));
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