package org.jusecase.jte.output;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class Utf8StreamOutputTest {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Utf8StreamOutput output = new Utf8StreamOutput(outputStream);

    @Test
    void test() {
        assertEncoding("Hello world");
        assertEncoding("Äöü-µ");
        assertEncoding("你好，世界");
        assertEncoding("你好，世界 \uD83D\uDE02\uD83D\uDE02\uD83D\uDE02");
    }

    @Test
    void binary() {
        output.writeStaticContent(null, "<h1>Hello ".getBytes(StandardCharsets.UTF_8));
        output.writeUnsafeContent("你好，世界");
        output.writeStaticContent(null, "</h1>".getBytes(StandardCharsets.UTF_8));

        String result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertThat(result).isEqualTo("<h1>Hello 你好，世界</h1>");
    }

    private void assertEncoding(String expected) {
        output.writeSafeContent(expected);
        String result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertThat(result).isEqualTo(expected);
        outputStream.reset();
    }


}