package gg.jte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateEngine_HasTemplateTest {

    TemplateEngine templateEngine;
    DummyCodeResolver codeResolver = new DummyCodeResolver();

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
        codeResolver.givenCode("foo.jte", "Hello, foo!");
        assertThat(templateEngine.hasTemplate("foo.jte")).isTrue();
    }

    @Test
    void templateIsAddedDuringDevelopment() {
        assertThat(templateEngine.hasTemplate("foo.jte")).isFalse();
        codeResolver.givenCode("foo.jte", "Hello, foo!");
        assertThat(templateEngine.hasTemplate("foo.jte")).isTrue();
    }
}
