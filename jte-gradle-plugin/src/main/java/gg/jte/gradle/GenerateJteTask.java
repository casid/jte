package gg.jte.gradle;

import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class GenerateJteTask extends JteTaskBase {


    public GenerateJteTask() {
        targetDirectory = Paths.get(getProject().getBuildDir().getAbsolutePath(), "generated-sources", "jte");
        targetResourceDirectory = Paths.get(getProject().getBuildDir().getAbsolutePath(), "generated-resources", "jte");
    }

    @TaskAction
    public void execute() {
        Logger logger = getLogger();
        long start = System.nanoTime();

        logger.info("Generating jte templates found in " + sourceDirectory);

        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourceDirectory), targetDirectory, contentType, null, packageName);
        templateEngine.setTrimControlStructures(Boolean.TRUE.equals(trimControlStructures));
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlAttributes(htmlAttributes);
        templateEngine.setHtmlCommentsPreserved(Boolean.TRUE.equals(htmlCommentsPreserved));
        templateEngine.setBinaryStaticContent(Boolean.TRUE.equals(binaryStaticContent));
        templateEngine.setTargetResourceDirectory(targetResourceDirectory);

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
