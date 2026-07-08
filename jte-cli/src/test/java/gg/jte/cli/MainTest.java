package gg.jte.cli;

import gg.jte.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    Path sourceDirectory;
    Path targetDirectory;
    Main main;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        sourceDirectory = tempDir.resolve("templates");
        targetDirectory = tempDir.resolve("generated");
        Files.createDirectories(sourceDirectory);

        main = new Main();
        main.sourceDirectory = sourceDirectory;
        main.targetDirectory = targetDirectory;
    }

    @Test
    void generatesJavaSourceFromTemplate() throws IOException {
        Files.writeString(sourceDirectory.resolve("hello.jte"), "@param String name\nHello, ${name}!");
        main.contentType = ContentType.Plain;

        int amount = main.run();

        assertThat(amount).isEqualTo(1);
        Path generated = targetDirectory.resolve("gg/jte/generated/precompiled/JtehelloGenerated.java");
        assertThat(generated).exists();
        assertThat(Files.readString(generated)).contains("Hello, ");
    }

    @Test
    void usesCustomPackageName() throws IOException {
        Files.writeString(sourceDirectory.resolve("hello.jte"), "Hello!");
        main.contentType = ContentType.Plain;
        main.packageName = "com.example.templates";

        main.run();

        Path generated = targetDirectory.resolve("com/example/templates/JtehelloGenerated.java");
        assertThat(generated).exists();
        assertThat(Files.readString(generated)).contains("package com.example.templates;");
    }

    @Test
    void trimsControlStructuresWhenEnabled() throws IOException {
        Files.writeString(sourceDirectory.resolve("hello.jte"), "before\n@if(true)\nyes\n@endif\nafter");
        main.contentType = ContentType.Plain;

        main.run();
        Path generated = targetDirectory.resolve("gg/jte/generated/precompiled/JtehelloGenerated.java");
        String untrimmed = Files.readString(generated);

        main.trimControlStructures = true;
        main.run();
        String trimmed = Files.readString(generated);

        assertThat(trimmed).isNotEqualTo(untrimmed);
    }

    @Test
    void interceptsConfiguredHtmlTags() throws IOException {
        Files.writeString(sourceDirectory.resolve("hello.jte"), "<form action=\"a\">\n</form>");
        main.contentType = ContentType.Html;
        main.htmlTags = new String[]{"form"};

        main.run();

        Path generated = targetDirectory.resolve("gg/jte/generated/precompiled/JtehelloGenerated.java");
        assertThat(Files.readString(generated)).contains("jteHtmlInterceptor.onHtmlTagOpened(\"form\"");
    }

    @Test
    void preservesHtmlCommentsWhenEnabled() throws IOException {
        Files.writeString(sourceDirectory.resolve("hello.jte"), "<!--a comment-->");
        main.contentType = ContentType.Html;
        main.htmlCommentsPreserved = true;

        main.run();

        Path generated = targetDirectory.resolve("gg/jte/generated/precompiled/JtehelloGenerated.java");
        assertThat(Files.readString(generated)).contains("a comment");
    }

    @Test
    void generatesBinaryStaticContentToTargetResourceDirectory() throws IOException {
        Path resourceDirectory = sourceDirectory.getParent().resolve("resources");
        Files.writeString(sourceDirectory.resolve("hello.jte"), "Hello!");
        main.contentType = ContentType.Plain;
        main.binaryStaticContent = true;
        main.targetResourceDirectory = resourceDirectory;

        main.run();

        Path resourceFile = resourceDirectory.resolve("gg/jte/generated/precompiled/JtehelloGenerated.bin");
        assertThat(resourceFile).exists();
    }
}
