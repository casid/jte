package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SameParameterValue")
public class TemplateEngine_ParentClassLoaderTest {
    String templateName = "test/template.jte";

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver, Path.of("jte-classes"), ContentType.Plain, new URLClassLoader(new URL[0]));


    @Test
    void helloWorld() {
        givenTemplate("${hello} World");
        thenOutputIs("Hello World");
    }

    private void givenTemplate(String template) {
        template = "@param String hello\n" + template;
        givenRawTemplate(template);
    }

    private void givenRawTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(templateName, "Hello", output);

        assertThat(output.toString()).isEqualTo(expected);
    }
}