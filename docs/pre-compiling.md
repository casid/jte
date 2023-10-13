---
title: jte precompiling templates
description: How to speed up startup and rendering in production when using jte.
---

# Precompiling Templates

To speed up startup and rendering on your production server, it is possible to precompile all templates during the build. This way, the template engine can load each template's `.class` file directly without first compiling it. For security reasons, you may not want to run a JDK on production - with precompiled templates, this is unnecessary. The recommended way to set up jte is to instantiate the engine differently, depending on when you are developing or running on a server.

```java linenums="1"
if (isDeveloperMachine()) {
    // create template engine with hot reloading (a bit slower)
} else {
    // create template engine with precompiled templates (fastest)
}
```

To do this, you must create a `gg.jte.TemplateEngine` with the `createPrecompiled` factory method and specify where compiled template classes are located. Currently, there are two options available to do this.

## Using a directory on your server (recommended)

You must deploy the precompiled templates to your server when using this method.

```java linenums="1"
Path targetDirectory = Path.of("jte-classes"); // This is the directory where compiled templates are located.
TemplateEngine templateEngine = TemplateEngine.createPrecompiled(targetDirectory, ContentType.Html);
```

### Maven

You can use a [Maven plugin][jte-maven-compiler-plugin] to precompile all templates during the Maven build. You would need to put this in build / plugins of your projects' `pom.xml`. Please note that paths specified in Java must match those specified in Maven.

!!! warning

    Make sure the jte maven plugin version matches the jte dependency version. You can create a `${jte.version}` to sync the versions easily.

```xml linenums="1"
<plugin>
    <groupId>gg.jte</groupId>
    <artifactId>jte-maven-plugin</artifactId>
    <version>${jte.version}</version>
    <configuration>
        <!-- This is the directory where your .jte files are located. -->
        <sourceDirectory>src/main/jte</sourceDirectory>
        <!-- This is the directory where compiled templates are located. -->
        <targetDirectory>jte-classes</targetDirectory>
        <contentType>Html</contentType>
    </configuration>
    <executions>
        <execution>
            <phase>process-classes</phase>
            <goals>
                <goal>precompile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Gradle

{{ since("1.6.0") }}

The [Gradle plugin][jte-gradle-plugin] can precompile all templates during the Gradle build. Please note that paths specified in Java must match those specified in Gradle.

!!! warning

    Make sure the jte gradle plugin version matches the jte dependency version. You can create a `jteVersion` in `gradle.properties` to sync the versions easily.

=== "Grovy"

    ```groovy linenums="1"
    plugins {
        id 'java'
        id 'gg.jte.gradle' version '${jte.version}'
    }

    dependencies {
        implementation('gg.jte:jte:${jte.version}')
    }

    jte {
        precompile()
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    plugins {
        java
        id("gg.jte.gradle") version("${jte.version}")
    }

    dependencies {
        implementation("gg.jte:jte:${jte.version}")
    }

    jte {
        precompile()
    }
    ```

In case you would like to build a self-contained JAR, you can add this to your `build.gradle`:

=== "Groovy"

    ```groovy linenums="1"
    jar {
        dependsOn precompileJte
        from fileTree("jte-classes") {
            include "**/*.class"
            include "**/*.bin" // Only required if you use binary templates
        }
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    tasks.jar {
        dependsOn(tasks.precompileJte)
        from(fileTree("jte-classes") {
            include("**/*.class")
            include("**/*.bin") // Only required if you use binary templates
        })
    }
    ```

And init the template engine like this for production builds:

```java linenums="1"
TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
```

This way, the templates are loaded from the application class loader. See [this issue](https://github.com/casid/jte/issues/62) for additional information.

### Using the application class loader

{{ since("1.2.0") }}

When using this method, the precompiled templates are bundled within your application jar file. The plugin generates `*.java` files for all jte templates during Maven's `GENERATE_SOURCES` phase. Compilation of the templates is left to the Maven Compiler plugin.

While this provides you with a nice self-containing jar, it has some limitations:

- Some plugin settings are not supported, like configuring a custom HtmlPolicy class (this is because project classes are not yet compiled at the `GENERATE_SOURCES` phase).

```java linenums="1"
TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
```

### Maven

You can use a [Maven plugin][jte-maven-compiler-plugin] to generate all templates during the Maven build. You would need to put this in build / plugins of your projects' `pom.xml`. Please note that paths specified in Java must match those specified in Maven.

!!! warning

    Make sure the jte maven plugin version matches the jte dependency version. You can create a `${jte.version}` to sync the versions easily.

```xml linenums="1"
<plugin>
    <groupId>gg.jte</groupId>
    <artifactId>jte-maven-plugin</artifactId>
    <version>${jte.version}</version>
    <configuration>
        <sourceDirectory>${basedir}/src/main/jte</sourceDirectory> <!-- This is the directory where your .jte files are located. -->
        <contentType>Html</contentType>
    </configuration>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Gradle

{{ since("1.6.0") }}

The [Gradle plugin](https://plugins.gradle.org/plugin/gg.jte.gradle) can generate all templates during the Gradle build. Please note that paths specified in Java must match those specified in Gradle.

!!! warning

    Make sure the jte gradle plugin version matches the jte dependency version. You can create a `jteVersion` in `gradle.properties` to sync the versions easily.

=== "Groovy"

    ```groovy linenums="1"
    plugins {
        id 'java'
        id 'gg.jte.gradle' version '${jte.version}'
    }

    dependencies {
        implementation('gg.jte:jte:${jte.version}')
    }

    jte {
        generate()
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    plugins {
        java
        id("gg.jte.gradle") version("${jte.version}")
    }

    dependencies {
        implementation("gg.jte:jte:${jte.version}")
    }

    jte {
        generate()
    }
    ```
  
### GraalVM native-image support

{{ since("1.10.0") }}

An application jar with generated classes can be built into a native binary using [GraalVM native-image](https://www.graalvm.org/reference-manual/native-image/). To support this, jte can generate the necessary configuration files to tell `native-image` about classes loaded by reflection.

To use this feature, set `#!groovy jteExtension("gg.jte.nativeimage.NativeResourcesExtension")` in your Gradle `jte` block. (Docs for Maven TBD)

There's an example [Gradle test project](https://github.com/casid/jte/blob/main/test/jte-runtime-cp-test-gradle-convention/build.gradle) using `native-image` compilation.

[jte-maven-compiler-plugin]: https://github.com/casid/jte-maven-compiler-plugin
[jte-gradle-plugin]: https://plugins.gradle.org/plugin/gg.jte.gradle
