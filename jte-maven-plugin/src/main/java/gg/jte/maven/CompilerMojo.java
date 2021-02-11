package gg.jte.maven;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.html.HtmlPolicy;
import gg.jte.resolve.DirectoryCodeResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

@Mojo(name = "precompile", defaultPhase = PROCESS_CLASSES, requiresDependencyResolution = COMPILE)
public class CompilerMojo extends AbstractMojo {

    /**
     * The directory where template files are located
     */
    @Parameter
    public String sourceDirectory;

    /**
     * The directory where compiled classes should be written to
     */
    @Parameter
    public String targetDirectory;

    @Parameter(defaultValue = "${project.compileClasspathElements}", readonly = true, required = true)
    public List<String> compilePath;

    @Parameter(readonly = true, required = true)
    public String contentType;

    @Parameter(readonly = true)
    public boolean trimControlStructures;

    @Parameter(readonly = true)
    public String[] htmlTags;

    @Parameter(readonly = true)
    public String[] htmlAttributes;

    @Parameter(readonly = true)
    public String htmlPolicyClass;

    @Parameter(readonly = true)
    public boolean htmlCommentsPreserved;

    @Parameter(readonly = true)
    public String[] compileArgs;


    @Override
    public void execute() {

        long start = System.nanoTime();

        Path source = Paths.get(sourceDirectory);
        Path target = Paths.get(targetDirectory);

        getLog().info("Precompiling jte templates found in " + source);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(source), target, ContentType.valueOf(contentType));
        templateEngine.setTrimControlStructures(trimControlStructures);
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlAttributes(htmlAttributes);
        if (htmlPolicyClass != null) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass));
        }
        templateEngine.setHtmlCommentsPreserved(htmlCommentsPreserved);
        templateEngine.setCompileArgs(compileArgs);

        int amount;
        try {
            templateEngine.cleanAll();
            amount = templateEngine.precompileAll(compilePath).size();
        } catch (Exception e) {
            getLog().error("Failed to precompile templates.");
            getLog().error(e);

            throw e;
        }

        long end = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
        getLog().info("Successfully precompiled " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + duration + "s to " + target);
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
            String element = compilePath.get(i);
            runtimeUrls[i] = new File(element).toURI().toURL();
        }
        return new URLClassLoader(runtimeUrls, Thread.currentThread().getContextClassLoader());
    }
}
