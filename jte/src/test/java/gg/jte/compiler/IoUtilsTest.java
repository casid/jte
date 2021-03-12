package gg.jte.compiler;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class IoUtilsTest {
    @Test
    void shouldIdentifyTemplateFiles() {
        assertThat(IoUtils.isTemplateFile("foo.jte")).isTrue();
        assertThat(IoUtils.isTemplateFile("foo.kte")).isTrue();
        assertThat(IoUtils.isTemplateFile("foo")).isFalse();
        assertThat(IoUtils.isTemplateFile("")).isFalse();
    }

    @Test
    void shouldConvertStreamToString() throws IOException {
        assertThat(IoUtils.toString(new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8))))
                .isEqualTo("hello");
        assertThat(IoUtils.toString(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))))
                .isEqualTo("");
    }

    @Test
    void shouldDeleteDirectories() throws IOException {
        Path tempDir = Files.createTempDirectory(null);
        IoUtils.deleteDirectoryContent(tempDir);

        // non existing directory:
        IoUtils.deleteDirectoryContent(tempDir);
    }
}