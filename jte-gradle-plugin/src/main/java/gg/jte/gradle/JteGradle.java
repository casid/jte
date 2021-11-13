package gg.jte.gradle;

import gg.jte.ContentType;
import gg.jte.runtime.Constants;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class JteGradle implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        SourceSet main = getMainSourceSet(project);
        JteExtension extension = project.getExtensions().create("jte", JteExtension.class);
        defaults(project, extension, main);

        TaskProvider<PrecompileJteTask> precompileJteTask = project.getTasks().register("precompileJte", PrecompileJteTask.class, extension);
        precompileJteTask.configure(t -> t.dependsOn("compileJava"));
        project.getTasks().named("test").configure(t -> t.dependsOn(precompileJteTask));

        TaskProvider<GenerateJteTask> generateJteTask = project.getTasks().register("generateJte", GenerateJteTask.class, extension);
        project.getTasks().named("compileJava").configure(t -> t.dependsOn(generateJteTask));
        try {
            project.getTasks().named("compileKotlin").configure(t -> t.dependsOn(generateJteTask));
        } catch (UnknownTaskException ignore) {
        }

        project.getTasks().named("clean").configure(t -> t.dependsOn("cleanPrecompileJte", "cleanGenerateJte")); // clean tasks are generated based on task outputs

        generateJteTask.configure(t -> {
            if (extension.getStage().isPresent() && extension.getStage().get() == JteStage.GENERATE) {
                main.getJava().srcDir(t.getTargetDirectory());
                main.getResources().srcDir(t.getTargetResourceDirectory());
            }
        });
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
    }
}
