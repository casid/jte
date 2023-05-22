package gg.jte.output;

import gg.jte.TemplateOutput;

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

    @Override
    public void writeContent(String value, int beginIndex, int endIndex) {
        writer.write(value, beginIndex, endIndex - beginIndex);
    }
}
