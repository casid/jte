package org.jusecase.jte;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.benchmark.WelcomePage;
import org.jusecase.jte.output.StringOutput;
import org.jusecase.jte.resolve.DirectoryCodeResolver;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PreCompileTemplatesTest {
    @Test
    void precompileAll() {
        TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/test/resources/benchmark")), Path.of("jte-classes"), ContentType.Plain);
        templateEngine.cleanAll();
        templateEngine.precompileAll();

        StringOutput output = new StringOutput();
        templateEngine.render("welcome.jte", new WelcomePage(12), output);
        assertThat(output.toString()).contains("This page has 12 visits already.");
    }

    @Test
    void precompileAll_externalClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader externalClassLoader = new URLClassLoader(new URL[] {contextClassLoader.getResource("external/external.jar")});
            Thread.currentThread().setContextClassLoader(externalClassLoader);

            TemplateEngine templateEngine = TemplateEngine.create(new DirectoryCodeResolver(Path.of("src/test/resources/external")), Path.of("jte-classes"), ContentType.Plain);
            templateEngine.cleanAll();
            templateEngine.precompileAll(getCompilePath("src/test/resources/external/external.jar"));

            assertThat(Files.exists(Path.of("jte-classes", "org", "jusecase", "jte", "generated", "JteexternalGenerated.java"))).isTrue();
            assertThat(Files.exists(Path.of("jte-classes", "org", "jusecase", "jte", "generated", "JteexternalGenerated.class"))).isTrue();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private List<String> getCompilePath(String ... additionalPaths) {
        List<String> result = new ArrayList<>(Arrays.asList(additionalPaths));

        String classPath = System.getProperty("java.class.path");
        if (classPath != null) {
            result.addAll(Arrays.asList(classPath.split(File.pathSeparator)));
        }

        return result;
    }
}
