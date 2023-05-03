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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

@Mojo(name = "generate", defaultPhase = GENERATE_SOURCES, threadSafe = true)
public class GeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    public MavenProject project;

    /**
     * The directory where template files are located.
     */
    @Parameter(required = true)
    public String sourceDirectory;

    /**
     * Destination directory to store generated templates. Defaults to 'target/generated-sources/jte'.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/jte")
    public String targetDirectory;

    @Parameter(required = true)
    public String contentType;

    @Parameter
    public boolean trimControlStructures;

    @Parameter
    public String[] htmlTags;

    @Parameter
    public boolean htmlCommentsPreserved;

    @Parameter
    public boolean binaryStaticContent;

    @Parameter
    public String packageName = Constants.PACKAGE_NAME_PRECOMPILED;

    @Parameter
    public String targetResourceDirectory;

    @Parameter(defaultValue = "false")
    public boolean generateNativeImageResources;

    @Parameter
    public List<ExtensionSettings> extensions;

    @Override
    public void execute() {

        long start = System.nanoTime();

        Path source = Paths.get(sourceDirectory);
        Path target = Paths.get(targetDirectory);

        getLog().info("Generating jte templates found in " + source);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(source), target, ContentType.valueOf(contentType), null, packageName);
        templateEngine.setTrimControlStructures(trimControlStructures);
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlCommentsPreserved(htmlCommentsPreserved);
        templateEngine.setBinaryStaticContent(binaryStaticContent);
        if (targetResourceDirectory != null) {
            templateEngine.setTargetResourceDirectory(Paths.get(targetResourceDirectory));
            templateEngine.setGenerateNativeImageResources(generateNativeImageResources);
        }
        templateEngine.setProjectNamespace(project.getGroupId() + "/" + project.getArtifactId());
        getLog().info("extensions=" + extensions);
        if (extensions != null) {
            templateEngine.setExtensions(
                    extensions.stream()
                            .collect(Collectors.toMap(ExtensionSettings::getClassName, ExtensionSettings::getSettings))
            );
        }
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
