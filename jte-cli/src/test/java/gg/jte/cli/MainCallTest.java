package gg.jte.cli;

import gg.jte.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MainCallTest {

    @Test
    void printsFriendlyErrorWhenSourceDirectoryDoesNotExist(@TempDir Path tempDir) {
        Main main = new Main();
        main.sourceDirectory = tempDir.resolve("does-not-exist");
        main.targetDirectory = tempDir.resolve("generated");
        main.contentType = ContentType.Plain;

        String err = callAndCaptureStderr(main);

        assertThat(err).contains("source directory").contains(main.sourceDirectory.toString()).contains("does not exist");
        assertThat(err).doesNotContain("Exception");
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void printsFriendlyErrorWhenTargetDirectoryIsNotWritable(@TempDir Path tempDir) throws IOException {
        Path sourceDirectory = tempDir.resolve("templates");
        Files.createDirectories(sourceDirectory);
        Files.writeString(sourceDirectory.resolve("hello.jte"), "Hello!");

        Path readOnlyDirectory = tempDir.resolve("readonly");
        Files.createDirectories(readOnlyDirectory);
        assertThat(readOnlyDirectory.toFile().setWritable(false)).isTrue();

        try {
            Main main = new Main();
            main.sourceDirectory = sourceDirectory;
            main.targetDirectory = readOnlyDirectory.resolve("generated");
            main.contentType = ContentType.Plain;

            String err = callAndCaptureStderr(main);

            assertThat(err).contains("target directory").contains(main.targetDirectory.toString());
            assertThat(err).doesNotContain("Exception");
        } finally {
            readOnlyDirectory.toFile().setWritable(true);
        }
    }

    private static String callAndCaptureStderr(Main main) {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(err));
        int exitCode;
        try {
            exitCode = main.call();
        } finally {
            System.setErr(originalErr);
        }

        assertThat(exitCode).isEqualTo(1);
        return err.toString();
    }
}
