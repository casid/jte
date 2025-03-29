package gg.jte.gradle;

import gg.jte.ContentType;
import gg.jte.runtime.Constants;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class JteGradle implements Plugin<Project> {
    private static final Logger logger = Logging.getLogger(JteGradle.class);
    private static final String KOTLIN_COMPILER_EMBEDDABLE = "org.jetbrains.kotlin:kotlin-compiler-embeddable";

    @Override 
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        
        // Check for kotlin-compiler-embeddable on classpath
        project.getConfigurations().all(configuration -> {
            configuration.getDependencies().all(dependency -> {
                if (isKotlinCompilerEmbeddable(dependency)) {
                    logger.warn(
                        "Warning: {} was found on the build classpath. This may cause compatibility issues. " +
                        "Consider using Gradle Worker API with classloader isolation instead.",
                        KOTLIN_COMPILER_EMBEDDABLE
                    );
                }
            });
        });
        SourceSet main = getMainSourceSet(project);
        JteExtension extension = project.getExtensions().create("jte", JteExtension.class, project.getObjects());
        defaults(project, extension, main);

        TaskProvider<PrecompileJteTask> precompileJteTask = project.getTasks().register("precompileJte", PrecompileJteTask.class, extension);
        precompileJteTask.configure(t -> t.dependsOn("compileJava"));
        project.getTasks().named("test").configure(t -> t.dependsOn(precompileJteTask));

        TaskProvider<GenerateJteTask> generateJteTask = project.getTasks().register("generateJte", GenerateJteTask.class, extension);
        configureTask(project, "compileJava", t -> t.dependsOn(generateJteTask));
        configureTask(project, "sourcesJar", t -> t.dependsOn(generateJteTask));
        configureTask(project, "processResources", t -> t.dependsOn(generateJteTask));
        configureTask(project, "compileKotlin", t -> t.dependsOn(generateJteTask));

        project.getTasks().named("clean").configure(t -> t.dependsOn("cleanPrecompileJte", "cleanGenerateJte")); // clean tasks are generated based on task outputs

        Configuration additionalClasspath = project.getConfigurations().create("jteGenerate");
        generateJteTask.configure(t -> {
            t.getClasspath().from(additionalClasspath);
            if (extension.getStage().isPresent() && extension.getStage().get() == JteStage.GENERATE) {
                main.getJava().srcDir(t.getTargetDirectory());
                main.getResources().srcDir(t.getTargetResourceDirectory());
            }
        });
        project.getConfigurations().named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, conf -> conf.extendsFrom(additionalClasspath));
    }

    private SourceSet getMainSourceSet(Project project) {
        JavaPluginExtension javaPluginExtension = project.getExtensions().getByType(JavaPluginExtension.class);
        return javaPluginExtension.getSourceSets().findByName("main");
    }

    private void defaults(Project project, JteExtension extension, SourceSet main) {
        extension.getSourceDirectory().convention(project.file("src/main/jte").toPath()); // TODO can it use sourceset?
        extension.getTargetDirectory().convention(extension.getStage().map(stage -> {
            if (stage == JteStage.PRECOMPILE) {
                return project.file("jte-classes").toPath();
            } else if (stage == JteStage.GENERATE) {
                return new File(project.getBuildDir(), "generated-sources/jte").toPath();
            } else {
                //noinspection ConstantConditions // according to https://docs.gradle.org/current/javadoc/org/gradle/api/provider/Provider.html#map-org.gradle.api.Transformer- it's ok to return null here
                return null;
            }
        }));
        extension.getContentType().convention(ContentType.Html);
        extension.getBinaryStaticContent().convention(false);
        extension.getPackageName().convention(Constants.PACKAGE_NAME_PRECOMPILED);
        extension.getTargetResourceDirectory().convention(new File(project.getBuildDir(), "generated-resources/jte").toPath());

        extension.getCompilePath().setFrom(main.getRuntimeClasspath());
        extension.getProjectNamespace().convention(project.getGroup() + "/" + project.getName());
    }

    private void configureTask(Project project, String taskName, Action<Task> action) {
        try {
            project.getTasks().named(taskName).configure(action);
        } catch (UnknownTaskException ignore) {
        }
    }

    private boolean isKotlinCompilerEmbeddable(Dependency dependency) {
        return KOTLIN_COMPILER_EMBEDDABLE.equals(dependency.getGroup() + ":" + dependency.getName());
    }
}
