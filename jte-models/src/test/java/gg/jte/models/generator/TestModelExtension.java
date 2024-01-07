package gg.jte.models.generator;

import gg.jte.ContentType;
import gg.jte.extension.api.JteConfig;
import gg.jte.extension.api.JteExtension;
import gg.jte.extension.api.TemplateDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static gg.jte.extension.api.mocks.MockConfig.mockConfig;
import static gg.jte.extension.api.mocks.MockParamDescription.mockParamDescription;
import static gg.jte.extension.api.mocks.MockTemplateDescription.mockTemplateDescription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("jte-models Extension tests")
public class TestModelExtension {

    private static JteConfig config(Path destination, String packageName) {
        return mockConfig()
                .contentType(ContentType.Plain)
                .generatedSourcesRoot(destination)
                .packageName(packageName);
    }

    @Nested
    class TargetJava {
        private static final String TEST_PACKAGE = "test.myjtemplates";
        public static final Pattern INTERFACE_SOURCE_FILE = Pattern.compile("target.generated-test-sources." + TEST_PACKAGE + ".Templates.java");
        public static final Pattern DYNAMIC_SOURCE_FILE = Pattern.compile("target.generated-test-sources." + TEST_PACKAGE + ".DynamicTemplates.java");
        public static final Pattern STATIC_SOURCE_FILE = Pattern.compile("target.generated-test-sources." + TEST_PACKAGE + ".StaticTemplates.java");

        @Test
        public void generateJavaFacades() {
            // Given
            JteExtension modelExtension = new ModelExtension();
            TemplateDescription templateDescription = mockTemplateDescription()
                    .packageName(TEST_PACKAGE)
                    .name("hello.jte")
                    .className("JtehelloGenerated")
                    .addParams(mockParamDescription()
                            .type("java.lang.String")
                            .name("message"));


            // When
            Collection<Path> generatedPaths = modelExtension.generate(
                    config(Paths.get("target/generated-test-sources"), TEST_PACKAGE),
                    Collections.singleton(templateDescription)
            );

            // Then
            assertEquals(
                    3,
                    generatedPaths.size(),
                    "Three files (Interfaces, Dynamic, and Static) templates must be generated"
            );

            assertThat(generatedPaths)
                    .withFailMessage("JAVA: Templates interface file %s should be generated", INTERFACE_SOURCE_FILE)
                    .anyMatch(path -> INTERFACE_SOURCE_FILE.matcher(path.toString()).find());

            assertThat(generatedPaths)
                    .withFailMessage("JAVA: Templates static file %s should be generated", DYNAMIC_SOURCE_FILE)
                    .anyMatch(path -> DYNAMIC_SOURCE_FILE.matcher(path.toString()).find());

            assertThat(generatedPaths)
                    .withFailMessage("JAVA: Templates static file %s should be generated", STATIC_SOURCE_FILE)
                    .anyMatch(path -> STATIC_SOURCE_FILE.matcher(path.toString()).find());

            generatedPaths.forEach(path -> {
                try {
                    String contents = Files.readString(path);
                    assertThat(contents).contains("JteModel hello(java.lang.String message)");
                } catch (IOException ex) {
                    fail("Could not read file " + path, ex);
                }
            });
        }
    }

    @Nested
    class TargetKotlin {
        private static final String TEST_PACKAGE = "test.myktemplates";
        public static final Pattern INTERFACE_SOURCE_FILE = Pattern.compile("target.generated-test-sources." + TEST_PACKAGE + ".Templates.kt");
        public static final Pattern DYNAMIC_SOURCE_FILE = Pattern.compile("target.generated-test-sources." + TEST_PACKAGE + ".DynamicTemplates.kt");
        public static final Pattern STATIC_SOURCE_FILE = Pattern.compile("target.generated-test-sources." + TEST_PACKAGE + ".StaticTemplates.kt");

        private JteExtension kotlinModelExtension() {
            JteExtension modelExtension = new ModelExtension();
            Map<String, String> config = new HashMap<>();
            config.put("language", Language.Kotlin.toString());
            modelExtension.init(config);

            return modelExtension;
        }

