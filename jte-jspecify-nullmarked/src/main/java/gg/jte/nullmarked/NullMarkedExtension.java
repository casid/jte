package gg.jte.nullmarked;

import gg.jte.extension.api.JteConfig;
import gg.jte.extension.api.JteExtension;
import gg.jte.extension.api.TemplateDescription;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NullMarkedExtension implements JteExtension {

    @Override
    public String name() {
        return "NullMarked package-info generator";
    }

    @Override
    public Collection<Path> generate(JteConfig config, Set<TemplateDescription> templateDescriptions) {
        if (config.generatedSourcesRoot() == null || templateDescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        return templateDescriptions.stream()
                .map(TemplateDescription::packageName)
                .distinct()
                .map(pkg -> writePackageInfo(config.generatedSourcesRoot(), pkg))
                .collect(Collectors.toList());
    }

    private Path writePackageInfo(Path sourcesRoot, String packageName) {
        Path packageDir = sourcesRoot.resolve(packageName.replace('.', '/'));
        Path file = packageDir.resolve("package-info.java");
        if (Files.exists(file)) {
            return file;
        }
        try {
            Files.createDirectories(packageDir);
            Files.writeString(file,
                    "@NullMarked\npackage " + packageName + ";\n\nimport org.jspecify.annotations.NullMarked;\n");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return file;
    }
}
