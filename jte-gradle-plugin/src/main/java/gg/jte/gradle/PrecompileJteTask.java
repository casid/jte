package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.html.HtmlPolicy;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PrecompileJteTask extends JteTaskBase {

    private FileCollection compilePath;
    private String htmlPolicyClass;
    private String[] compileArgs;

    @Nested
    public FileCollection getCompilePath() {
        return compilePath;
    }

    public void setCompilePath(FileCollection compilePath) {
        this.compilePath = compilePath;
    }

    @Input
    @Optional
    public String getHtmlPolicyClass() {
        return htmlPolicyClass;
    }

    public void setHtmlPolicyClass(String htmlPolicyClass) {
        this.htmlPolicyClass = htmlPolicyClass;
    }

    @Input
    @Optional
    public String[] getCompileArgs() {
        return compileArgs;
    }

    public void setCompileArgs(String[] compileArgs) {
        this.compileArgs = compileArgs;
    }

    @TaskAction
    public void execute() {
        Logger logger = getLogger();
        long start = System.nanoTime();

        logger.info("Precompiling jte templates found in " + sourceDirectory);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourceDirectory), targetDirectory, contentType);
        templateEngine.setTrimControlStructures(Boolean.TRUE.equals(trimControlStructures));
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlAttributes(htmlAttributes);
        if (htmlPolicyClass != null) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass));
        }
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE.equals(htmlCommentsPreserved));
        templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(binaryStaticContent));
        templateEngine.setCompileArgs(compileArgs);

        int amount;
        try {
            templateEngine.cleanAll();
            List<String> compilePathFiles = compilePath.getFiles().stream().map(File::getAbsolutePath).collect(Collectors.toList());
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
        List<File> files = new ArrayList<>(compilePath.getFiles());

        URL[] runtimeUrls = new URL[files.size()];
        for (int i = 0; i < files.size(); i++) {
            File element = files.get(i);
            runtimeUrls[i] = element.toURI().toURL();
        }
        return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
    }
}
