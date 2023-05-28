package gg.jte.compiler.extensionsupport;

import gg.jte.ContentType;
import gg.jte.TemplateConfig;
import gg.jte.extension.api.JteConfig;

import java.nio.file.Path;

public class ExtensionConfig implements JteConfig {
    private final TemplateConfig config;
    private final Path sourcesRoot;
    private final ClassLoader classLoader;

    public ExtensionConfig(TemplateConfig config, Path sourcesRoot, ClassLoader classLoader) {
        this.config = config;
        this.sourcesRoot = sourcesRoot;
        this.classLoader = classLoader;
    }

    @Override
    public Path generatedSourcesRoot() {
        return sourcesRoot;
    }

    @Override
    public Path generatedResourcesRoot() {
        return config.resourceDirectory == null ? sourcesRoot : config.resourceDirectory;
    }

    @Override
    public String projectNamespace() {
        return config.projectNamespace;
    }

    @Override
    public String packageName() {
        return config.packageName;
    }

    @Override
    public ContentType contentType() {
        return config.contentType;
    }

    @Override
    public ClassLoader classLoader() {
        return classLoader;
    }
}
