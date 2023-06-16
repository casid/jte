package gg.jte.maven;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Collections;
import java.util.Map;

public class ExtensionSettings {

    /**
     * The class name of the extension, e.g. <code>gg.jte.models.generator.ModelExtension</code>
     */
    @Parameter
    public String className;

    /**
     * The settings the extension should use.
     */
    @Parameter
    public Map<String, String> settings;

    public String getClassName() {
        return className;
    }

    public Map<String, String> getSettings() {
        return settings == null ? Collections.emptyMap() : settings;
    }
}
