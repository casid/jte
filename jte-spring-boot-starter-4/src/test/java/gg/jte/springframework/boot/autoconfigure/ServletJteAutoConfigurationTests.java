package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class ServletJteAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class, ServletJteAutoConfiguration.class, ReactiveJteAutoConfiguration.class))
            .withPropertyValues("gg.jte.developmentMode:true");

    @Test
    void autoConfigurationBackOffWithoutJte() {
        this.contextRunner.withClassLoader(new FilteredClassLoader("gg.jte"))
                .run((context) -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(TemplateEngine.class));
    }

    @Test
    void templateEngineIsAvailable() {
        this.contextRunner.run((context) -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(TemplateEngine.class));
    }

    @Test
    void jteViewResolverIsAvailable() {
        this.contextRunner.run((context) -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(JteViewResolver.class)
                .doesNotHaveBean(ReactiveJteViewResolver.class));
    }

    @Test
    void missingAbstractTemplateViewResolver() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(AbstractTemplateViewResolver.class))
                .run((context) -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(TemplateEngine.class)
                        .doesNotHaveBean(JteViewResolver.class));
    }
}
