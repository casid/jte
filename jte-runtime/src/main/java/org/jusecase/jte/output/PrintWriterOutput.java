package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;

import java.io.PrintWriter;
import java.io.Writer;

public class PrintWriterOutput implements TemplateOutput {
    private final PrintWriter writer;

    public PrintWriterOutput(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public Writer getWriter() {
        return writer;
    }

    @Override
    public void writeContent(String value) {
        if (value != null) {
            writer.write(value);
        }
    }
}
