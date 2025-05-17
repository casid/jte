package gg.jte.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;

public class JteGradleTest {
    @TempDir(cleanup = CleanupMode.NEVER, factory = BuildDirFactory.class)
    Path tempProjectDir;

    Path resourcesDir = Path.of("src/test/resources");

    @Test
    public void conventionGenerate() throws Exception {
        Files.copy(resourcesDir.resolve("conventionGenerate.gradle"), tempProjectDir.resolve("build.gradle"), StandardCopyOption.REPLACE_EXISTING);
        var jteDir = Files.createDirectories(tempProjectDir.resolve("src/main/jte"));
        Files.copy(resourcesDir.resolve("helloHtml.jte"), jteDir.resolve("helloHtml.jte"), StandardCopyOption.REPLACE_EXISTING);

        var result = GradleRunner.create()
                .withProjectDir(tempProjectDir.toFile())
                .withArguments("compileJava")
                .withPluginClasspath()
                .build();

        assertThat(result).isNotNull();
        assertThat(tempProjectDir.resolve("build/generated-sources/jte/gg/jte/generated/precompiled/JtehelloHtmlGenerated.java")).exists();
        assertThat(tempProjectDir.resolve("build/classes/java/main/gg/jte/generated/precompiled/JtehelloHtmlGenerated.class")).exists();
    }

    @Test
    public void conventionPlusTask() throws Exception {
        Files.copy(resourcesDir.resolve("conventionPlusTask.gradle"), tempProjectDir.resolve("build.gradle"), StandardCopyOption.REPLACE_EXISTING);
        var jteDir = Files.createDirectories(tempProjectDir.resolve("src/main/jte"));
        var htmlDir = Files.createDirectories(jteDir.resolve("html"));
        var plainDir = Files.createDirectories(jteDir.resolve("plain"));
        Files.copy(resourcesDir.resolve("helloHtml.jte"), htmlDir.resolve("helloHtml.jte"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(resourcesDir.resolve("helloPlain.jte"), plainDir.resolve("helloPlain.jte"), StandardCopyOption.REPLACE_EXISTING);

        var result = GradleRunner.create()
                .withProjectDir(tempProjectDir.toFile())
                .withArguments("compileJava")
                .withPluginClasspath()
                .build();

        assertThat(result).isNotNull();
        assertThat(tempProjectDir.resolve("build/generated-sources/jte/gg/jte/generated/precompiled/html/JtehelloHtmlGenerated.java")).exists();
        assertThat(tempProjectDir.resolve("build/classes/java/main/gg/jte/generated/precompiled/html/JtehelloHtmlGenerated.class")).exists();
        assertThat(tempProjectDir.resolve("build/generated-sources/jte/gg/jte/generated/precompiled/plain/JtehelloPlainGenerated.java")).exists();
        assertThat(tempProjectDir.resolve("build/classes/java/main/gg/jte/generated/precompiled/plain/JtehelloPlainGenerated.class")).exists();
    }
}
