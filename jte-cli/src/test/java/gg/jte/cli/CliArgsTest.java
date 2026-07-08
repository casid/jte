package gg.jte.cli;

import gg.jte.ContentType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CliArgsTest {

    @Test
    void parsesAllOptions() {
        Main main = new Main();
        Main.newCommandLine(main).parseArgs(
                "-s", "src/jte",
                "-t", "target/generated",
                "-c", "Html",
                "--trim-control-structures",
                "--html-tags", "form,input",
                "--html-comments-preserved",
                "--binary-static-content",
                "--package-name", "com.example.templates",
                "--target-resource-directory", "target/resources"
        );

        assertThat(main.sourceDirectory).isEqualTo(Path.of("src/jte"));
        assertThat(main.targetDirectory).isEqualTo(Path.of("target/generated"));
        assertThat(main.contentType).isEqualTo(ContentType.Html);
        assertThat(main.trimControlStructures).isTrue();
        assertThat(main.htmlTags).containsExactly("form", "input");
        assertThat(main.htmlCommentsPreserved).isTrue();
        assertThat(main.binaryStaticContent).isTrue();
        assertThat(main.packageName).isEqualTo("com.example.templates");
        assertThat(main.targetResourceDirectory).isEqualTo(Path.of("target/resources"));
    }

    @Test
    void defaultsPackageNameAndRequiresSourceTargetAndContentType() {
        Main main = new Main();
        Main.newCommandLine(main).parseArgs("-s", "src/jte", "-t", "target/generated", "-c", "Plain");

        assertThat(main.packageName).isEqualTo("gg.jte.generated.precompiled");
        assertThat(main.trimControlStructures).isFalse();
        assertThat(main.htmlTags).isNull();
    }

    @Test
    void parsesContentTypeCaseInsensitively() {
        Main main = new Main();
        Main.newCommandLine(main).parseArgs("-s", "src/jte", "-t", "target/generated", "-c", "html");

        assertThat(main.contentType).isEqualTo(ContentType.Html);
    }
}
