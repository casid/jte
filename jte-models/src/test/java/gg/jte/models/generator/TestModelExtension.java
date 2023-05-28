package gg.jte.models.generator;

import gg.jte.extension.JteExtension;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gg.jte.extension.MockParamDescription.mockParamDescription;
import static org.assertj.core.api.Assertions.assertThat;

import static gg.jte.extension.MockConfig.mockConfig;
import static gg.jte.extension.MockTemplateDescription.mockTemplateDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestModelExtension {
    public static final Pattern INTERFACE_SOURCE_FILE = Pattern.compile("target.generated-test-sources.test.mytemplates.Templates.java");
    JteExtension modelExtension = new ModelExtension();
    private static final String TEST_PACKAGE = "test.mytemplates";

    @Test
    public void simpleTest() {
        Collection<Path> generatedPaths =
                modelExtension.generate(
                        mockConfig()
                                .generatedSourcesRoot(Paths.get("target/generated-test-sources"))
                                .packageName(TEST_PACKAGE),
                        Collections.singleton(
                                mockTemplateDescription()
                                        .packageName(TEST_PACKAGE)
                                        .name("hello.jte")
                                        .className("JtehelloGenerated")
                                        .addParams(mockParamDescription()
                                                .type("java.lang.String")
                                                .name("message"))
                        )
                );

        assertEquals(3, generatedPaths.size());
        Collection<String> pathStrings = generatedPaths.stream().map(Path::toString).collect(Collectors.toList());
        assertThat(pathStrings).anyMatch(p -> INTERFACE_SOURCE_FILE.matcher(p).find());
        generatedPaths.forEach(path -> {
            try (Stream<String> lines = Files.lines(path)) {
                assertThat(lines).anyMatch(line -> line.matches(".*JteModel hello\\(.*"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
