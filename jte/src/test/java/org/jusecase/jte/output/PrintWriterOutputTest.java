package org.jusecase.jte.output;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class PrintWriterOutputTest {
    @Test
    void test() {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        PrintWriterOutput output = new PrintWriterOutput(printWriter);

        output.writeStaticContent("Hello world");

        assertThat(writer.toString()).isEqualTo("Hello world");
    }
}