package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.html.HtmlPolicy;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PrecompileJteTask extends JteTaskBase {

    @Inject
    public PrecompileJteTask(JteExtension extension)
    {
        super(extension, JteStage.PRECOMPILE);
    }

    @Nested
    public FileCollection getCompilePath() {
        return extension.getCompilePath();
    }

    public void setCompilePath(FileCollection compilePath) {
        extension.getCompilePath().from(compilePath);
        setterCalled();
    }

    @Input
    @Optional
    public String getHtmlPolicyClass() {
        return extension.getHtmlPolicyClass().getOrNull();
    }

    public void setHtmlPolicyClass(String htmlPolicyClass) {
        extension.getHtmlPolicyClass().set(htmlPolicyClass);
        setterCalled();
    }

    @Input
    @Optional
    public String[] getCompileArgs() {
        return extension.getCompileArgs().getOrNull();
    }

    public void setCompileArgs(String[] compileArgs) {
        extension.getCompileArgs().set(compileArgs);
        setterCalled();
    }

    @TaskAction
    public void execute() {
        // Prevent Kotlin compiler to leak file handles, see https://github.com/casid/jte/issues/77
        // Use String literal as KOTLIN_COMPILER_ENVIRONMENT_KEEPALIVE_PROPERTY constant isn't available
        System.setProperty("kotlin.environment.keepalive", "false");

        Logger logger = getLogger();
        long start = System.nanoTime();

         Path sourceDirectory = getSourceDirectory();
        logger.info("Precompiling jte templates found in " + sourceDirectory);

        Path targetDirectory = getTargetDirectory();
        TemplateEngine templateEngine = TemplateEngine.create(
                new DirectoryCodeResolver(sourceDirectory),
                targetDirectory,
                getContentType(),
                null,
                getPackageName());
        templateEngine.setTrimControlStructures(Boolean.TRUE.equals(getTrimControlStructures()));
        templateEngine.setHtmlTags(getHtmlTags());
        templateEngine.setHtmlAttributes(getHtmlAttributes());
        if (extension.getHtmlPolicyClass().isPresent()) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(getHtmlPolicyClass()));
        }
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE.equals(getHtmlCommentsPreserved()));
        templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(getBinaryStaticContent()));
        templateEngine.setCompileArgs(getCompileArgs());
        templateEngine.setTargetResourceDirectory(getTargetResourceDirectory());

        int amount;
        try {
            templateEngine.cleanAll();
            List<String> compilePathFiles = getCompilePath().getFiles().stream().map(File::getAbsolutePath).collect(Collectors.toList());
            amount = templateEngine.precompileAll(compilePathFiles).size();
        } catch (Exception e) {
            logger.error("Failed to precompile templates.", e);

            throw e;
        }

        long end = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
        logger.info("Successfully precompiled " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + duration + "s to " + targetDirectory);
    }

    private HtmlPolicy createHtmlPolicy(String htmlPolicyClass) {
        try {
            URLClassLoader projectClassLoader = createProjectClassLoader();
            Class<?> clazz = projectClassLoader.loadClass(htmlPolicyClass);
            return (HtmlPolicy) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to instantiate custom HtmlPolicy " + htmlPolicyClass, e);
        }
    }

    private URLClassLoader createProjectClassLoader() throws IOException {
        List<File> files = new ArrayList<>(getCompilePath().getFiles());

        URL[] runtimeUrls = new URL[files.size()];
        for (int i = 0; i < files.size(); i++) {
            File element = files.get(i);
            runtimeUrls[i] = element.toURI().toURL();
        }
        return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
    }
}
