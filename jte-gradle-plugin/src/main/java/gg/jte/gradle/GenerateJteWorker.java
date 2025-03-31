package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.workers.WorkAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public abstract class GenerateJteWorker implements WorkAction<GenerateJteParams> {
    @Override
    public void execute() {
        Logger logger = LoggerFactory.getLogger(GenerateJteWorker.class);
        long start = System.nanoTime();

        GenerateJteParams params = getParameters();

        Path sourceDirectory = path(params.getSourceDirectory());
        Path targetDirectory = path(params.getTargetDirectory());

        logger.info("Generating jte templates found in {}", sourceDirectory);

        TemplateEngine templateEngine = TemplateEngine.create(
                new DirectoryCodeResolver(sourceDirectory),
                targetDirectory,
                params.getContentType().get(),
                null,
                params.getPackageName().get());
        templateEngine.setTrimControlStructures(params.getTrimControlStructures().getOrElse(false));
        templateEngine.setHtmlTags(params.getHtmlTags().getOrNull());
        templateEngine.setHtmlCommentsPreserved(params.getHtmlCommentsPreserved().getOrElse(false));
        templateEngine.setBinaryStaticContent(params.getBinaryStaticContent().getOrElse(false));
        templateEngine.setTargetResourceDirectory(path(params.getTargetResourceDirectory()));
        templateEngine.setProjectNamespace(params.getProjectNamespace().getOrNull());
        templateEngine.setExtensions(params.getJteExtensions().get());

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
        logger.info("Successfully generated {} jte file{} in {}s to {}", amount, amount == 1 ? "" : "s", duration, targetDirectory);
    }

    private static Path path(RegularFileProperty property) {
        return property.getAsFile().get().toPath();
    }
}
