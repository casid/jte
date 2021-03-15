package gg.jte.output;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class WriterOutputTest extends AbstractTemplateOutputTest<WriterOutput> {

    private StringWriter writer;

    @Override
    WriterOutput createTemplateOutput() {
        writer = new StringWriter();
        return new WriterOutput(writer);
    }

    @Override
    void thenOutputIs(String expected) {
        assertThat(writer.toString()).isEqualTo(expected);
    }
}