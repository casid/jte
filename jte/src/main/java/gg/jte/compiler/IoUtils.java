package gg.jte.compiler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class IoUtils {

    public static String toString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    public static void deleteDirectoryContent(Path directory) {
        if (!Files.exists(directory)) {
            return;
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            Files.walk(directory)
                    .filter(d -> d != directory)
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
