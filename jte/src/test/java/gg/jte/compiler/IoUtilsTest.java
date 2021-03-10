package gg.jte.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class IoUtilsTest {
    @Test
    void shouldIdentifyTemplateFiles() {
        Assertions.assertTrue(IoUtils.isTemplateFile("foo.jte"));
        Assertions.assertTrue(IoUtils.isTemplateFile("foo.kte"));
        Assertions.assertFalse(IoUtils.isTemplateFile("foo"));
        Assertions.assertFalse(IoUtils.isTemplateFile(""));
    }

    @Test
    void shouldConvertStreamToString() throws IOException {
        Assertions.assertEquals("hello", IoUtils.toString(
                new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)))
        );
        Assertions.assertEquals("", IoUtils.toString(
                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)))
        );
    }

    @Test
    void shouldDeleteDirectories() throws IOException {
        Path tempDir  = Files.createTempDirectory(null);
        IoUtils.deleteDirectoryContent(tempDir);

        // non existing directory:
        IoUtils.deleteDirectoryContent(tempDir);
    }
}