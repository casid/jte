package gg.jte.compiler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

public final class IoUtils {

    private IoUtils() {
    }

    public static String toString(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public static String toString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    public static void deleteDirectoryContent(Path directory) {
        Objects.requireNonNull(directory);
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> pathStream = Files.walk(directory)) {
            //noinspection ResultOfMethodCallIgnored
            pathStream.filter(d -> d != directory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isTemplateFile(String name) {
        return name.endsWith(".jte") || name.endsWith(".kte");
    }
}
