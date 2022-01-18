package gg.jte.watcher;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DirectoryWatcherTest {
    static final String TEMPLATE = "test.jte";

    final Set<String> templatesInvalidated = new LinkedHashSet<>();

    Path tempDirectory;
    DirectoryCodeResolver codeResolver;
    TemplateEngine templateEngine;
    DirectoryWatcher watcher;

    @BeforeEach
    void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("temp-code");
        codeResolver = new DirectoryCodeResolver(tempDirectory);
        templateEngine = TemplateEngine.create(codeResolver, ContentType.Plain);

        watcher = new DirectoryWatcher(templateEngine, codeResolver);

        watcher.start(this::onTemplatesInvalidated);
        Thread.sleep(100); // Give the listener thread some time to start
    }

    @AfterEach
    void tearDown() {
        watcher.stop();
        IoUtils.deleteDirectoryContent(tempDirectory);
    }

    @Test
    void template() {
        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!");
        thenTemplateOutputIs("Hello hot reload!");

        TestUtils.sleepIfLegacyJavaVersion(1000); // File.getLastModified() only has seconds precision on most Java 8 versions

        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!!!");
        waitForTemplateInvalidation();
        thenListenerIsCalledWith(TEMPLATE);
        thenTemplateOutputIs("Hello hot reload!!!");
    }

    @Test
    void tagUsedByTemplate() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@template.tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

        TestUtils.sleepIfLegacyJavaVersion(1000); // File.getLastModified() only has seconds precision on most Java 8 versions

        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!!!");
        waitForTemplateInvalidation();
        thenListenerIsCalledWith(TEMPLATE);
        thenTemplateOutputIs("Hello hot reload!!!");
    }

    private void waitForTemplateInvalidation() {
        try {
            synchronized (templatesInvalidated) {
                templatesInvalidated.wait(5000);
            }
        } catch (InterruptedException e) {
            // ignored
        }
    }

    private void thenTemplateOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(TEMPLATE, "hot reload", output);
        assertThat(output.toString()).isEqualTo(expected);
    }

    private void thenListenerIsCalledWith(String ... invalidatedTemplateNames) {
        assertThat(templatesInvalidated).containsExactlyInAnyOrder(invalidatedTemplateNames);
    }

    private void whenFileIsWritten(String name, String content) {
        synchronized (templatesInvalidated) {
            try (FileOutput fileOutput = new FileOutput(tempDirectory.resolve(name))) {
                fileOutput.writeContent(content);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void onTemplatesInvalidated(List<String> names) {
        synchronized (templatesInvalidated) {
            templatesInvalidated.addAll(names);
            templatesInvalidated.notifyAll();
        }
    }
}