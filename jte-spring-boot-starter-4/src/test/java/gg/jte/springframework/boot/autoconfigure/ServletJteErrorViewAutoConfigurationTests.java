package gg.jte.springframework.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.error.DefaultErrorViewResolver;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ServletJteErrorViewAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class, ServletJteAutoConfiguration.class))
            .withPropertyValues("gg.jte.developmentMode:true");

    @Test
    void errorViewResolverRegisteredByDefault() {
        contextRunner.run(context -> assertThat(context).hasNotFailed()
                .hasSingleBean(ErrorViewResolver.class)
                .hasSingleBean(JteErrorViewResolver.class));
    }

    @Test
    void canBeDisabled() {
        contextRunner.withPropertyValues("gg.jte.error-view.enabled=false")
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ErrorViewResolver.class));
    }

    @Test
    void propertiesAreBound() {
        contextRunner.withPropertyValues("gg.jte.error-view.path=pages/errors")
                .run(context -> assertThat(context.getBean(JteProperties.class).getErrorView().getPath()).isEqualTo("pages/errors"));
    }

    @Test
    void defaultPropertyValues() {
        contextRunner.run(context -> {
            JteProperties.ErrorView errorView = context.getBean(JteProperties.class).getErrorView();
            assertThat(errorView.isEnabled()).isTrue();
            assertThat(errorView.getPath()).isEqualTo("error");
        });
    }

    @Test
    void backsOffWhenUserDefinesErrorViewResolver() {
        contextRunner.withUserConfiguration(CustomResolverConfiguration.class)
                .run(context -> assertThat(context).hasNotFailed()
                        .hasSingleBean(ErrorViewResolver.class)
                        .doesNotHaveBean(JteErrorViewResolver.class)
                        .hasBean("customErrorViewResolver"));
    }

    @Test
    void backsOffWithoutBootErrorViewClasses() {
        contextRunner.withClassLoader(new FilteredClassLoader(DefaultErrorViewResolver.class))
                .run(context -> assertThat(context).hasNotFailed()
                        .hasSingleBean(JteViewResolver.class)
                        .doesNotHaveBean(JteErrorViewResolver.class));
    }

    @Test
    void notRegisteredForReactiveApplications() {
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class, ServletJteAutoConfiguration.class))
                .withPropertyValues("gg.jte.developmentMode:true")
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ErrorViewResolver.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomResolverConfiguration {
        @Bean
        ErrorViewResolver customErrorViewResolver() {
            return (request, status, model) -> null;
        }
    }
}
