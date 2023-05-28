package gg.jte.models.generator;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.extension.JteConfig;
import gg.jte.extension.JteExtension;
import gg.jte.extension.TemplateDescription;
import gg.jte.resolve.ResourceCodeResolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ModelExtension implements JteExtension {
    @Override
    public String name() {
        return "Generate type-safe model facade for templates";
    }

    @Override
    public Collection<Path> generate(JteConfig config, Set<TemplateDescription> templateDescriptions) {
        TemplateEngine engine = TemplateEngine.createPrecompiled(ContentType.Plain);
        return Stream.of(
                new ModelGenerator(engine, "interfacetemplates", "Templates", "Templates"),
                new ModelGenerator(engine, "statictemplates", "StaticTemplates", "Templates"),
                new ModelGenerator(engine, "dynamictemplates", "DynamicTemplates", "Templates")
        ).map(g -> g.generate(config, templateDescriptions))
                .collect(Collectors.toList());
    }
}
