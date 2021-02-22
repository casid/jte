package gg.jte;

import gg.jte.resolve.ResourceCodeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateEngine_HasTemplate_ResourceCodeResolverTest {

    TemplateEngine templateEngine;
    ResourceCodeResolver codeResolver = new ResourceCodeResolver("benchmark");

    @BeforeEach
    void setUp() {
        templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
    }

    @Test
    void noTemplateExists() {
        assertThat(templateEngine.hasTemplate("foo.jte")).isFalse();
    }

    @Test
    void templateExists() {
        assertThat(templateEngine.hasTemplate("welcome.jte")).isTrue();
    }
}
