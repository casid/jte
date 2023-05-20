package gg.jte.output;

import gg.jte.TemplateOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Heavily optimized UTF-8 template output, designed to be CPU and memory friendly.
 * You may want to use this class, if you write to a low-level binary output stream, and you need the exact content-size of the output.
 *
 * CAUTION: You must enable {@link gg.jte.TemplateEngine#setBinaryStaticContent(boolean)}, otherwise this class won't provide any benefits over {@link StringOutput}!
 */
public final class Utf8ByteOutput extends Writer implements TemplateOutput {

    private final int chunkSize;

    private final char[] tempBuffer;
    private final int tempBufferSize;

    private ArrayList<Chunk> chunks;
    private byte[] buffer;
    private int lastIndex;
    private int currentIndex;

    private char highSurrogate;

    /**
     * Constructs an output with sane defaults
     */
    public Utf8ByteOutput() {
        this(1024, 512);
    }

    /**
     * Constructs an output with custom settings.
     * This output maintains a list of binary chunks. Pre-encoded data is passed as is, while internal buffers are created for dynamic data as needed.
     * @param chunkSize The size in bytes for chunks of dynamic data.
     * @param tempBufferSize The size for the temporary buffer used for intermediate String encoding.
     */
    public Utf8ByteOutput(int chunkSize, int tempBufferSize) {
        this.chunkSize = chunkSize;
        buffer = new byte[this.chunkSize];

        this.tempBufferSize = tempBufferSize;
        tempBuffer = new char[tempBufferSize];
    }

    /**
     * @return The amount of bytes written to this output.
     */
    public int getContentLength() {
        int contentLength = currentIndex - lastIndex;

        if (chunks != null) {
            for (Chunk chunk : chunks) {
                contentLength += chunk.length;
            }
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
        if (chunks != null) {
            for (Chunk chunk : chunks) {
                dataConsumer.accept(chunk.data, chunk.startIndex, chunk.length);
            }
        }

        int remaining = currentIndex - lastIndex;
        if (remaining > 0) {
            dataConsumer.accept(buffer, lastIndex, remaining);
        }
    }

    @Override
    public Writer getWriter() {
        return this;
    }

    @Override
    public void writeContent(String s) {
        int len = s.length();
        for (int i = 0; i < len; i += tempBufferSize) {
            int size = Math.min(tempBufferSize, len - i);
            s.getChars(i, i + size, tempBuffer, 0);
            write(tempBuffer, 0, size);
        }
    }

    @Override
    public void writeBinaryContent(byte[] value) {
        if (value.length < 16) {
            doAppend(value); // Don't waste chunks if array is very small.
        } else {
            if (lastIndex < currentIndex) {
                addCurrentChunk();
                lastIndex = currentIndex;
            }

            addChunk(new Chunk(value, 0, value.length));
        }
    }

    @Override
    public void writeUserContent(boolean value) {
        appendLatin1(String.valueOf(value));
    }

    @Override
    public void writeUserContent(byte value) {
        appendLatin1(Byte.toString(value));
    }

    @Override
    public void writeUserContent(char value) {
        appendUtf8Char(value);
    }

    @Override
    public void writeUserContent(int value) {
        appendLatin1(Integer.toString(value));
    }

    @Override
    public void writeUserContent(long value) {
        appendLatin1(Long.toString(value));
    }

    @Override
    public void writeUserContent(float value) {
        appendLatin1(Float.toString(value));
    }

    @Override
    public void writeUserContent(double value) {
        appendLatin1(Double.toString(value));
    }

    // Writer interface

    @Override
    public void write(@SuppressWarnings("NullableProblems") char[] buffer, int off, int len) {
        int i = off;
        len += off;

        while (i < len) {
            write(buffer[i++]);
        }
    }

    public void write(char c) {
        if (highSurrogate != 0) {
            if (Character.isLowSurrogate(c)) {
                appendUtf8CodePoint(Character.toCodePoint(highSurrogate, c));
            } else {
                doAppend((byte)('�'));
                appendUtf8Char(c);
            }
            highSurrogate = 0;
        } else if (Character.isHighSurrogate(c)) {
            highSurrogate = c;
        } else {
            appendUtf8Char(c);
        }
    }

    @Override
    public void write(int c) {
        write((char) c);
    }

    @Override
    public void write(@SuppressWarnings("NullableProblems") char[] buffer) {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(@SuppressWarnings("NullableProblems") String str) {
        writeContent(str);
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void close() {
        // nothing to do
    }

    private void appendLatin1(String s) {
        int len = s.length();

        for (int i = 0; i < len; ++i) {
            doAppend((byte) s.charAt(i));
        }
    }

    private void appendUtf8Char(char c) {
        if (c < 0x80) {
            doAppend((byte) c);
        } else if (c < 0x800) {
            doAppend((byte) (0xc0 | c >> 6));
            doAppend((byte) (0x80 | c & 0x3f));
        } else {
            doAppend((byte)(0xe0 | (c >> 12)));
            doAppend((byte)(0x80 | ((c >> 6) & 0x3f)));
            doAppend((byte)(0x80 | (c & 0x3f)));
        }
    }

    private void appendUtf8CodePoint(int c) {
        doAppend((byte)(0xf0 | (c >> 18)));
        doAppend((byte)(0x80 | ((c >> 12) & 0x3f)));
        doAppend((byte)(0x80 | ((c >> 6) & 0x3f)));
        doAppend((byte)(0x80 | (c & 0x3f)));
    }

    private void doAppend(byte[] bytes) {
        int length = bytes.length;

        if (currentIndex + length < chunkSize) {
            System.arraycopy(bytes, 0, buffer, currentIndex, length);
            currentIndex += length;
        } else {
            for (byte b : bytes) {
                doAppend(b);
            }
        }
    }

    private void doAppend(byte b) {
        if (currentIndex == chunkSize) {
            createNewChunk();
        }
        buffer[currentIndex++] = b;
    }

    private void createNewChunk() {
        addCurrentChunk();

        buffer = new byte[chunkSize];
        lastIndex = 0;
        currentIndex = 0;
    }

    private void addCurrentChunk() {
        addChunk(new Chunk(buffer, lastIndex, currentIndex - lastIndex));
    }

    private void addChunk(Chunk chunk) {
        if (chunks == null) {
            chunks = new ArrayList<>();
        }
        chunks.add(chunk);
    }

    private static final class Chunk {
        final byte[] data;
        final int startIndex;
        final int length;

        public Chunk(byte[] data, int startIndex, int length) {
            this.data = data;
            this.startIndex = startIndex;
            this.length = length;
        }
    }

    @FunctionalInterface
    public interface DataConsumer {
        void accept(byte[] bytes, int startIndex, int length) throws IOException;
    }
}
