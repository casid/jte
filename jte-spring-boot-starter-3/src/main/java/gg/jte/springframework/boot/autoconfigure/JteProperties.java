package gg.jte.springframework.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gg.jte")
public class JteProperties {
    private Boolean usePrecompiledTemplates = false;
    private String templateLocation = "src/main/jte";
    private String templateSuffix = ".jte";

    public String getTemplateSuffix() {
        return templateSuffix;
    }

    public void setTemplateSuffix(final String templateSuffix) {
        this.templateSuffix = templateSuffix;
    }

    public String getTemplateLocation() {
        return templateLocation;
    }

    public void setTemplateLocation(String templateLocation) {
        this.templateLocation = templateLocation;
    }

    public Boolean usePreCompiledTemplates() {
        return usePrecompiledTemplates;
    }

    public void setUsePrecompiledTemplates(Boolean usePrecompiledTemplates) {
        this.usePrecompiledTemplates = usePrecompiledTemplates;
    }

}
