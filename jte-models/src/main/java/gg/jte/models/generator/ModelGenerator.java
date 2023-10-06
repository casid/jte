package gg.jte.models.generator;

import gg.jte.TemplateEngine;
import gg.jte.extension.api.JteConfig;
import gg.jte.extension.api.TemplateDescription;
import gg.jte.output.WriterOutput;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ModelGenerator {
    private final TemplateEngine engine;
    private final String templateSubDirectory;
    private final String targetClassName;
    private final String interfaceName;
    private final Language language;

    public ModelGenerator(TemplateEngine engine, String templateSubDirectory, String targetClassName, String interfaceName, Language language) {
        this.engine = engine;
        this.templateSubDirectory = templateSubDirectory;
        this.targetClassName = targetClassName;
        this.interfaceName = interfaceName;
        this.language = language;
    }

    public Path generate(JteConfig config, Set<TemplateDescription> templateDescriptions, ModelConfig modelConfig) {
        String fileExtension = language == Language.JAVA ? ".java" : ".kt";
        String templateName = language == Language.JAVA ? "/main.jte" : "/kmain.jte";

        Path sourceFilePath = config.generatedSourcesRoot()
                .resolve(config.packageName().replace('.', '/'))
                .resolve(targetClassName + fileExtension);
        Iterable<String> imports = templateDescriptions.stream()
                .flatMap(t -> t.imports().stream())
                .collect(Collectors.toCollection(TreeSet::new));
        try {
            Files.createDirectories(sourceFilePath.getParent());
            try (Writer w = new FileWriter(sourceFilePath.toFile())) {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("targetClassName", targetClassName);
                paramMap.put("interfaceName", interfaceName);
                paramMap.put("config", config);
                paramMap.put("templates", templateDescriptions);
                paramMap.put("imports", imports);
                paramMap.put("modelConfig", modelConfig);
                engine.render(templateSubDirectory + templateName, paramMap, new SquashBlanksOutput(new WriterOutput(w)));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sourceFilePath;
    }
}
