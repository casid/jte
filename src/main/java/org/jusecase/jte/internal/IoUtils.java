package org.jusecase.jte.internal;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        return result.toString(StandardCharsets.UTF_8);
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

    @SuppressWarnings("unused") // Used by template code
    public static byte[] getUtf8Bytes(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public static int getUtf8Length(String string) {
        int result = 0;
        int length = string.length();

        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (c < 0x80) {
                // Have at most seven bits
                result += 1;
            } else if (c < 0x800) {
                // 2 bytes, 11 bits
                result += 2;
            } else if (Character.isSurrogate(c)) {
                // Have a surrogate pair
                result += 4;
                i += 1;
            } else {
                result += 3;
            }
        }

        return result;
    }

    public static void writeUtf8(String string, OutputStream outputStream) throws IOException {
        int length = string.length();

        for (int i = 0; i < length; i++) {
            char c = string.charAt(i);
            if (c < 0x80) {
                // Have at most seven bits
                outputStream.write((byte) c);
            } else if (c < 0x800) {
                // 2 bytes, 11 bits
                outputStream.write((byte)(0xc0 | (c >> 6)));
                outputStream.write((byte)(0x80 | (c & 0x3f)));
            } else if (Character.isSurrogate(c)) {
                // Have a surrogate pair
                int cp = Character.toCodePoint(c, string.charAt(++i));
                outputStream.write((byte)(0xf0 | ((cp >> 18))));
                outputStream.write((byte)(0x80 | ((cp >> 12) & 0x3f)));
                outputStream.write((byte)(0x80 | ((cp >>  6) & 0x3f)));
                outputStream.write((byte)(0x80 | (cp & 0x3f)));
            } else {
                // 3 bytes, 16 bits
                outputStream.write((byte)(0xe0 | ((c >> 12))));
                outputStream.write((byte)(0x80 | ((c >>  6) & 0x3f)));
                outputStream.write((byte)(0x80 | (c & 0x3f)));
            }
        }
    }
}
