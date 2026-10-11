package gg.jte.springframework.boot.autoconfigure;

import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JteClassDirectoryTests {

    @TempDir
    Path temporaryDirectory;

    @Test
    void retainsDefaultClassDirectory() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class))
                .withPropertyValues("gg.jte.development-mode=true")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(TemplateEngine.class);
                    assertThat(context.getBean(JteProperties.class).getClassDirectory()).isEqualTo("jte-classes");
                });
    }

    @Test
    void precompiledTemplatesIgnoreRuntimeClassDirectory() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class))
                .withPropertyValues("gg.jte.use-precompiled-templates=true", "gg.jte.class-directory=\0")
                .run(context -> assertThat(context).hasNotFailed().hasSingleBean(TemplateEngine.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rendersIntoConfiguredClassDirectory(boolean relativePath) throws Exception {
        Path templates = temporaryDirectory.resolve("templates");
        Files.createDirectories(templates);
        Files.writeString(templates.resolve("greeting.jte"), "@param String subject\nHello ${subject}!");
        Path classes = relativePath
                ? Path.of("target", "class-directory-tests", temporaryDirectory.getFileName().toString(), "compiled classes")
                : temporaryDirectory.resolve("compiled classes");

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JteAutoConfiguration.class))
                .withPropertyValues("gg.jte.development-mode=true",
                        "gg.jte.template-location=" + templates,
                        "gg.jte.class-directory=" + classes)
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(TemplateEngine.class);
                    TemplateEngine engine = context.getBean(TemplateEngine.class);
                    try {
                        StringOutput output = new StringOutput();
                        engine.render("greeting.jte", Map.of("subject", "world"), output);
                        assertThat(output.toString()).isEqualTo("Hello world!");
                        assertThat(classes).isDirectory();
                        try (Stream<Path> generatedFiles = Files.walk(classes)) {
                            assertThat(generatedFiles.anyMatch(path -> path.toString().endsWith(".class"))).isTrue();
                        }
                    } finally {
                        engine.cleanAll();
                    }
                });
    }
}
