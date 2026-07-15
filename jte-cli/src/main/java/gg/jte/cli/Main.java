package gg.jte.cli;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.runtime.Constants;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "jte-cli", mixinStandardHelpOptions = true,
        description = "Generates Java sources from jte templates, without needing Maven or Gradle.")
public class Main implements Callable<Integer> {

    @Option(names = {"-s", "--source-directory"}, required = true, description = "The directory where template files are located.")
    public Path sourceDirectory;

    @Option(names = {"-t", "--target-directory"}, required = true, description = "Destination directory to store generated sources.")
    public Path targetDirectory;

    @Option(names = {"-c", "--content-type"}, required = true, description = "The content type of all templates: ${COMPLETION-CANDIDATES}.")
    public ContentType contentType;

    @Option(names = "--trim-control-structures", description = "Trims control structures, resulting in prettier output.")
    public boolean trimControlStructures;

    @Option(names = "--html-tags", split = ",", description = "Intercepts the given html tags during template compilation.")
    public String[] htmlTags;

    @Option(names = "--preserve-html-comments", description = "Preserves HTML/CSS/JS comments, which are omitted by default when using Html content type.")
    public boolean htmlCommentsPreserved;

    @Option(names = "--binary-static-content", description = "UTF-8 encodes all static template parts at compile time.")
    public boolean binaryStaticContent;

    @Option(names = "--package-name", description = "The package name, where template classes are generated to. Defaults to ${DEFAULT-VALUE}.")
    public String packageName = Constants.PACKAGE_NAME_PRECOMPILED;

    @Option(names = "--target-resource-directory", description = "Directory in which to generate non-java files (resources).")
    public Path targetResourceDirectory;

    public static void main(String[] args) {
        System.exit(newCommandLine(new Main()).execute(args));
    }

    static CommandLine newCommandLine(Main main) {
        CommandLine commandLine = new CommandLine(main);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        return commandLine;
    }

    @Override
    public Integer call() {
        if (!Files.isDirectory(sourceDirectory)) {
            System.err.println("Error: source directory " + sourceDirectory + " does not exist.");
            return 1;
        }

        try {
            Files.createDirectories(targetDirectory);
        } catch (IOException e) {
            System.err.println("Error: target directory " + targetDirectory + " is not writable: " + e.getMessage());
            return 1;
        }

        long start = System.nanoTime();
        int amount = run();
        long durationMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Successfully generated " + amount + " jte file" + (amount == 1 ? "" : "s") + " in " + durationMs + "ms to " + targetDirectory);
        return 0;
    }

    int run() {
        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(sourceDirectory), targetDirectory, contentType, null, packageName);
        templateEngine.setTrimControlStructures(trimControlStructures);
        templateEngine.setHtmlTags(htmlTags);
        templateEngine.setHtmlCommentsPreserved(htmlCommentsPreserved);
        templateEngine.setBinaryStaticContent(binaryStaticContent);
        if (targetResourceDirectory != null) {
            templateEngine.setTargetResourceDirectory(targetResourceDirectory);
        }

        templateEngine.cleanAll();
        return templateEngine.generateAll().size();
    }
}
