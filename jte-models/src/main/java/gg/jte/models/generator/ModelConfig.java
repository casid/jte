package gg.jte.models.generator;

import java.util.Map;

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
}
