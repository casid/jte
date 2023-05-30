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
import static org.assertj.core.api.Assertions.catchThrowable;

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

        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!!!");
        thenTemplateOutputIs("Hello hot reload!!!");
    }

    @Test
    void tagUsedByTemplate() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!!!");
        thenTemplateOutputIs("Hello hot reload!!!");
    }

    @Test
    void tagUsedByTemplates() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten("test1.jte", "@param String name\ntest1: @template.tag.name(name)");
        whenFileIsWritten("test2.jte", "@param String name\ntest2: @template.tag.name(name)");
        thenTemplateOutputIs("test1.jte", "test1: Hello hot reload!");
        thenTemplateOutputIs("test2.jte", "test2: Hello hot reload!");

        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!!!");
        thenTemplateOutputIs("test1.jte", "test1: Hello hot reload!!!");
        thenTemplateOutputIs("test2.jte", "test2: Hello hot reload!!!");
    }

    @Test
    void newDependencyAdded() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten("tag/nested.jte", "@param String name\nnested ${name}");
        whenFileIsWritten("tag/name.jte", "@param String name\nHello @template.tag.nested(name)!!!");

        thenTemplateOutputIs("Hello nested hot reload!!!");

        whenFileIsWritten("tag/nested.jte", "@param String name\nnested, ${name}");

        thenTemplateOutputIs("Hello nested, hot reload!!!");
    }

    @Test
    void anotherDependencyAdded() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten("tag/nested.jte", "@param String name\nnested ${name}");
        whenFileIsWritten("tag/name.jte", "@param String name\nHello @template.tag.nested(name)!!!");

        thenTemplateOutputIs("Hello nested hot reload!!!");

        whenFileIsWritten("tag/nested.jte", "@param String name\nnested, ${name}");

        thenTemplateOutputIs("Hello nested, hot reload!!!");

        whenFileIsWritten("tag/beforeNested.jte", "@param String name\nbefore ${name}.");
        whenFileIsWritten("tag/name.jte", "@param String name\nHello @template.tag.beforeNested(name) @template.tag.nested(name)!!!");

        thenTemplateOutputIs("Hello before hot reload. nested, hot reload!!!");

        whenFileIsWritten("tag/beforeNested.jte", "@param String name\nbefore2 ${name}.");

        thenTemplateOutputIs("Hello before2 hot reload. nested, hot reload!!!");
    }

    @Test
    void newDependencyAddedAndDeleted() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten("tag/nested.jte", "@param String name\nnested ${name}");
        whenFileIsWritten("tag/name.jte", "@param String name\nHello @template.tag.nested(name)!!!");

        thenTemplateOutputIs("Hello nested hot reload!!!");

        whenFileIsWritten("tag/nested.jte", "@param String name\nnested, ${name}");

        thenTemplateOutputIs("Hello nested, hot reload!!!");

        whenFileIsDeleted("tag/nested.jte");
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!!!");

        thenTemplateOutputIs("Hello hot reload!!!");
    }

    @Test
    void paramWithDefaultValueAdded() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten("tag/name.jte", "@param String name\n@param String suffix = \"?!\"\nHello ${name}${suffix}");
        thenTemplateOutputIs("Hello hot reload?!");
    }

    @Test
    void binaryContent_oneCharacterReplaced() {
        templateEngine.setBinaryStaticContent(true);
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}?");
        thenTemplateOutputIs("Hello hot reload?");
    }

    @Test
    void cacheIsNotUsedAfterCompilationError() {
        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name2}");
        thenCompilationFails();
        thenCompilationFails(); // Should continue failing and not use an old template from the cache
    }

    @Test
    void clearCache() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        templateEngine.clearCache();
        thenTemplateOutputIs("Hello hot reload!");
    }

    private void thenTemplateOutputIs(String expected) {
        thenTemplateOutputIs(TEMPLATE, expected);
    }

    private void thenTemplateOutputIs(String name, String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(name, "hot reload", output);
        assertThat(output.toString()).isEqualTo(expected);
    }

    private void thenCompilationFails() {
        Throwable throwable = catchThrowable(() -> thenTemplateOutputIs("ignored"));
        assertThat(throwable).isInstanceOf(TemplateException.class);
    }

    private void whenFileIsWritten(String name, String content) {
        try (FileOutput fileOutput = new FileOutput(tempDirectory.resolve(name))) {
            fileOutput.writeContent(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void whenFileIsDeleted(String name) {
        try {
            Files.delete(tempDirectory.resolve(name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}