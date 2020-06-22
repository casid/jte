package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.internal.IoUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Utf8FileOutput implements TemplateOutput, Closeable {

    private final OutputStream outputStream;

    public Utf8FileOutput(Path path) throws IOException {
        outputStream = Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    @Override
    public void writeSafeContent(String value) {
        try {
            IoUtils.writeUtf8(value, outputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeStaticContent(String value, byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
