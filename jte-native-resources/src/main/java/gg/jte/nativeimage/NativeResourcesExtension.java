package gg.jte.nativeimage;

import gg.jte.extension.api.JteConfig;
import gg.jte.extension.api.JteExtension;
import gg.jte.extension.api.TemplateDescription;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NativeResourcesExtension implements JteExtension {
    @Override
    public String name() {
        return "native-image resource generator";
    }

    @Override
    public Collection<Path> generate(JteConfig config, Set<TemplateDescription> templateDescriptions) {
        if (config.generatedResourcesRoot() == null || templateDescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        String namespace = config.projectNamespace();
        if (namespace == null) {
            namespace = config.packageName();
        }

        Path resourceRoot = config.generatedResourcesRoot().resolve("META-INF/native-image/jte-generated/" + namespace);
        try {
            Files.createDirectories(resourceRoot);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Arrays.asList(
            writeFile(resourceRoot.resolve("native-image.properties"), "Args = -H:ReflectionConfigurationResources=${.}/reflection-config.json -H:ResourceConfigurationResources=${.}/resource-config.json\n"),
            writeFile(resourceRoot.resolve("resource-config.json"), "{\"resources\": {\"includes\": [{\"pattern\": \".*Generated\\\\.bin$\"}]}}\n"),
            writeFile(resourceRoot.resolve("reflection-config.json"),
                    templateDescriptions.stream()
                            .map(TemplateDescription::fullyQualifiedClassName)
                            .map("{\"name\":\"%s\", \"allDeclaredMethods\":true, \"allDeclaredFields\":true}"::formatted)
                            .collect(Collectors.joining(",\n", "[\n", "\n]\n"))
            )
        );
    }

    private Path writeFile(Path path, String content) {
        try (Writer w = new FileWriter(path.toFile())) {
            w.write(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return path;
    }
}
