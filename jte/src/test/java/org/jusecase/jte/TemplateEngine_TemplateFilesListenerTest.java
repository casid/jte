package org.jusecase.jte;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.jte.internal.IoUtils;
import org.jusecase.jte.output.FileOutput;
import org.jusecase.jte.output.StringOutput;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngine_TemplateFilesListenerTest {

    public static final String TEMPLATE = "test.jte";

    private final Set<String> templatesInvalidated = new LinkedHashSet<>();

    private Path tempDirectory;
    private DirectoryCodeResolver codeResolver;
    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("temp-code");
        codeResolver = new DirectoryCodeResolver(tempDirectory);
        templateEngine = TemplateEngine.create(codeResolver);

        givenHotReloadIsActivated();
    }

    @AfterEach
    void tearDown() {
        codeResolver.stopTemplateFilesListener();
        IoUtils.deleteDirectoryContent(tempDirectory);
    }

    @Test
    void template() {
        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!");
        thenTemplateOutputIs("Hello hot reload!");

        whenFileIsWritten(TEMPLATE, "@param String name\nHello ${name}!!!");
        waitForTemplateInvalidation();
        thenListenerIsCalledWith(TEMPLATE);
        thenTemplateOutputIs("Hello hot reload!!!");
    }

    @Test
    void tagUsedByTemplate() {
        whenFileIsWritten("tag/name.jte", "@param String name\nHello ${name}!");
        whenFileIsWritten(TEMPLATE, "@param String name\n@tag.name(name)");
        thenTemplateOutputIs("Hello hot reload!");

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

    private void givenHotReloadIsActivated() {
        codeResolver.startTemplateFilesListener(templateEngine, this::onTemplatesInvalidated);
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