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

    /**
     * The content type of all templates. Either Plain or Html.
     */
    @Parameter(required = true)
    public String contentType;

    /**
     * Trims control structures, resulting in prettier output.
     */
    @Parameter
    public boolean trimControlStructures;

    /**
     * Intercepts the given html tags during template compilation
     * and calls the configured htmlInterceptor during template rendering.
     */
    @Parameter
    public String[] htmlTags;

    /**
     * By default, jte omits all HTML/CSS/JS comments, when compiling with {@link ContentType#Html}.
     * If you don't want this behavior, you can disable it here.
     */
    @Parameter
    public boolean htmlCommentsPreserved;

    /**
     * Setting, that UTF-8 encodes all static template parts at compile time.
     * Only makes sense if you use a binary output, like {@link gg.jte.output.Utf8ByteOutput}.
     */
    @Parameter
    public boolean binaryStaticContent;

    /**
     * The package name, where template classes are generated to
     */
    @Parameter
    public String packageName = Constants.PACKAGE_NAME_PRECOMPILED;

    /**
     * Directory in which to generate non-java files (resources). Typically, set by plugin rather than end user.
     * Optional - if null, resources will not be generated
     */
    @Parameter
    public String targetResourceDirectory;

    /**
     * Optional - Extensions this template engine should load. Currently, the following extensions exist:
     *
     * <ul>
     *     <li>gg.jte.models.generator.ModelExtension</li>
     *     <li>gg.jte.nativeimage.NativeResourcesExtension</li>
     * </ul>
     */
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
        }
        templateEngine.setProjectNamespace(project.getGroupId() + "/" + project.getArtifactId());
        if (extensions != null) {
            templateEngine.setExtensions(
                    extensions.stream()
                            .collect(Collectors.toMap(ExtensionSettings::getClassName, ExtensionSettings::getSettings))
            );
            getLog().info("Using extensions = " + extensions);
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
