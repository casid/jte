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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ModelExtension implements JteExtension {
    private ModelConfig modelConfig = new ModelConfig(Map.of());

    @Override
    public String name() {
        return "Generate type-safe model facade for templates in Java";
    }

    @Override
    public JteExtension init(Map<String, String> value) {
        modelConfig = new ModelConfig(value);
        return this;
    }

    @Override
    public Collection<Path> generate(JteConfig config, Set<TemplateDescription> templateDescriptions) {
        TemplateEngine engine = TemplateEngine.createPrecompiled(ContentType.Plain);

        Pattern includePattern = modelConfig.includePattern();
        Pattern excludePattern = modelConfig.excludePattern();

        Language language = modelConfig.language();

        var templateDescriptionsFiltered = templateDescriptions.stream() //
                .filter(x -> includePattern == null || includePattern.matcher(x.fullyQualifiedClassName()).matches()) //
                .filter(x -> excludePattern == null || !excludePattern.matcher(x.fullyQualifiedClassName()).matches()) //
                .collect(Collectors.toSet());

        var interfaceName = modelConfig.interfaceName();
        return Stream.of(
                new ModelGenerator(engine, "interfacetemplates", interfaceName, interfaceName, language),
                new ModelGenerator(engine, "statictemplates", "Static" + interfaceName, interfaceName, language),
                new ModelGenerator(engine, "dynamictemplates", "Dynamic" + interfaceName, interfaceName, language)
        ).map(g -> g.generate(config, templateDescriptionsFiltered, modelConfig))
                .toList();
    }
}
