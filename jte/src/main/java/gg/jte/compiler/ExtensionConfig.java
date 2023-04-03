package gg.jte.compiler;

import gg.jte.TemplateConfig;
import gg.jte.extension.JteConfig;

public class ExtensionConfig implements JteConfig {
    private final TemplateConfig config;

    public ExtensionConfig(TemplateConfig config) {
        this.config = config;
    }
}
