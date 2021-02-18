package gg.jte.output;

import gg.jte.TemplateOutput;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class PrintWriterOutputTest extends AbstractTemplateOutputTest<PrintWriterOutput> {

    private StringWriter writer;

    @Override
    PrintWriterOutput createTemplateOutput() {
        writer = new StringWriter();
        return new PrintWriterOutput(new PrintWriter(writer));
    }

    @Override
    void thenOutputIs(String expected) {
        assertThat(writer.toString()).isEqualTo(expected);
    }
}