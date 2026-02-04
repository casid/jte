package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JteTemplateEngineRenderTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("gg.jte.developmentMode:true")
            .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class));

    @Test
    void rendersTemplate() {
        this.contextRunner.run((context) -> {
            TemplateEngine templateEngine = context.getBean(TemplateEngine.class);
            TemplateOutput output = new StringOutput();
            templateEngine.render("greeting.jte", Map.of("subject", "world"), output);

            assertThat(output.toString()).contains("Hello world!");
        });
    }
}
