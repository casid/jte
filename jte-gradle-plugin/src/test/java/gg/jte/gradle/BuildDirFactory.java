package gg.jte.gradle;

import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDirFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class BuildDirFactory implements TempDirFactory {
    @Override
    public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext) throws Exception {
        var testName = extensionContext.getRequiredTestMethod().getName();
        var path = Path.of("build", "test", testName);
        Files.createDirectories(path);
        return path;
    }
}
