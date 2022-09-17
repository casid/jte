package gg.jte.output;

import gg.jte.compiler.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class FileOutputTest extends AbstractTemplateOutputTest<FileOutput> {

    Path outputFile;

    @Override
    FileOutput createTemplateOutput() {
        try {
            File tempFile = File.createTempFile("file-output", "txt");
            tempFile.deleteOnExit();

            outputFile = tempFile.toPath();
            return new FileOutput(outputFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    void thenOutputIs(String expected) {
        try {
            output.close(); // flushes written data to disk
            assertThat(IoUtils.toString(outputFile)).isEqualTo(expected);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
