package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;

import java.io.PrintWriter;

public class PrintWriterOutput implements TemplateOutput {
    private final PrintWriter writer;

    public PrintWriterOutput(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void writeContent(String value) {
        writer.write(value);
    }
}
