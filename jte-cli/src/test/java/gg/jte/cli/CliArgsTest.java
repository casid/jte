package gg.jte.cli;

import gg.jte.ContentType;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CliArgsTest {

    @Test
    void parsesAllOptions() {
        GenerateCommand generateCommand = parseGenerate(
                "generate",
                "-s", "src/jte",
                "-t", "target/generated",
                "-c", "Html",
                "--trim-control-structures",
                "--html-tags", "form,input",
                "--preserve-html-comments",
                "--binary-static-content",
                "--package-name", "com.example.templates",
                "--target-resource-directory", "target/resources"
        );

        assertThat(generateCommand.sourceDirectory).isEqualTo(Path.of("src/jte"));
        assertThat(generateCommand.targetDirectory).isEqualTo(Path.of("target/generated"));
        assertThat(generateCommand.contentType).isEqualTo(ContentType.Html);
        assertThat(generateCommand.trimControlStructures).isTrue();
        assertThat(generateCommand.htmlTags).containsExactly("form", "input");
        assertThat(generateCommand.htmlCommentsPreserved).isTrue();
        assertThat(generateCommand.binaryStaticContent).isTrue();
        assertThat(generateCommand.packageName).isEqualTo("com.example.templates");
        assertThat(generateCommand.targetResourceDirectory).isEqualTo(Path.of("target/resources"));
    }

    @Test
    void defaultsPackageNameAndRequiresSourceTargetAndContentType() {
        GenerateCommand generateCommand = parseGenerate("generate", "-s", "src/jte", "-t", "target/generated", "-c", "Plain");

        assertThat(generateCommand.packageName).isEqualTo("gg.jte.generated.precompiled");
        assertThat(generateCommand.trimControlStructures).isFalse();
        assertThat(generateCommand.htmlTags).isNull();
    }

    @Test
    void parsesContentTypeCaseInsensitively() {
        GenerateCommand generateCommand = parseGenerate("generate", "-s", "src/jte", "-t", "target/generated", "-c", "html");

        assertThat(generateCommand.contentType).isEqualTo(ContentType.Html);
    }

    private static GenerateCommand parseGenerate(String... args) {
        CommandLine.ParseResult result = Main.newCommandLine(new Main()).parseArgs(args);
        return (GenerateCommand) result.subcommand().commandSpec().userObject();
    }
}
