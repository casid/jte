package gg.jte.migrate;

import gg.jte.output.FileOutput;
import gg.jte.resolve.DirectoryCodeResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

public class MigrateV1To2 {
    public static void migrateTemplates(Path sourceDirectory) {
        System.out.println("Migrating jte templates from v1 to v2...");
        System.out.println("Source root directory: " + sourceDirectory);

        DirectoryCodeResolver directoryCodeResolver = new DirectoryCodeResolver(sourceDirectory);
        List<String> templateNames = directoryCodeResolver.resolveAllTemplateNames();

        for (String templateName : templateNames) {
            migrateTemplate(sourceDirectory, directoryCodeResolver, templateName);
        }

        System.out.println("Migration complete.");
    }

    private static void migrateTemplate(Path sourceDirectory, DirectoryCodeResolver directoryCodeResolver, String templateName) {
        String templateCode = directoryCodeResolver.resolve(templateName);

        String migratedTemplateCode = replaceUsages(templateCode);

        if (!templateCode.equals(migratedTemplateCode)) {
            writeToFile(templateName, migratedTemplateCode, sourceDirectory.resolve(templateName));
            System.out.println("Migrated " + templateName);
        }
    }

    private static String replaceUsages(String templateCode) {
        templateCode = templateCode.replace("@tag.", "@template.tag.");
        templateCode = templateCode.replace("@layout.", "@template.layout.");
        return templateCode;
    }

    private static void writeToFile(String templateName, String migratedTemplateCode, Path templateFile) {
        try (FileOutput fileOutput = new FileOutput(templateFile)) {
            fileOutput.writeContent(migratedTemplateCode);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write template " + templateName, e);
        }
    }
}
