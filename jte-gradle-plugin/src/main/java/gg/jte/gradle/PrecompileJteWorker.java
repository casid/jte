package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.html.HtmlPolicy;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class PrecompileJteWorker implements WorkAction<PrecompileJteWorker.Parameters> {
    private static final Logger logger = Logging.getLogger(PrecompileJteWorker.class);

    interface Parameters extends WorkParameters {
        RegularFileProperty getSourceDirectory();

        RegularFileProperty getTargetDirectory();

        Property<gg.jte.ContentType> getContentType();

        Property<String> getPackageName();

        Property<Boolean> getTrimControlStructures();

        Property<String[]> getHtmlTags();

        Property<String> getHtmlPolicyClass();

        Property<Boolean> getHtmlCommentsPreserved();

        Property<Boolean> getBinaryStaticContent();

        Property<String[]> getCompileArgs();

        Property<String[]> getKotlinCompileArgs();

        RegularFileProperty getTargetResourceDirectory();

        ConfigurableFileCollection getCompilePath();
    }

    @Override
    public void execute() {
        // Prevent Kotlin compiler to leak file handles
        System.setProperty("kotlin.environment.keepalive", "false");

        long start = System.nanoTime();
        Parameters params = getParameters();

        Path sourceDirectory = params.getSourceDirectory().get().getAsFile().toPath();
        Path targetDirectory = params.getTargetDirectory().get().getAsFile().toPath();

        logger.info("Precompiling jte templates found in {}", sourceDirectory);

        TemplateEngine templateEngine = TemplateEngine.create(
                new DirectoryCodeResolver(sourceDirectory),
                targetDirectory,
                params.getContentType().get(),
                null,
                params.getPackageName().get());

        templateEngine.setTrimControlStructures(Boolean.TRUE.equals(params.getTrimControlStructures().getOrNull()));
        templateEngine.setHtmlTags(params.getHtmlTags().getOrNull());

        String htmlPolicyClass = params.getHtmlPolicyClass().getOrNull();
        if (htmlPolicyClass != null) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass, params.getCompilePath()));
        }

        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE.equals(params.getHtmlCommentsPreserved().getOrNull()));
        templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(params.getBinaryStaticContent().getOrNull()));
        templateEngine.setCompileArgs(params.getCompileArgs().getOrNull());
        templateEngine.setKotlinCompileArgs(params.getKotlinCompileArgs().getOrNull());

        File targetResourceDir = params.getTargetResourceDirectory().getAsFile().getOrNull();
        if (targetResourceDir != null) {
            templateEngine.setTargetResourceDirectory(targetResourceDir.toPath());
        }

        int amount;
        try {
            templateEngine.cleanAll();
            List<String> compilePathFiles = params.getCompilePath().getFiles().stream()
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            amount = templateEngine.precompileAll(compilePathFiles).size();
        } catch (Exception e) {
            logger.error("Failed to precompile templates.", e);
            throw e;
        }

        long end = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
        logger.info("Successfully precompiled {} jte file{} in {}s to {}", amount, amount == 1 ? "" : "s", duration, targetDirectory);
    }

    private HtmlPolicy createHtmlPolicy(String htmlPolicyClass, ConfigurableFileCollection compilePath) {
        try (URLClassLoader projectClassLoader = createProjectClassLoader(compilePath)) {
            Class<?> clazz = projectClassLoader.loadClass(htmlPolicyClass);
            return (HtmlPolicy) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate custom HtmlPolicy " + htmlPolicyClass, e);
        }
    }

    private URLClassLoader createProjectClassLoader(ConfigurableFileCollection compilePath) {
        try {
            List<File> files = new ArrayList<>(compilePath.getFiles());
            URL[] runtimeUrls = new URL[files.size()];
            for (int i = 0; i < files.size(); i++) {
                runtimeUrls[i] = files.get(i).toURI().toURL();
            }
            return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create project classloader", e);
        }
    }
}
