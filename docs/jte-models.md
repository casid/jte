---
title: jte-models facade generator
description: A generator extension for jte which creates a typesafe facade for rendering templates.
---

# jte-models Facade Generator

jte-models is a generator extension for jte that creates a typesafe facade for rendering templates.

## Setup

To use jte-models, set up your build script to include one of these:

=== "Maven"

    ```xml linenums="1"
    <plugin>
        <groupId>gg.jte</groupId>
        <artifactId>jte-maven-plugin</artifactId>
        <version>${jte.version}</version>
        <configuration>
            <sourceDirectory>${project.basedir}/src/main/jte</sourceDirectory>
            <contentType>Html</contentType>
            <extensions>
                <extension>
                    <className>gg.jte.models.generator.ModelExtension</className>
                </extension>
            </extensions>
        </configuration>
        <executions>
            <execution>
                <phase>generate-sources</phase>
                <goals>
                    <goal>generate</goal>
                </goals>
            </execution>
        </executions>
        <dependencies>
            <dependency>
                <groupId>gg.jte</groupId>
                <artifactId>jte-models</artifactId>
                <version>${jte.version}</version>
            </dependency>
        </dependencies>
    </plugin>
    ```

=== "Gradle (Groovy DSL)"

    ```groovy linenums="1"
    plugins {
        id 'gg.jte.gradle' version '${jte.version}'
    }
    
    dependencies {
        implementation 'gg.jte:jte-runtime:${jte.version}'
        jteGenerate 'gg.jte:jte-models:${jte.version}'
    }
    
    jte {
        generate()
        binaryStaticContent = true
        jteExtension 'gg.jte.models.generator.ModelExtension'
    }
    ```

=== "Gradle (Kotlin DSL)"

    ```kotlin linenums="1"
    plugins {
        id("gg.jte.gradle") version "${jte.version}"
    }
    
    dependencies {
        implementation("gg.jte:jte-runtime:${jte.version}")
        jteGenerate("gg.jte:jte-models:${jte.version}")
    }
    
    jte {
        generate()
        binaryStaticContent.set(true)
        jteExtension("gg.jte.models.generator.ModelExtension")
    }
    ```

Run the build to generate classes.

!!! info "Full configuration"

    See details about how to fully configure the extension:

    - [Maven plugin documentation](maven-plugin.md#model-extension)
    - [Gradle plugin documentation](gradle-plugin.md#model-extension)

## Output

Additional generated classes will include a facade interface named `gg.jte.generated.precompiled.Templates`, with implementations:

- `gg.jte.generated.precompiled.StaticTemplates`
- `gg.jte.generated.precompiled.DynamicTemplates`

`Templates` has a method for each of your templates, for example:

=== "Java"

    ```java linenums="1"
    public interface Templates {
        JteModel helloWorld(String greeting);
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    interface Templates {
        fun helloWord(greeting: String): JteModel
    }
    ```

!!! tip

    The package name can be changed by setting the packageName option in the build.

## Usage

First, construct an instance of `Templates`. If you are using a dependency injection framework, you could make a factory for this. Otherwise, use `new StaticTemplates()` or `new DynamicTemplates(templateEngine)`.

To use, call a template method to get a `JteModel` object, then call one of its `render` methods. For example:

```java linenums="1"
OutputStream output = ...
Templates templates = new StaticTemplates();
templates.helloWorld("Hi!").render(output);
```

### Static vs. Dynamic

`StaticTemplates` is built so that it calls directly to jte generated render classes, with no reflection used. **This is good for a production build**. It builds on the outputs of [precompiled templates](pre-compiling.md).

`DynamicTemplates` delegates to a `TemplateEngine`, so it can be set up to hot-reload templates. **This is good for development**. You will still have to rerun the build if @params of a template are changed.