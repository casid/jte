package gg.jte.output;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class WriterOutputTest {
    @Test
    void test() {
        StringWriter writer = new StringWriter();
        WriterOutput output = new WriterOutput(writer);

        output.writeContent("Hello world");

        assertThat(writer.toString()).isEqualTo("Hello world");
    }
}