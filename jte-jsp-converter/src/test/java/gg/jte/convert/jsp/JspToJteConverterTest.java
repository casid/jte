package gg.jte.convert.jsp;

import gg.jte.convert.IoUtils;
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
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithIfStatement() {
        givenUsecase("simpleTagWithIfStatement");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithChooseStatement() {
        givenUsecase("simpleTagWithChooseStatement");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithVariable() {
        givenUsecase("simpleTagWithVariable");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithFormatMessage() {
        givenUsecase("simpleTagWithFormatMessage");
        whenJspTagIsConverted("simple.tag", "tag/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleTagWithUsages() {
        givenUsecase("simpleTagWithUsages");
        whenJspTagIsConverted("my/simple.tag", "tag/my/simple.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleJspWithAlreadyConvertedTag() {
        givenUsecase("simpleJspWithAlreadyConvertedTag");
        whenJspTagIsConverted("usage.jsp", "usage.jte");
        thenConversionIsAsExpected();
    }

    @Test
    void simpleJspWithNotYetConvertedTag() {
        // TODO implement
    }

    void givenUsecase(String usecase) {
        jspRoot = tempDir.resolve("jsp");
        jteRoot = tempDir.resolve("jte");

        IoUtils.copyDirectory(Path.of("testdata", usecase, "before"), tempDir);

        this.usecase = usecase;
    }

    void whenJspTagIsConverted(String jspTag, String jteTag) {
        JspToJteConverter converter = new JspToJteConverter(jspRoot, jteRoot, "my:jte");
        converter.setDefaultImports("@import static example.JteContext.*\n");
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