package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.benchmark.WelcomePage;
import org.jusecase.jte.output.StringOutput;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class PreCompileTemplatesTest {
    @Test
    void precompileAll() {
        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/test/resources/benchmark")), Path.of("jte"));
        templateEngine.cleanAll();
        templateEngine.precompileAll();

        StringOutput output = new StringOutput();
        templateEngine.render("welcome.jte", new WelcomePage(12), output);
        assertThat(output.toString()).contains("This page has 12 visits already.");
    }

    @Test
    void precompileAll_externalClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader externalClassLoader = new URLClassLoader(new URL[] {contextClassLoader.getResource("external/external.jar")});
            Thread.currentThread().setContextClassLoader(externalClassLoader);

            TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/test/resources/external")), Path.of("jte"));
            templateEngine.cleanAll();
            templateEngine.precompileAll(Arrays.asList("src/test/resources/external/external.jar", "target/classes"));

            assertThat(Files.exists(Path.of("jte", "org", "jusecase", "jte", "generated", "JteexternalGenerated.java"))).isTrue();
            assertThat(Files.exists(Path.of("jte", "org", "jusecase", "jte", "generated", "JteexternalGenerated.class"))).isTrue();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
}
