---
title: jte-nullmarked NullMarked annotation generator
description: A generator extension for jte that adds @NullMarked package annotations to generated classes.
---

# jte-nullmarked NullMarked Annotation Generator

jte-nullmarked is a generator extension for jte that creates `package-info.java` files annotated
with `@NullMarked` for each package containing generated classes. This enables null-safety tooling
(such as NullAway or ErrorProne) to treat generated classes as null-safe, preventing build failures
in projects that enforce null-safety annotations.

## Setup

Add `jspecify` 1.0.0 or later to your project dependencies, then configure the build plugin to use the extension.

=== "Maven"

    Add to your `<dependencies>` section:

    ```xml linenums="1"
    <dependency>
        <groupId>org.jspecify</groupId>
        <artifactId>jspecify</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```

    Add to your `<build><plugins>` section:

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
                    <className>gg.jte.nullmarked.NullMarkedExtension</className>
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
                <artifactId>jte-nullmarked</artifactId>
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
        implementation 'org.jspecify:jspecify:1.0.0'
        jteGenerate 'gg.jte:jte-nullmarked:${jte.version}'
    }

    jte {
        generate()
        jteExtension 'gg.jte.nullmarked.NullMarkedExtension'
    }
    ```

=== "Gradle (Kotlin DSL)"

    ```kotlin linenums="1"
    plugins {
        id("gg.jte.gradle") version "${jte.version}"
    }

    dependencies {
        implementation("gg.jte:jte-runtime:${jte.version}")
        implementation("org.jspecify:jspecify:1.0.0")
        jteGenerate("gg.jte:jte-nullmarked:${jte.version}")
    }

    jte {
        generate()
        jteExtension("gg.jte.nullmarked.NullMarkedExtension")
    }
    ```

Run the build to generate classes.

## Output

For each package containing generated classes, a `package-info.java` file is created:

```java
@NullMarked
package gg.jte.generated.precompiled;

import org.jspecify.annotations.NullMarked;
```

If a `package-info.java` already exists in a package directory, it is left unchanged.
