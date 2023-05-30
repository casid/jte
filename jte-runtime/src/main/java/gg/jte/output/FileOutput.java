package gg.jte.output;

import gg.jte.TemplateOutput;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileOutput implements TemplateOutput, Closeable {

    private final BufferedWriter writer;

    public FileOutput(Path file) throws IOException {
        this(file, StandardCharsets.UTF_8);
    }

    public FileOutput(Path file, Charset charset) throws IOException {
        Files.createDirectories(file.getParent());
        writer = Files.newBufferedWriter(file, charset, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    @Override
    public void writeContent(String value) {
        try {
            writer.write(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeContent(String value, int beginIndex, int endIndex) {
        try {
            writer.write(value, beginIndex, endIndex - beginIndex);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
