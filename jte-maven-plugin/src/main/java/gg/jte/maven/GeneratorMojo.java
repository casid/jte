package gg.jte.maven;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

@Mojo(name = "generate", defaultPhase = GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    /**
     * The directory where template files are located.
     */
    @Parameter
    public String sourceDirectory;

    /**
     * Destination directory to store generated templates. Defaults to 'target/generated-sources/jte'.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/jte")
    public String targetDirectory;

    @Parameter(readonly = true, required = true)
    public String contentType;

    @Parameter(readonly = true)
    public String[] htmlTags;

    @Parameter(readonly = true)
    public String[] htmlAttributes;


    @Override
    public void execute() {

        long start = System.nanoTime();

        Path source = Path.of(sourceDirectory);
        Path target = Path.of(targetDirectory);

        getLog().info("Generating jte templates found in " + source);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(source), target, ContentType.valueOf(contentType));
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlAttributes(htmlAttributes);

        int amount;
        try {
            templateEngine.cleanAll();
            amount = templateEngine.generateAll();
        } catch (Exception e) {
            getLog().error("Failed to generate templates.");
            getLog().error(e);

            throw e;
        }

        long end = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
        getLog().info("Successfully generated " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + duration + "s to " + target);

        project.addCompileSourceRoot(targetDirectory);
    }
}
