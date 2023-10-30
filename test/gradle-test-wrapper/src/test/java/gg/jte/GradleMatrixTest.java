package gg.jte;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GradleMatrixTest {
    public static final List<String> GRADLE_VERSIONS = getTestGradleVersions();
    public static final String DEFAULT = "DEFAULT";

    /**
     * Use system property "gradle.matrix.versions" to test multiple versions. Note this may result in downloading those
     * versions if they are not already present.
     * @return
     */
    private static List<String> getTestGradleVersions() {
        String versionProperty = System.getProperty("gradle.matrix.versions", DEFAULT);
        return Arrays.asList(versionProperty.split("[,\\s]+"));
    }

    public static final String TASK_NAME = ":check";
    public static Stream<Arguments> runGradleBuild() throws IOException {
        return Files.find(Paths.get(".."), 2, (p, attr) -> p.getFileName().toString().startsWith("settings.gradle"))
                .map(Path::getParent)
                .filter(p -> p.getFileName().toString().startsWith("jte-runtime") || p.getFileName().toString().startsWith("kte-runtime"))
                .flatMap(p -> GRADLE_VERSIONS.stream().map(v -> Arguments.arguments(p, v)));
    }

    @ParameterizedTest
    @MethodSource
    public void runGradleBuild(Path projectDir, String gradleVersion) {
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withTestKitDir(Paths.get("build").resolve(projectDir.getFileName()).toAbsolutePath().toFile())
                .withArguments("--configuration-cache", TASK_NAME);

        if (!DEFAULT.equals(gradleVersion)) {
            runner = runner.withGradleVersion(gradleVersion);
        }

        BuildResult result = runner.build();

        Assertions.assertNotEquals(TaskOutcome.FAILED, result.task(TASK_NAME).getOutcome(), String.format("Build failed in %s with Gradle Version %s", projectDir, gradleVersion));
    }
}
