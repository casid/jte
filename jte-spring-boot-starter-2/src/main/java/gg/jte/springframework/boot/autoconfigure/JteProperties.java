package gg.jte.springframework.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gg.jte")
public class JteProperties {
    /**
     * You can precompile the templates for faster startup and rendering. To use this you need to include
     * the Maven or Gradle Plugin: <a href="https://jte.gg/pre-compiling/">https://jte.gg/pre-compiling/</a>
     */
    private Boolean usePrecompiledTemplates = false;

    /**
     * You can change the location where jte expects the templates to compile
     */
    private String templateLocation = "src/main/jte";

    /**
     * You can configure the file suffix of jte templates the compiler resolves
     */
    private String templateSuffix = ".jte";

    /**
     * Set whether all request attributes should be added to the model prior to merging with the template
     */
    private boolean exposeRequestAttributes = false;

    /**
     * Set whether to trim control structures, resulting in prettier output.
     */
    private boolean trimControlStructures = false;

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
    
	public boolean isExposeRequestAttributes() {
		return exposeRequestAttributes;
	}

	public void setExposeRequestAttributes(boolean exposeRequestAttributes) {
		this.exposeRequestAttributes = exposeRequestAttributes;
	}

    public boolean isTrimControlStructures() {
        return trimControlStructures;
    }

    public void setTrimControlStructures(boolean trimControlStructures) {
        this.trimControlStructures = trimControlStructures;
    }
}
