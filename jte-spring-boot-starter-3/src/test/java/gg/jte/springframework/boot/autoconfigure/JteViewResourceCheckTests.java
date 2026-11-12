package gg.jte.springframework.boot.autoconfigure;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class JteViewResourceCheckTests {

    @TempDir
    Path templateDirectory;

    @TempDir
    Path classDirectory;

    TemplateEngine templateEngine;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(templateDirectory.resolve("hello.jte"), "Hello");
        templateEngine = TemplateEngine.create(new DirectoryCodeResolver(templateDirectory), classDirectory, ContentType.Plain);
    }

    @Test
    void servletViewFindsExistingTemplate() {
        JteView view = new JteView(templateEngine);
        view.setUrl("hello.jte");

        assertThat(view.checkResource(Locale.ROOT)).isTrue();
    }

    @Test
    void servletViewReportsMissingTemplateWithoutThrowing() {
        JteView view = new JteView(templateEngine);
        view.setUrl("does-not-exist.jte");

        assertThat(view.checkResource(Locale.ROOT)).isFalse();
    }

    @Test
    void reactiveViewFindsExistingTemplate() {
        ReactiveJteView view = new ReactiveJteView(templateEngine);
        view.setUrl("hello.jte");

        assertThat(view.checkResourceExists(Locale.ROOT)).isTrue();
    }

    @Test
    void reactiveViewReportsMissingTemplateWithoutThrowing() {
        ReactiveJteView view = new ReactiveJteView(templateEngine);
        view.setUrl("does-not-exist.jte");

        assertThat(view.checkResourceExists(Locale.ROOT)).isFalse();
    }
}
