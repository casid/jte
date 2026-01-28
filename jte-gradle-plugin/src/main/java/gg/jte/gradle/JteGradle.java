package gg.jte.gradle;

import gg.jte.ContentType;
import gg.jte.runtime.Constants;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

public class JteGradle implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);
        
        SourceSet main = getMainSourceSet(project);
        JteExtension extension = project.getExtensions().create("jte", JteExtension.class, project.getObjects());
        defaults(project, extension, main);

        TaskProvider<PrecompileJteTask> precompileJteTask = project.getTasks().register("precompileJte", PrecompileJteTask.class);
        precompileJteTask.configure(t -> {
            t.dependsOn("compileJava");
            t.wireExtension(extension, project.provider(() -> project.getLayout().getProjectDirectory().dir("jte-classes").getAsFile().toPath()));
        });
        project.getTasks().named("test").configure(t -> t.dependsOn(precompileJteTask));

        TaskProvider<GenerateJteTask> generateJteTask = project.getTasks().register("generateJte", GenerateJteTask.class);
        configureTask(project, "compileJava", t -> t.dependsOn(generateJteTask));
        configureTask(project, "sourcesJar", t -> t.dependsOn(generateJteTask));
        configureTask(project, "processResources", t -> t.dependsOn(generateJteTask));
        configureTask(project, "compileKotlin", t -> t.dependsOn(generateJteTask));

        project.getTasks().named("clean").configure(t -> t.dependsOn("cleanPrecompileJte", "cleanGenerateJte")); // clean tasks are generated based on task outputs

        Configuration additionalClasspath = project.getConfigurations().create("jteGenerate");
        generateJteTask.configure(t -> {
            t.getClasspath().from(additionalClasspath);
            t.wireExtension(extension, project.getLayout().getBuildDirectory().dir("generated-sources/jte").map(d -> d.getAsFile().toPath()));
            if (t.getConfiguredStage().isPresent() && t.getConfiguredStage().get() == JteStage.GENERATE) {
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
        extension.getStage().convention(JteStage.NONE);
        extension.getSourceDirectory().convention(project.getLayout().getProjectDirectory().dir("src/main/jte").getAsFile().toPath()); // TODO can it use sourceset?
        extension.getContentType().convention(ContentType.Html);
        extension.getBinaryStaticContent().convention(false);
        extension.getPackageName().convention(Constants.PACKAGE_NAME_PRECOMPILED);
        extension.getTargetResourceDirectory().convention(project.getLayout().getBuildDirectory().dir("generated-resources/jte").map(d -> d.getAsFile().toPath()));

        // Create configuration to include Kotlin Compiler isolated from user Kotlin version
        String configurationName = "jteKotlinCompiler";
        Configuration kotlinCompiler = project.getConfigurations().create(configurationName);
        project.getDependencies().add(configurationName, "org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.20");

        extension.getCompilePath().setFrom(main.getRuntimeClasspath(), kotlinCompiler);
        extension.getProjectNamespace().convention(project.getGroup() + "/" + project.getName());
    }

    private void configureTask(Project project, String taskName, Action<Task> action) {
        try {
            project.getTasks().named(taskName).configure(action);
        } catch (UnknownTaskException ignore) {
        }
    }
}