        @Test
        public void generatesKotlinFacades() {
            // Given
            JteExtension modelExtension = kotlinModelExtension();
            TemplateDescription templateDescription = mockTemplateDescription()
                    .packageName(TEST_PACKAGE)
                    .name("hello.kte")
                    .className("JtehelloGenerated")
                    .addParams(mockParamDescription()
                            .type("java.lang.String")
                            .name("message"));

            // When
            Collection<Path> generatedPaths = modelExtension.generate(
                    config(Paths.get("target/generated-test-sources"), TEST_PACKAGE),
                    Collections.singleton(templateDescription)
            );

            // Then
            assertEquals(
                    3,
                    generatedPaths.size(),
                    "Three files (Interfaces, Dynamic, and Static) templates must be generated"
            );

            assertThat(generatedPaths)
                    .withFailMessage("KOTLIN: Templates interface file %s should be generated", INTERFACE_SOURCE_FILE)
                    .anyMatch(path -> INTERFACE_SOURCE_FILE.matcher(path.toString()).find());

            assertThat(generatedPaths)
                    .withFailMessage("KOTLIN: Templates static file %s should be generated", DYNAMIC_SOURCE_FILE)
                    .anyMatch(path -> DYNAMIC_SOURCE_FILE.matcher(path.toString()).find());

            assertThat(generatedPaths)
                    .withFailMessage("KOTLIN: Templates static file %s should be generated", STATIC_SOURCE_FILE)
                    .anyMatch(path -> STATIC_SOURCE_FILE.matcher(path.toString()).find());

            generatedPaths.forEach(path -> {
                try {
                    assertThat(Files.readString(path)).contains("fun hello(message: java.lang.String): JteModel");
                } catch (IOException ex) {
                    fail("Could not read file " + path, ex);
                }
            });
        }

        @Test
        public void generatesKotlinFacadesWhenParamHasDefaultValue() {
            // Given
            JteExtension modelExtension = kotlinModelExtension();
            TemplateDescription templateDescription = mockTemplateDescription()
                    .packageName(TEST_PACKAGE)
                    .name("hello.kte")
                    .className("JtehelloGenerated")
                    .addParams(mockParamDescription()
                            .type("Int")
                            .name("value")
                            .defaultValue("10"));

            // When
            Collection<Path> generatedPaths = modelExtension.generate(
                    config(Paths.get("target/generated-test-sources"), TEST_PACKAGE),
                    Collections.singleton(templateDescription)
            );

            // Then
            Optional<Path> interfaceFacadeFile = generatedPaths.stream()
                    .filter(path -> INTERFACE_SOURCE_FILE.matcher(path.toString()).find())
                    .findFirst();
            assertThat(interfaceFacadeFile).isNotEmpty();
            assertThat(interfaceFacadeFile.get()).content().contains("fun hello(value: Int = 10): JteModel");

            // Implementations should add `override` and NOT include the default value.
            List<Path> implementationsPaths = generatedPaths.stream()
                    .filter(path ->
                        DYNAMIC_SOURCE_FILE.matcher(path.toString()).find() ||
                        STATIC_SOURCE_FILE.matcher(path.toString()).find()
                    )
                    .toList();

            assertThat(implementationsPaths).isNotEmpty();
            implementationsPaths.forEach(path -> {
                try {
                    assertThat(Files.readString(path)).contains("override fun hello(value: Int): JteModel");
                } catch (IOException ex) {
                    fail("Could not read file " + path, ex);
                }
            });
        }

        @Test
        public void generatesKotlinWithoutDefaultValueForContentParams() {
            // Given
            JteExtension modelExtension = kotlinModelExtension();
            TemplateDescription templateDescription = mockTemplateDescription()
                    .packageName(TEST_PACKAGE)
                    .name("hello.kte")
                    .className("JtehelloGenerated")
                    .addParams(mockParamDescription()
                            .type("gg.jte.Content")
                            .name("content")
                            .defaultValue("@`Some Content`"));

            // When
            Collection<Path> generatedPaths = modelExtension.generate(
                    config(Paths.get("target/generated-test-sources"), TEST_PACKAGE),
                    Collections.singleton(templateDescription)
            );

            // Then
            generatedPaths.forEach(path -> {
                try {
                    assertThat(Files.readString(path)).contains("fun hello(content: gg.jte.Content): JteModel");
                } catch (IOException ex) {
                    fail("Could not read file " + path, ex);
                }
            });
        }

        @Test
        public void generatesKotlinFacadesWithNoParameters() {
            // Given
            JteExtension modelExtension = kotlinModelExtension();
            TemplateDescription templateDescription = mockTemplateDescription()
                    .packageName(TEST_PACKAGE)
                    .name("hello.kte")
                    .className("JtehelloGenerated");

            // When
            Collection<Path> generatedPaths = modelExtension.generate(
                    config(Paths.get("target/generated-test-sources"), TEST_PACKAGE),
                    Collections.singleton(templateDescription)
            );

            // Then
            generatedPaths.forEach(path -> {
                try {
                    assertThat(Files.readString(path)).contains("fun hello(): JteModel");
                } catch (IOException ex) {
                    fail("Could not read file " + path, ex);
                }
            });

            // Dynamic has a map with explicit generics types declared.
            // This is relevant for empty maps (templates with no params).
            assertThat(generatedPaths).anySatisfy(path -> {
                try {
                    assertThat(Files.readString(path)).contains("val paramMap = mapOf<String, Any?>");
                } catch (IOException ex) {
                    fail("Could not read file " + path, ex);
                }
            });
        }
    }
}
