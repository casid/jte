package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.internal.IoUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public final class Utf8StreamOutput implements TemplateOutput {
    private final OutputStream outputStream;

    public Utf8StreamOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void writeContent(String value) {
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
}
