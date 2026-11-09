package gg.jte;

import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngine_HasTemplate_DirectoryCodeResolverTest {

    TemplateEngine templateEngine;
    DirectoryCodeResolver codeResolver = new DirectoryCodeResolver(Paths.get("src/test/resources/benchmark"));

    @BeforeEach
    void setUp() {
        templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
    }

    @Test
    void noTemplateExists() {
        assertThat(codeResolver.exists("foo.jte")).isFalse();
        assertThat(templateEngine.hasTemplate("foo.jte")).isFalse();
    }

    @Test
    void templateExists() {
        assertThat(codeResolver.exists("welcome.jte")).isTrue();
        assertThat(templateEngine.hasTemplate("welcome.jte")).isTrue();
    }

    @Test
    void resolveRequired() {
        Throwable throwable = catchThrowable(() -> codeResolver.resolveRequired("does-not-exist.jte"));

        assertThat(throwable).isInstanceOf(TemplateNotFoundException.class)
                .hasMessageStartingWith("does-not-exist.jte not found (tried to load file at ")
                .hasMessageEndingWith("does-not-exist.jte)");
    }

    @Test
    void notFoundCallbackReceivesException() {
        List<TemplateNotFoundException> exceptions = new ArrayList<>();

        assertThat(templateEngine.hasTemplate("foo.jte", exceptions::add)).isFalse();

        assertThat(exceptions).hasSize(1);
        assertThat(exceptions.get(0).getMessage()).contains("foo.jte");
    }

    @Test
    void notFoundCallbackNotCalledIfTemplateExists() {
        List<TemplateNotFoundException> exceptions = new ArrayList<>();

        assertThat(templateEngine.hasTemplate("welcome.jte", exceptions::add)).isTrue();

        assertThat(exceptions).isEmpty();
    }
}
