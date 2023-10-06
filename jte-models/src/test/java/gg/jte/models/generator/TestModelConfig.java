package gg.jte.models.generator;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestModelConfig {

    @Test
    public void configureInterfaceAnnotation() {
        var modelConfig = new ModelConfig(Map.of("interfaceAnnotation", "@Deprecated"));
        assertEquals(modelConfig.interfaceAnnotation(), "@Deprecated");
    }

    @Test
    public void interfaceAnnotationBlankWhenConfigurationNotPresent() {
        var modelConfig = new ModelConfig(Map.of());
        assertEquals("", modelConfig.interfaceAnnotation());
    }

    @Test
    public void configureImplementationAnnotation() {
        var modelConfig = new ModelConfig(Map.of("implementationAnnotation", "@Singleton"));
        assertEquals("@Singleton", modelConfig.implementationAnnotation());
    }

    @Test
    public void implementationAnnotationNullWhenConfigurationNotPresent() {
        var modelConfig = new ModelConfig(Map.of());
        assertEquals("", modelConfig.implementationAnnotation());
    }

    @Test
    public void languageConfigurationSupportsJava() {
        var modelConfig = new ModelConfig(Map.of("language", "JAVA"));
        assertEquals(modelConfig.language(), Language.JAVA);
    }

    @Test
    public void languageConfigurationDefaultsToJava() {
        var modelConfig = new ModelConfig(Map.of());
        assertEquals(modelConfig.language(), Language.JAVA);
    }

    @Test
    public void languageConfigurationSupportsKotlin() {
        var modelConfig = new ModelConfig(Map.of("language", "KOTLIN"));
        assertEquals(modelConfig.language(), Language.KOTLIN);
    }

    @Test
    public void languageConfigurationIsCaseInsensitive() {
        var modelConfig = new ModelConfig(Map.of("language", "jAvA"));
        assertEquals(modelConfig.language(), Language.JAVA);
    }

    @Test
    public void languageConfigurationFailsWhenLanguageIsNotSupported() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new ModelConfig(Map.of("language", "Ooops")).language();
        });

        assertEquals(
            "JTE ModelExtension 'language' property is not configured correctly (current value is 'Ooops'). Supported values: [JAVA, KOTLIN]",
            exception.getMessage()
        );
    }
}
