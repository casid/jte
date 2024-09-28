package gg.jte.springframework.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gg.jte")
public class JteProperties {
    private boolean usePrecompiledTemplates = false;
    private boolean developmentMode = false;
    private String templateLocation = "src/main/jte";
    private String templateSuffix = ".jte";
    private boolean exposeRequestAttributes = false;

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

    public boolean usePreCompiledTemplates() {
        return usePrecompiledTemplates;
    }

    public void setUsePrecompiledTemplates(Boolean usePrecompiledTemplates) {
        this.usePrecompiledTemplates = usePrecompiledTemplates;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public void setDevelopmentMode(boolean developmentMode) {
        this.developmentMode = developmentMode;
    }

	public boolean isUsePrecompiledTemplates() {
		return usePrecompiledTemplates;
	}

	public void setUsePrecompiledTemplates(boolean usePrecompiledTemplates) {
		this.usePrecompiledTemplates = usePrecompiledTemplates;
	}

	public boolean isExposeRequestAttributes() {
		return exposeRequestAttributes;
	}

	public void setExposeRequestAttributes(boolean exposeRequestAttributes) {
		this.exposeRequestAttributes = exposeRequestAttributes;
	}
    
}
