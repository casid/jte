package org.jusecase.jte.output;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.internal.IoUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public final class Utf8ArrayOutput implements TemplateOutput {
    private final List<Object> result = new ArrayList<>();
    private int contentLength;

    public int getContentLength() {
        return contentLength;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        for (Object data : result) {
            if (data instanceof byte[]) {
                outputStream.write((byte[]) data);
            } else {
                IoUtils.writeUtf8((String) data, outputStream);
            }
        }
    }

    @Override
    public void writeStaticContent(String value, byte[] bytes) {
        result.add(bytes);
        contentLength += bytes.length;
    }

    @Override
    public void writeContent(String value) {
        result.add(value);
        contentLength += IoUtils.getUtf8Length(value);
    }
}
