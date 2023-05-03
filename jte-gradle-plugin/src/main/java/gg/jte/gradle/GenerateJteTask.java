package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class GenerateJteTask extends JteTaskBase {

    @Inject
    public GenerateJteTask(JteExtension extension) {
        super(extension, JteStage.GENERATE);
    }

    @Override
    public Path getTargetDirectory()
    {
        if (!extension.getStage().isPresent())
        {
            extension.getStage().set(JteStage.GENERATE);
        }
        return super.getTargetDirectory();
    }

    @Input
    public boolean getGenerateNativeImageResources() {
        return extension.getGenerateNativeImageResources().getOrElse(false);
    }

    public void setGenerateNativeImageResources(boolean value) {
        extension.getGenerateNativeImageResources().set(value);
        setterCalled();
    }

    @TaskAction
    public void execute() {
        Logger logger = getLogger();
        long start = System.nanoTime();

        Path sourceDirectory = getSourceDirectory();
        Path targetDirectory = getTargetDirectory();
        logger.info("Generating jte templates found in " + sourceDirectory);

        TemplateEngine templateEngine = TemplateEngine.create(
                new DirectoryCodeResolver(sourceDirectory),
                targetDirectory,
                getContentType(),
                null,
                getPackageName());
        templateEngine.setTrimControlStructures(Boolean.TRUE.equals(getTrimControlStructures()));
        templateEngine.setHtmlTags(getHtmlTags());
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE.equals(getHtmlCommentsPreserved()));
        templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(getBinaryStaticContent()));
        templateEngine.setTargetResourceDirectory(getTargetResourceDirectory());
        templateEngine.setGenerateNativeImageResources(getGenerateNativeImageResources());
        templateEngine.setProjectNamespace(extension.getProjectNamespace().getOrNull());

        int amount;
        try {
            templateEngine.cleanAll();
            amount = templateEngine.generateAll().size();
        } catch (Exception e) {
            logger.error("Failed to generate templates.", e);

            throw e;
        }

        long end = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toSeconds(end - start);
        logger.info("Successfully generated " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + duration + "s to " + targetDirectory);
    }


}
