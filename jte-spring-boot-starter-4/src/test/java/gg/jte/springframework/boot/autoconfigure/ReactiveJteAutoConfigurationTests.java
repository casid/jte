package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.web.reactive.result.view.UrlBasedViewResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactiveJteAutoConfigurationTests {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
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
    void reactiveJteViewResolverIsAvailable() {
        this.contextRunner.run((context) -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(ReactiveJteViewResolver.class)
                .doesNotHaveBean(JteViewResolver.class));
    }

    @Test
    void missingUrlBasedViewResolver() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(UrlBasedViewResolver.class))
                .run((context) -> assertThat(context)
                        .hasNotFailed()
                        .hasSingleBean(TemplateEngine.class)
                        .doesNotHaveBean(ReactiveJteViewResolver.class));
    }
}
