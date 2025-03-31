package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.workers.WorkAction;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class GenerateJteWorker implements WorkAction<GenerateJteParams> {
    @Override
    public void execute() {
        Logger logger = Logging.getLogger(GenerateJteWorker.class);
        long start = System.nanoTime();

        GenerateJteParams params = getParameters();

        // Load compiler in isolated classloader
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader compilerClassLoader = createCompilerClassLoader(params.getCompilerClasspath())) {
            Thread.currentThread().setContextClassLoader(compilerClassLoader);

            Path sourceDirectory = path(params.getSourceDirectory());
            Path targetDirectory = path(params.getTargetDirectory());

            logger.info("Generating jte templates found in {}", sourceDirectory);

            TemplateEngine templateEngine = TemplateEngine.create(
                    new DirectoryCodeResolver(sourceDirectory),
                    targetDirectory,
                    params.getContentType().get(),
                    null,
                    params.getPackageName().get());
            templateEngine.setTrimControlStructures(params.getTrimControlStructures().getOrElse(false));
            templateEngine.setHtmlTags(params.getHtmlTags().getOrNull());
            templateEngine.setHtmlCommentsPreserved(params.getHtmlCommentsPreserved().getOrElse(false));
            templateEngine.setBinaryStaticContent(params.getBinaryStaticContent().getOrElse(false));
            templateEngine.setTargetResourceDirectory(path(params.getTargetResourceDirectory()));
            templateEngine.setProjectNamespace(params.getProjectNamespace().getOrNull());
            templateEngine.setExtensions(params.getJteExtensions().get());

            int amount;
            try {
                templateEngine.cleanAll();
                amount = templateEngine.generateAll().size();
            } catch (Exception e) {
                logger.error("Failed to generate templates.", e);
                throw e;
            }

            long end = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
            logger.info("Successfully generated {} jte file{} in {}s to {}", amount, amount == 1 ? "" : "s", duration, targetDirectory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute template generation", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private URLClassLoader createCompilerClassLoader(ConfigurableFileCollection compilerClasspath) {
        try {
            List<URL> urls = new ArrayList<>();
            for (File file : compilerClasspath.getFiles()) {
                urls.add(file.toURI().toURL());
            }
            return new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create compiler classloader", e);
        }
    }

    private static Path path(RegularFileProperty property) {
        return property.getAsFile().get().toPath();
    }
}
