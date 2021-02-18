package gg.jte.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@SuppressWarnings("unused") // By generated template code
public final class BinaryContent {

    public static BinaryContent load(Class<?> templateClass, String resource, int ... lengths) {
        int total = lengths.length;

        byte[][] data = new byte[total][];

        try (InputStream is = templateClass.getResourceAsStream(resource)) {
            for (int i = 0; i < total; ++i) {
                data[i] = read(is, lengths[i]);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return new BinaryContent(data);
    }

    private static byte[] read(InputStream is, int length) throws IOException {
        byte[] result = new byte[length];

        int offset = 0;
        while (offset < length) {
            int read = is.read(result, offset, length - offset);
            if (read == -1) {
                return result;
            } else {
                offset += read;
            }
        }

        return result;
    }


    private final byte[][] data;

    public BinaryContent(byte[][] data) {
        this.data = data;
    }

    public byte[] get(int index) {
        return data[index];
    }
}
