package gg.jte.convert.jsp;

import gg.jte.convert.IoUtils;
import gg.jte.convert.jsp.JspToJteConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JspToJteConverterTest {
    @TempDir
    Path tempDir;

    String usecase;
    Path jspRoot;
    Path jteRoot;

    @Test
    void simpleTag() {
        givenUsecase("simpleTag");
        whenJspTagIsConverted("simple.tag", "simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithIfStatement() {
        givenUsecase("simpleTagWithIfStatement");
        whenJspTagIsConverted("simple.tag", "simple.jte");
        thenConversionIsAsExpected();
    }

    void givenUsecase(String usecase) {
        jspRoot = tempDir.resolve("jsp");
        jteRoot = tempDir.resolve("jte");

        IoUtils.copyDirectory(Path.of("testdata", usecase, "before"), tempDir);

        this.usecase = usecase;
    }

    void whenJspTagIsConverted(String jspTag, String jteTag) {
        JspToJteConverter converter = new JspToJteConverter(jspRoot, jteRoot);
        converter.convertTag(jspTag, jteTag);
    }

    private void thenConversionIsAsExpected() {
        Path expected = Path.of("testdata", usecase, "after");
        Path actual = tempDir;

        try (Stream<Path> stream = Files.walk(expected)) {
            stream.filter(p -> !Files.isDirectory(p)).forEach(expectedFile -> {
                Path actualFile = actual.resolve(expected.relativize(expectedFile));
                assertThat(actualFile).exists();
                assertThat(actualFile).hasSameTextualContentAs(expectedFile);
            });
        } catch (IOException e){
            throw new UncheckedIOException(e);
        }
    }
}