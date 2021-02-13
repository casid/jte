package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.html.HtmlPolicy;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PrecompileJteTask extends PrecompileJteBase {

    @TaskAction
    public void execute() throws URISyntaxException {
        Logger logger = getLogger();
        long start = System.nanoTime();

        logger.info("Precompiling jte templates found in " + sourceDirectory);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourceDirectory), targetDirectory, contentType);
        templateEngine.setTrimControlStructures(trimControlStructures);
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlAttributes(htmlAttributes);
        if (htmlPolicyClass != null) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass));
        }
        templateEngine.setHtmlCommentsPreserved(htmlCommentsPreserved);
        templateEngine.setCompileArgs(compileArgs);

        if (compilePath == null) {
            compilePath = new ArrayList<>();
        }

        // Somehow it doesn't link the dependencies, so we need to provide the path to jte-runtime and others to make it work
        compilePath.add(new File(PrecompileJteTask.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath());

        int amount;
        try {
            templateEngine.cleanAll();
            amount = templateEngine.precompileAll(compilePath.stream().map(path -> path.toAbsolutePath().toString()).collect(Collectors.toList())).size();
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
        URL[] runtimeUrls = new URL[compilePath.size()];
        for (int i = 0; i < compilePath.size(); i++) {
            Path element = compilePath.get(i);
            runtimeUrls[i] = element.toFile().toURI().toURL();
        }
        return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
    }
}
