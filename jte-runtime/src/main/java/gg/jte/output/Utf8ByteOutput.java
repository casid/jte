package gg.jte.output;

import gg.jte.TemplateOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * UTF-8 template output, designed to be CPU and memory friendly.
 * You may want to use this class, if you write to a low-level binary output stream, and you need the exact content-size of the output.
 *
 * CAUTION: You must enable {@link gg.jte.TemplateEngine#setBinaryStaticContent(boolean)}, otherwise this class won't provide any benefits over {@link StringOutput}!
 */
public final class Utf8ByteOutput implements TemplateOutput {

    private final List<byte[]> chunks = new ArrayList<>();

    /**
     * @return The amount of bytes written to this output.
     */
    public int getContentLength() {
        int contentLength = 0;

        for (byte[] chunk : chunks) {
            contentLength += chunk.length;
        }

        return contentLength;
    }

    /**
     * Passes all collected bytes to the given output stream. Does not close the stream.
     * @param os the output stream
     * @throws IOException in case the stream operation fails
     */
    public void writeTo(OutputStream os) throws IOException {
        writeTo(os::write);
    }

    /**
     * Passes all collected bytes to the given data consumer.
     * CAUTION: For performance reasons no copy of the byte arrays is made. It is the consumer's duty to never alter their content!
     * @param dataConsumer the data consumer
     * @throws IOException in case the consume operation fails
     */
    public void writeTo(DataConsumer dataConsumer) throws IOException {
        for (byte[] chunk : chunks) {
            dataConsumer.accept(chunk, 0, chunk.length);
        }
    }

    @Override
    public void writeContent(String s) {
        chunks.add(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void writeContent(String s, int beginIndex, int endIndex) {
        chunks.add(s.substring(beginIndex, endIndex).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void writeBinaryContent(byte[] value) {
        chunks.add(value);
    }

    @FunctionalInterface
    public interface DataConsumer {
        void accept(byte[] bytes, int startIndex, int length) throws IOException;
    }
}
