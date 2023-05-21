package gg.jte.models.runtime;

import gg.jte.Content;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.output.Utf8ByteOutput;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface JteModel extends Content {
    default String render() {
        try (StringOutput output = new StringOutput()) {
            render(output);
            return output.toString();
        }
    }

    void render(TemplateOutput output);

}
