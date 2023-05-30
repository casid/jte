package gg.jte.maven;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Collections;
import java.util.Map;

public class ExtensionSettings {
    @Parameter
    private String className;

    @Parameter
    private Map<String, String> settings;

    public String getClassName() {
        return className;
    }

    public Map<String, String> getSettings() {
        return settings == null ? Collections.emptyMap() : settings;
    }
}
