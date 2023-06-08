package gg.jte.models.generator;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.extension.api.JteConfig;
import gg.jte.extension.api.JteExtension;
import gg.jte.extension.api.TemplateDescription;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ModelExtension implements JteExtension {
    private ModelConfig modelConfig = new ModelConfig(Map.of());

    @Override
    public String name() {
        return "Generate type-safe model facade for templates";
    }

    @Override
    public JteExtension init(Map<String, String> value) {
        modelConfig = new ModelConfig(value);
        return this;
    }

    @Override
    public Collection<Path> generate(JteConfig config, Set<TemplateDescription> templateDescriptions) {
        TemplateEngine engine = TemplateEngine.createPrecompiled(ContentType.Plain);
        return Stream.of(
                new ModelGenerator(engine, "interfacetemplates", "Templates", "Templates"),
                new ModelGenerator(engine, "statictemplates", "StaticTemplates", "Templates"),
                new ModelGenerator(engine, "dynamictemplates", "DynamicTemplates", "Templates")
        ).map(g -> g.generate(config, templateDescriptions, modelConfig))
                .collect(Collectors.toList());
    }
}
