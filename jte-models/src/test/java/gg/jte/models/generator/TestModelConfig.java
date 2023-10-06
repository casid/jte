package gg.jte.models.generator;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var modelConfig = new ModelConfig(Map.of("language", "Java"));
        assertEquals(modelConfig.language(), Language.Java);
    }

    @Test
    public void languageConfigurationDefaultsToJava() {
        var modelConfig = new ModelConfig(Map.of());
        assertEquals(modelConfig.language(), Language.Java);
    }

    @Test
    public void languageConfigurationSupportsKotlin() {
        var modelConfig = new ModelConfig(Map.of("language", "Kotlin"));
        assertEquals(modelConfig.language(), Language.Kotlin);
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
            "JTE ModelExtension 'language' property is not configured correctly (current value is 'Ooops'). Supported values: [Java, Kotlin]",
            exception.getMessage()
        );
    }
}
