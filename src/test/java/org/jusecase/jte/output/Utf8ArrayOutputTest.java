package org.jusecase.jte.output;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class Utf8ArrayOutputTest {
    @Test
    void test() {
        assertEncoding("Hello world", 11);
        assertEncoding("你好，世界", 15);
        assertEncoding("你好，世界 \uD83D\uDE02\uD83D\uDE02\uD83D\uDE02", 28);
    }

    private void assertEncoding(String expected, int expectedContentLength) {
        Utf8ArrayOutput output = new Utf8ArrayOutput();
        output.writeSafeContent(expected);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            output.writeTo(outputStream);

            String result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            assertThat(result).isEqualTo(expected);
            assertThat(output.getContentLength()).isEqualTo(expectedContentLength);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}