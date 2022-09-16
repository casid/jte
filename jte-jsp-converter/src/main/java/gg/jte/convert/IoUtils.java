package gg.jte.convert;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

public final class IoUtils {

    private IoUtils() {
    }

    public static String readFile(Path file) {
        try {
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void writeFile(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copyDirectory(Path src, Path dest) {
        try {
            try (Stream<Path> stream = Files.walk(src)) {
                stream.forEach(f -> copy(f, dest.resolve(src.relativize(f))));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void copy(Path src, Path dst) {
        try {
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteFile(Path file) {
        try {
            Files.delete(file);
        } catch ( NoSuchFileException e ) {
            // Okay, it is already deleted.
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
