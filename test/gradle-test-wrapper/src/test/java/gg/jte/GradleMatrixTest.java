package gg.jte;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GradleMatrixTest {
    public static final List<String> GRADLE_VERSIONS = getTestGradleVersions();
    public static final String DEFAULT = "DEFAULT";

    // Follows Gradle recommendation:
    // https://docs.gradle.org/current/userguide/test_kit.html#sub:test-kit-build-cache
    // Basically, it uses a temporary directory to avoid test runs to polute the build
    // cache and interfer with each other.
    @TempDir
    public Path temporaryBuildCacheDir;

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
        BuildResult result = runner(projectDir, gradleVersion, TASK_NAME).build();

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
        // Start the test with a clean slate. This ensures that the task outcome
        // won't be something as `TaskOutcome.UP_TO_DATE`.
        deleteDir(projectDir.resolve("build"));

        // First run to populate the cache
        runner(projectDir, gradleVersion, "--build-cache", TASK_NAME).build();

        // Delete the build directory so that the next run uses
        // will need to use the build cache.
        deleteDir(projectDir.resolve("build"));

        // The second run must use the build cache.
        BuildResult result = runner(projectDir, gradleVersion, "--build-cache", TASK_NAME).build();

        BuildTask mainTask = result.task(TASK_NAME);
        Assertions.assertNotNull(mainTask, String.format("A task named %s must be part of the build result", TASK_NAME));
        Assertions.assertNotEquals(
            TaskOutcome.FAILED,
            mainTask.getOutcome(),
            String.format("Build failed in %s with Gradle Version %s", projectDir, gradleVersion)
        );

        // `generateJte` and `precompileJte` are the cacheable tasks we want to test.
        List<BuildTask> tasks = Stream.of(":generateJte", ":precompileJte")
                .map(result::task)
                // When `generate` is executed, `precompile` is skipped, and vice versa, then we
                // filter out the skipped one here.
                .filter(task -> task != null && task.getOutcome() != TaskOutcome.SKIPPED)
                .toList();

        Assertions.assertFalse(tasks.isEmpty(), "At least one of :generateJte or :precompileJte tasks should be present");
        tasks.forEach(task -> Assertions.assertEquals(
            TaskOutcome.FROM_CACHE,
            task.getOutcome(),
            String.format("Expected outcome for task %s was %s, but got %s. Build output: \n %s", task.getPath(), TaskOutcome.FROM_CACHE, task.getOutcome(), result.getOutput())
        ));
    }

    private GradleRunner runner(Path projectDir, String gradleVersion, String ... extraArgs) {
        List<String> arguments = new ArrayList<>(Arrays.asList(extraArgs));
        arguments.addAll(
            List.of(
                "--configuration-cache",
                "-Dtest.build.cache.dir=" + temporaryBuildCacheDir.toUri()
            )
        );

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withTestKitDir(Paths.get("build").resolve(projectDir.getFileName()).toAbsolutePath().toFile())
                .withArguments(arguments);

        if (!DEFAULT.equals(gradleVersion)) {
            runner = runner.withGradleVersion(gradleVersion);
        }
        return runner;
    }

    private void deleteDir(Path directoryToDelete) throws IOException {
        if (Files.notExists(directoryToDelete)) return;
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
