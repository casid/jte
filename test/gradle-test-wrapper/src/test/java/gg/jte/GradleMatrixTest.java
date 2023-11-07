package gg.jte;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GradleMatrixTest {
    public static final List<String> GRADLE_VERSIONS = getTestGradleVersions();
    public static final String DEFAULT = "DEFAULT";

    /**
     * Use system property "gradle.matrix.versions" to test multiple versions. Note this may result in downloading those
     * versions if they are not already present.
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
    public void runGradleBuild(Path projectDir, String gradleVersion) throws IOException {
        // Clean the configuration cache. Makes it easier to run the tests locally
        // multiple times in a predictable way. For reference, see:
        // https://docs.gradle.org/current/userguide/configuration_cache.html#config_cache:usage:invalidate
        deleteDir(projectDir.resolve(".gradle/configuration-cache"));

        // Then run the task
        BuildResult result = runner(projectDir, gradleVersion)
                .withArguments("--configuration-cache", TASK_NAME)
                .build();

        // Check task was successful
        BuildTask task = result.task(TASK_NAME);
        Assertions.assertNotNull(task, "Build result must have a task " + TASK_NAME);
        Assertions.assertNotEquals(
            TaskOutcome.FAILED,
            task.getOutcome(),
            String.format("Build failed in %s with Gradle Version %s", projectDir, gradleVersion)
        );
    }

    public static Stream<Arguments> checkBuildCache() {
        return Stream
                .of(Paths.get("../jte-runtime-cp-test-models-gradle"), Paths.get("../jte-runtime-test-gradle-convention"))
                .flatMap(p -> GRADLE_VERSIONS.stream().map(v -> Arguments.arguments(p, v)));
    }

    @ParameterizedTest
    @MethodSource
    public void checkBuildCache(Path projectDir, String gradleVersion) throws IOException {
        // Populates the cache
        runner(projectDir, gradleVersion)
                .withArguments("--build-cache", TASK_NAME)
                .build();

        // Clean the build directory
        deleteDir(projectDir.resolve("build"));

        // A second run must result in build-cache
        BuildResult result = runner(projectDir, gradleVersion)
                .withArguments("--build-cache", TASK_NAME)
                .withDebug(true)
                .build();

        BuildTask mainTask = result.task(TASK_NAME);
        Assertions.assertNotNull(mainTask, String.format("A task named %s must be part of the build result", TASK_NAME));
        Assertions.assertNotEquals(
            TaskOutcome.FAILED,
            mainTask.getOutcome(),
            String.format("Build failed in %s with Gradle Version %s", projectDir, gradleVersion)
        );

        // Check the outcome for only these tasks since we want to test if they
        // populate the cache properly.
        List<BuildTask> tasks = Stream.of(":generateJte", ":precompileJte")
                .map(result::task)
                // When generate is executed, precompile is skipped, and vice versa, then we filter out the
                // skipped one here.
                .filter(task -> task != null && task.getOutcome() != TaskOutcome.SKIPPED)
                .toList();

        Assertions.assertFalse(tasks.isEmpty(), "At least one of :generateJte or :precompileJte tasks should be present");
        tasks.forEach(task -> Assertions.assertEquals(
                TaskOutcome.FROM_CACHE,
                task.getOutcome(),
                String.format("Expected outcome for task %s was %s, but got %s. Build output: \n %s", task.getPath(), TaskOutcome.FROM_CACHE, task.getOutcome(), result.getOutput())
        ));
    }

    private GradleRunner runner(Path projectDir, String gradleVersion) {
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withTestKitDir(Paths.get("build").resolve(projectDir.getFileName()).toAbsolutePath().toFile());

        if (!DEFAULT.equals(gradleVersion)) {
            runner = runner.withGradleVersion(gradleVersion);
        }
        return runner;
    }

    private void deleteDir(Path directoryToDelete) throws IOException {
        Files.walkFileTree(directoryToDelete, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
