package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JteAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class));

    @Test
    void autoConfigurationBackOffWithoutJte() {
        this.contextRunner.withClassLoader(new FilteredClassLoader("gg.jte"))
                .run((context) -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(TemplateEngine.class));
    }

    @Test
    void templateEngineIsAvailable() {
        this.contextRunner.withPropertyValues("gg.jte.developmentMode:true")
                .run((context) -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(TemplateEngine.class));
    }

    @Test
    void unconfiguredOperatingMode() {
        this.contextRunner.run((context) -> assertThat(context).hasFailed());
    }
}
