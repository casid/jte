package gg.jte.maven;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.html.HtmlPolicy;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;
import gg.jte.runtime.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

@Mojo(name = "precompile", defaultPhase = PROCESS_CLASSES, requiresDependencyResolution = COMPILE, threadSafe = true)
public class CompilerMojo extends AbstractMojo {

    /**
     * The directory where template files are located
     */
    @Parameter(required = true)
    public String sourceDirectory;

    /**
     * The directory where compiled classes should be written to
     */
    @Parameter(required = true)
    public String targetDirectory;

    @Parameter(defaultValue = "${project.compileClasspathElements}", required = true)
    public List<String> compilePath;

    @Parameter(required = true)
    public String contentType;

    @Parameter
    public boolean trimControlStructures;

    @Parameter
    public String[] htmlTags;

    @Parameter
    public String htmlPolicyClass;

    @Parameter
    public boolean htmlCommentsPreserved;

    @Parameter
    public boolean binaryStaticContent;

    @Parameter
    public String[] compileArgs;

    @Parameter
    public String packageName = Constants.PACKAGE_NAME_PRECOMPILED;

    @Parameter
    public boolean keepGeneratedSourceFiles;


    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;


    @Override
    public void execute() {
        // Prevent Kotlin compiler to leak file handles, see https://github.com/casid/jte/issues/77
        // Use String literal as KOTLIN_COMPILER_ENVIRONMENT_KEEPALIVE_PROPERTY constant isn't available
        System.setProperty("kotlin.environment.keepalive", "false");

        long start = System.nanoTime();

        Path source = Paths.get(sourceDirectory);
        Path target = Paths.get(targetDirectory);

        getLog().info("Precompiling jte templates found in " + source);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(source), target, ContentType.valueOf(contentType), null, packageName);
        templateEngine.setTrimControlStructures(trimControlStructures);
        templateEngine.setHtmlTags(htmlTags);
        if (htmlPolicyClass != null) {
            templateEngine.setHtmlPolicy(createHtmlPolicy(htmlPolicyClass));
        }
        templateEngine.setHtmlCommentsPreserved(htmlCommentsPreserved);
        templateEngine.setBinaryStaticContent(binaryStaticContent);
        templateEngine.setCompileArgs(calculateCompileArgs());

        int amount;
        try {
            templateEngine.cleanAll();
            List<String> generatedSourceFiles = templateEngine.precompileAll(compilePath);
            if (!keepGeneratedSourceFiles) {
                deleteGeneratedSourceFiles(target, generatedSourceFiles);
            }
            amount = generatedSourceFiles.size();
        } catch (Exception e) {
            getLog().error("Failed to precompile templates.");
            getLog().error(e);

            throw e;
        }

        long end = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
        getLog().info("Successfully precompiled " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + duration + "s to " + target);
    }

    private String[] calculateCompileArgs() {
        List<String> allCompileArgs = new ArrayList<>();

        // See https://docs.oracle.com/en/java/javase/14/docs/specs/man/javac.html#option-release
        String javaRelease = project.getProperties().getProperty("maven.compiler.release");
        if (!StringUtils.isBlank(javaRelease)) {
            allCompileArgs.add("--release");
            allCompileArgs.add(javaRelease);
        } else {
            String javaSource = project.getProperties().getProperty("maven.compiler.source");
            if (!StringUtils.isBlank(javaSource)) {
                allCompileArgs.add("-source");
                allCompileArgs.add(javaSource);
            }

            String javaTarget = project.getProperties().getProperty("maven.compiler.target");
            if (!StringUtils.isBlank(javaTarget)) {
                allCompileArgs.add("-target");
                allCompileArgs.add(javaTarget);
            }
        }

        if (compileArgs != null) {
            allCompileArgs.addAll(Arrays.asList(compileArgs));
        }

        return allCompileArgs.toArray(new String[0]);
    }

    private void deleteGeneratedSourceFiles(Path target, List<String> generatedSources) {
        for (String generatedSource : generatedSources) {
            Path generatedSourceFile = target.resolve(generatedSource);
            if (!generatedSourceFile.toFile().delete()) {
                getLog().warn("Failed to delete generated source file " + generatedSourceFile);
            }
        }
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
