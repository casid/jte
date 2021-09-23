package gg.jte;

import gg.jte.compiler.IoUtils;
import gg.jte.output.FileOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngine_HotReloadTest {

    public static final String TEMPLATE = "test.jte";

    private Path tempDirectory;
    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("temp-code");
        templateEngine = TemplateEngine.create(new DirectoryCodeResolver(tempDirectory), ContentType.Plain);
    }

    @AfterEach
    void tearDown() {
        IoUtils.deleteDirectoryContent(tempDirectory);
    }

    @Test
    void template() {
        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!");
        thenTemplateOutputIs("Hello hot reload!");

        TestUtils.sleepIfLegacyJavaVersion(1000); // File.getLastModified() only has seconds precision on most Java 8 versions

        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!!!");
        thenTemplateOutputIs("Hello hot reload!!!");
    }

    @Test
    void tagUsedByTemplate() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        TestUtils.sleepIfLegacyJavaVersion(1000); // File.getLastModified() only has seconds precision on most Java 8 versions

        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!!!");
        thenTemplateOutputIs("Hello hot reload!!!");
    }

    @Test
    void tagUsedByTemplates() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten("test1.jte", "@param String name\ntest1: @tag.name(name)");
        whenFileIsWritten("test2.jte", "@param String name\ntest2: @tag.name(name)");
        thenTemplateOutputIs("test1.jte", "test1: Hello hot reload!");
        thenTemplateOutputIs("test2.jte", "test2: Hello hot reload!");

        TestUtils.sleepIfLegacyJavaVersion(1000); // File.getLastModified() only has seconds precision on most Java 8 versions

        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!!!");
        thenTemplateOutputIs("test1.jte", "test1: Hello hot reload!!!");
        thenTemplateOutputIs("test2.jte", "test2: Hello hot reload!!!");
    }

    private void thenTemplateOutputIs(String expected) {
        thenTemplateOutputIs(TEMPLATE, expected);
    }

    private void thenTemplateOutputIs(String name, String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(name, "hot reload", output);
        assertThat(output.toString()).isEqualTo(expected);
    }

    private void whenFileIsWritten(String name, String content) {
        try (FileOutput fileOutput = new FileOutput(tempDirectory.resolve(name))) {
            fileOutput.writeContent(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}