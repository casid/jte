package gg.jte.maven;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.nio.file.Paths;
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
    public boolean trimControlStructures;

    @Parameter(readonly = true)
    public String[] htmlTags;

    @Parameter(readonly = true)
    public String[] htmlAttributes;

    @Parameter(readonly = true)
    public boolean htmlCommentsPreserved;

    @Parameter(readonly = true)
    public boolean binaryStaticContent;

    @Parameter(readonly = true)
    public String packageName = Constants.PACKAGE_NAME_PRECOMPILED;

    @Parameter(defaultValue = "${project.build.directory}/classes")
    public String targetResourceDirectory;

    @Override
    public void execute() {

        long start = System.nanoTime();

        Path source = Paths.get(sourceDirectory);
        Path target = Paths.get(targetDirectory);

        getLog().info("Generating jte templates found in " + source);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(source), target, ContentType.valueOf(contentType), null, packageName);
        templateEngine.setTrimControlStructures(trimControlStructures);
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlAttributes(htmlAttributes);
        templateEngine.setHtmlCommentsPreserved(htmlCommentsPreserved);
        templateEngine.setBinaryStaticContent(binaryStaticContent);
        if (targetResourceDirectory != null)
        {
            templateEngine.setTargetResourceDirectory(Paths.get(targetResourceDirectory));
        }
        templateEngine.setProjectNamespace(project.getGroupId() + "/" + project.getArtifactId());

        int amount;
        try {
            templateEngine.cleanAll();
            amount = templateEngine.generateAll().size();
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
