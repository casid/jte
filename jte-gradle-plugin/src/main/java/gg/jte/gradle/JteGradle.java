package gg.jte.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JteGradle implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getTasks().register("generatorMojo", GeneratorMojoTask.class);
        project.getTasks().register("compilerMojo", CompilerMojoTask.class);
    }
}
