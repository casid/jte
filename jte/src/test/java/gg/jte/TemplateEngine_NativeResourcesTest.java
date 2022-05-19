package gg.jte;

import gg.jte.compiler.IoUtils;
import gg.jte.runtime.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("SameParameterValue")
public class TemplateEngine_NativeResourcesTest {

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver, ContentType.Html);
    Path resourceDirectory;

    @BeforeEach
    void setUp() {
        resourceDirectory = Paths.get("jte-classes");
        templateEngine.setTargetResourceDirectory(resourceDirectory);
        templateEngine.setGenerateNativeImageResources(true);

        IoUtils.deleteDirectoryContent(resourceDirectory);
    }

    @Test
    void generate_noTemplates() {
        templateEngine.generateAll();

        assertThat(getNativeImageProperties()).doesNotExist();
        assertThat(getNativeImageReflectionConfig()).doesNotExist();
    }

    @Test
    void generate_oneTemplate() {
        dummyCodeResolver.givenCode("hello.jte", "Hello World");

        templateEngine.generateAll();

        Path properties = getNativeImageProperties();
        assertThat(properties).exists();
        assertThat(properties).hasContent("Args = -H:ReflectionConfigurationResources=${.}/reflection-config.json -H:ResourceConfigurationResources=${.}/resource-config.json");

        Path config = getNativeImageReflectionConfig();
        assertThat(config).exists();
        assertThat(config).hasContent("[\n" +
                "{\n" +
                "  \"name\":\"gg.jte.generated.ondemand.JtehelloGenerated\",\n" +
                "  \"allDeclaredMethods\":true,\n" +
                "  \"allDeclaredFields\":true\n" +
                "}\n" +
                "]\n");
    }

    @Test
    void generate_twoTemplates() {
        dummyCodeResolver.givenCode("hello.jte", "Hello World");
        dummyCodeResolver.givenCode("bye.jte", "Bye World");

        templateEngine.generateAll();

        Path config = getNativeImageReflectionConfig();
        assertThat(config).exists();
        assertThat(config).hasContent("[\n" +
                "{\n" +
                "  \"name\":\"gg.jte.generated.ondemand.JtebyeGenerated\",\n" +
                "  \"allDeclaredMethods\":true,\n" +
                "  \"allDeclaredFields\":true\n" +
                "},\n" +
                "{\n" +
                "  \"name\":\"gg.jte.generated.ondemand.JtehelloGenerated\",\n" +
                "  \"allDeclaredMethods\":true,\n" +
                "  \"allDeclaredFields\":true\n" +
                "}\n" +
                "]\n");
    }

    @Test
    void precompile_oneTemplate() {
        dummyCodeResolver.givenCode("hello.jte", "Hello World");

        templateEngine.precompileAll();

        Path properties = getNativeImageProperties();
        assertThat(properties).exists();
        assertThat(properties).hasContent("Args = -H:ReflectionConfigurationResources=${.}/reflection-config.json -H:ResourceConfigurationResources=${.}/resource-config.json");

        Path config = getNativeImageReflectionConfig();
        assertThat(config).exists();
        assertThat(config).hasContent("[\n" +
                "{\n" +
                "  \"name\":\"gg.jte.generated.ondemand.JtehelloGenerated\",\n" +
                "  \"allDeclaredMethods\":true,\n" +
                "  \"allDeclaredFields\":true\n" +
                "}\n" +
                "]\n");
    }

    private Path getNativeImageProperties() {
        return getResourceOutputDirectory().resolve("native-image.properties");
    }

    private Path getNativeImageReflectionConfig() {
        return getResourceOutputDirectory().resolve("reflection-config.json");
    }

    private Path getResourceOutputDirectory() {
        return resourceDirectory.resolve("META-INF").resolve("native-image").resolve("jte-generated").resolve(Constants.PACKAGE_NAME_ON_DEMAND);
    }
}