package gg.jte.springframework.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gg.jte")
public class JteProperties {

    /**
     * You can precompile the templates for faster startup and rendering. To use this you need to include
     * the Maven or Gradle Plugin: <a href="https://jte.gg/pre-compiling/">https://jte.gg/pre-compiling/</a>
     */
    private boolean usePrecompiledTemplates = false;

    /**
     * You can enable development mode so that the jte file watcher will watch for changes in templates and recompile them.
     */
    private boolean developmentMode = false;

    /**
     * You can change the location where jte expects the templates to compile
     */
    private String templateLocation = "src/main/jte";

    /**
     * Directory for runtime-compiled templates. Relative paths are resolved against the working directory.
     */
    private String classDirectory = "jte-classes";

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

    /**
     * Error view resolution (servlet only): resolve error pages from jte templates.
     */
    private final ErrorView errorView = new ErrorView();

    public ErrorView getErrorView() {
        return errorView;
    }

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

    public String getClassDirectory() {
        return classDirectory;
    }

    public void setClassDirectory(String classDirectory) {
        this.classDirectory = classDirectory;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public void setDevelopmentMode(boolean developmentMode) {
        this.developmentMode = developmentMode;
    }

	public boolean usePreCompiledTemplates() {
		return usePrecompiledTemplates;
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

    public boolean isTrimControlStructures() {
        return trimControlStructures;
    }

    public void setTrimControlStructures(boolean trimControlStructures) {
        this.trimControlStructures = trimControlStructures;
    }

    public static class ErrorView {

        /**
         * Whether error pages are resolved from jte templates, e.g. error/404.jte, falling back to error/4xx.jte and error/5xx.jte.
         */
        private boolean enabled = true;

        /**
         * Template directory (without suffix) where error templates are looked up.
         */
        private String path = "error";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
