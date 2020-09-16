package gg.jte.output;

import gg.jte.TemplateOutput;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class WriterOutputTest extends AbstractTemplateOutputTest {

    private StringWriter writer;

    @Override
    TemplateOutput createTemplateOutput() {
        writer = new StringWriter();
        return new WriterOutput(writer);
    }

    @Override
    void thenOutputIs(String expected) {
        assertThat(writer.toString()).isEqualTo(expected);
    }
}