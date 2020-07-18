package org.jusecase.jte.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jusecase.jte.ContentType;
import org.jusecase.jte.TemplateEngine;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.nio.file.Path;
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
    public String[] htmlTags;

    @Parameter(readonly = true)
    public String[] htmlAttributes;


    @Override
    public void execute() {

        Path source = Path.of(sourceDirectory);
        Path target = Path.of(targetDirectory);

        long start = System.nanoTime();
        getLog().info("Precompiling jte templates found in " + source);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(source), target, ContentType.valueOf(contentType));
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlAttributes(htmlAttributes);

        try {
            templateEngine.cleanAll();
            templateEngine.precompileAll(compilePath);
        } catch (Exception e) {
            getLog().error("Failed to precompile templates.");
            getLog().error(e);

            throw e;
        }

        long end = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
        getLog().info("Successfully precompiled jte templates in " + duration + "s to " + target);
    }
}
