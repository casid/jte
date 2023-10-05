package gg.jte.models.generator;

import java.util.Map;
import java.util.regex.Pattern;

public class ModelConfig {
    private final Map<String, String> map;

    public ModelConfig(Map<String, String> value) {
        map = value;
    }

    public String interfaceAnnotation() {
        return map.getOrDefault("interfaceAnnotation", "");
    }

    public String implementationAnnotation() {
        return map.getOrDefault("implementationAnnotation", "");
    }

    public Language language() {
        String configuredLanguage = map.getOrDefault("language", "JAVA").toUpperCase();
        Language language = Language.JAVA;
        try {
            language = Language.valueOf(configuredLanguage);
        } catch (IllegalArgumentException ex) {
            // how to report wrong conciguration? fail or default to Java?
        }
        return language;
    }

    public Pattern includePattern() {
        String includePattern = map.get("includePattern");
        if (includePattern == null) {
            return null;
        }
        return Pattern.compile(includePattern);
    }

    public Pattern excludePattern() {
        String excludePattern = map.get("excludePattern");
        if (excludePattern == null) {
            return null;
        }
        return Pattern.compile(excludePattern);
    }
}
