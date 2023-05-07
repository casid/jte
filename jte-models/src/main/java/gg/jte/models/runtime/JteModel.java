package gg.jte.models.runtime;

import gg.jte.Content;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface JteModel extends Content {
    default void render(OutputStream outputStream, Charset charset) {
        Writer writer = new OutputStreamWriter(outputStream, charset);
        render(writer);
    }

    default void render(OutputStream outputStream) {
        render(outputStream, StandardCharsets.UTF_8);
    }

    default String render() {
        Writer writer = new StringWriter();
        render(writer);
        return writer.toString();
    }

    void render(Writer writer);

}
