package gg.jte.gradle;

import gg.jte.ContentType;
import gg.jte.runtime.Constants;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

@SuppressWarnings("UnstableApiUsage")
public class JteGradle implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        JteExtension extension = project.getExtensions().create("jte", JteExtension.class);
        _defaults(project, extension);
        project.getTasks().register("precompileJte", PrecompileJteTask.class, extension);
        project.getTasks().register("generateJte", GenerateJteTask.class, extension);
    }

    private void _defaults(Project project, JteExtension extension)
    {
        extension.getSourceDirectory().convention(project.file("src/main/jte").toPath()); // TODO can it use sourceset?
        extension.getTargetDirectory().convention(extension.getStage().map(stage -> {
            if (stage == JteStage.PRECOMPILE)
            {
                return project.file("jte-classes").toPath();
            }
            else if (stage == JteStage.GENERATE)
            {
                return new File(project.getBuildDir(), "generated-sources/jte").toPath();
            }
            else
            {
                //noinspection ConstantConditions // according to https://docs.gradle.org/current/javadoc/org/gradle/api/provider/Provider.html#map-org.gradle.api.Transformer- it's ok to return null here
                return null;
            }
        }));
        extension.getContentType().convention(ContentType.Html);
        extension.getBinaryStaticContent().convention(false);
        extension.getPackageName().convention(Constants.PACKAGE_NAME_PRECOMPILED);
        extension.getTargetResourceDirectory().convention(new File(project.getBuildDir(), "generated-resources/jte").toPath());
    }
}
