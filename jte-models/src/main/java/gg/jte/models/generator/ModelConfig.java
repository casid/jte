package gg.jte.models.generator;

import java.util.Arrays;
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
        String configuredLanguage = map.getOrDefault("language", "Java");
        try {
            return Language.valueOf(configuredLanguage);
        } catch (IllegalArgumentException ex) {
            String supportedValues = Arrays.toString(Language.values());
            throw new IllegalArgumentException(
                String.format("jte ModelExtension 'language' property is not configured correctly (current value is '%s'). Supported values: %s", configuredLanguage, supportedValues),
                ex
            );
        }
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
