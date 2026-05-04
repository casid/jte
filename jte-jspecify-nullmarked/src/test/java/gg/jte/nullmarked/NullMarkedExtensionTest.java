package gg.jte.nullmarked;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import static gg.jte.extension.api.mocks.MockConfig.mockConfig;
import static gg.jte.extension.api.mocks.MockTemplateDescription.mockTemplateDescription;
import static org.assertj.core.api.Assertions.assertThat;

class NullMarkedExtensionTest {

    private final NullMarkedExtension extension = new NullMarkedExtension();

    @TempDir
    Path tempDir;

    @Test
    void singlePackage() throws IOException {
        var config = mockConfig().generatedSourcesRoot(tempDir).packageName("com.example");
        var template = mockTemplateDescription().packageName("com.example").className("JtefooGenerated").name("foo.jte");

        Collection<Path> result = extension.generate(config, Set.of(template));

        assertThat(result).hasSize(1);
        Path packageInfo = tempDir.resolve("com/example/package-info.java");
        assertThat(packageInfo).exists();
        assertThat(Files.readString(packageInfo)).isEqualTo(
                "@NullMarked\npackage com.example;\n\nimport org.jspecify.annotations.NullMarked;\n");
    }

    @Test
    void multipleTemplatesSamePackage() {
        var config = mockConfig().generatedSourcesRoot(tempDir).packageName("com.example");
        var t1 = mockTemplateDescription().packageName("com.example").className("JtefooGenerated").name("foo.jte");
        var t2 = mockTemplateDescription().packageName("com.example").className("JtebarGenerated").name("bar.jte");

        Collection<Path> result = extension.generate(config, Set.of(t1, t2));

        assertThat(result).hasSize(1);
        assertThat(tempDir.resolve("com/example/package-info.java")).exists();
    }

    @Test
    void multiplePackages() {
        var config = mockConfig().generatedSourcesRoot(tempDir).packageName("com.example");
        var t1 = mockTemplateDescription().packageName("com.example").className("JtefooGenerated").name("foo.jte");
        var t2 = mockTemplateDescription().packageName("com.example.sub").className("JtebarGenerated").name("sub/bar.jte");

        Collection<Path> result = extension.generate(config, Set.of(t1, t2));

        assertThat(result).hasSize(2);
        assertThat(tempDir.resolve("com/example/package-info.java")).exists();
        assertThat(tempDir.resolve("com/example/sub/package-info.java")).exists();
    }

    @Test
    void existingPackageInfoIsNotOverwritten() throws IOException {
        var config = mockConfig().generatedSourcesRoot(tempDir).packageName("com.example");
        var template = mockTemplateDescription().packageName("com.example").className("JtefooGenerated").name("foo.jte");
        Path packageDir = tempDir.resolve("com/example");
        Files.createDirectories(packageDir);
        Path existing = packageDir.resolve("package-info.java");
        Files.writeString(existing, "// existing content\n");

        extension.generate(config, Set.of(template));

        assertThat(Files.readString(existing)).isEqualTo("// existing content\n");
    }

    @Test
    void emptyTemplates() {
        var config = mockConfig().generatedSourcesRoot(tempDir).packageName("com.example");

        Collection<Path> result = extension.generate(config, Set.of());

        assertThat(result).isEmpty();
    }

    @Test
    void nullSourcesRoot() {
        var config = mockConfig().packageName("com.example");

        Collection<Path> result = extension.generate(config, Set.of(
                mockTemplateDescription().packageName("com.example").className("JtefooGenerated").name("foo.jte")));

        assertThat(result).isEmpty();
    }
}
