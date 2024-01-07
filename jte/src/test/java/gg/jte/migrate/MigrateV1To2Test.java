package gg.jte.migrate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrateV1To2Test {

    private final Path sourceDirectory = Paths.get("src/test/resources/migrate/v1To2");

    @Test
    void name(@TempDir Path tempDir) throws IOException {
        copyTemplatesTo(tempDir);

        MigrateV1To2.migrateTemplates(tempDir);

        assertThat(tempDir.resolve("template.jte")).hasContent("@template.tag.foo()\n@template.layout.foo()");
        assertThat(tempDir.resolve("template.kte")).hasContent("@template.tag.foo()\n@template.layout.foo()");
    }

    private void copyTemplatesTo(Path tempDir) throws IOException {
        List<Path> templates = Files.walk(sourceDirectory).filter(p -> p != sourceDirectory).toList();
        for (Path template : templates) {
            Files.copy(template, tempDir.resolve(template.getFileName()));
        }
    }
}