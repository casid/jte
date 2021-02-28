package gg.jte;

import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

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
}
