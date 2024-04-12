---
title: Maven Plugin
description: How to use the jte Maven plugin.
---

jte provides a [Maven Plugin][jte-maven-compiler-plugin] that you can integrate in your build process to either [precompile templates](pre-compiling.md), or to generate the Java/Kotlin code for your templates.

!!! tip "Versions alignment"

    Make sure the jte maven plugin version matches the jte dependency version. You can create a `${jte.version}` property to sync the versions easily.

The plugin provides two goals:

## `precompile` goal { #precompile-goal }

See [Precompiling Templates](pre-compiling.md) for more information. To use and configure this goal, you would use:

```xml linenums="1"
<plugin>
    <groupId>gg.jte</groupId>
    <artifactId>jte-maven-plugin</artifactId>
    <version>${jte.version}</version>
    <configuration>
        <sourceDirectory>${project.basedir}/src/main/jte</sourceDirectory>
        <targetDirectory>${project.build.directory}/jte-classes</targetDirectory>
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

### Configurations { #precompile-configurations }

The default phase for this goal is `process-classes`, and it accepts the following configuration options:

| Parameter                  | Description                                                                       | Default                                                        |
|----------------------------|-----------------------------------------------------------------------------------|----------------------------------------------------------------|
| `sourceDirectory`          | The directory where template files are located                                    | None. Required configuration.                                  |
| `targetDirectory`          | The directory where compiled classes should be written to                         | None. Required configuration.                                  |
| `compilePath`              | The compile-classpath to use                                                      | `project.compileClasspathElements`                             |
| `contentType`              | The content type of all templates. Either `Plain` or `Html`                       | None. Required configuration                                   |
| `trimControlStructures`    | Trims control structures, resulting in prettier output                            | `false`                                                        |
| `htmlTags`                 | Intercepts the given html tags during template compilation                        | None                                                           |
| `htmlPolicyClass`          | An [`HtmlPolicy`][html-policy] used to check the parsed HTML at compile time      | None                                                           |
| `htmlCommentsPreserved`    | If HTML comments should be preserved in the output                                | `false`                                                        |
| `binaryStaticContent`      | If to generate a [binary content](binary-rendering.md) resource for each template | `false`                                                        |
| `compileArgs`              | Sets additional compiler arguments for `jte` templates.                           | See note below                                                 |
| `kotlinCompileArgs`        | Sets additional compiler arguments for `kte` templates                            | None                                                           |
| `packageName`              | The package name, where template classes are generated to                         | [`Constants.PACKAGE_NAME_PRECOMPILED`][constants-package-name] |
| `keepGeneratedSourceFiles` | If it should keep all generated jte source files after compilation                | `false`                                                        |

!!! info "About `htmlPolicyClass`"

    The `htmlPolicyClass` will default to `gg.jte.html.OwaspHtmlPolicy` if the content type is `Html`.

!!! info "About `compileArgs`"

    This option default depends on the `maven.compiler.release` or, if it is absent, on `maven.compiler.source` and `maven.compiler.target` properties.

    So, if the following property is configured in your `pom.xml`:

    ```xml linenums="1"
    <properties>
        <maven.compiler.release>21</maven.compiler.release>
    </properties>
    ```
    
    The `compileArgs` will default to `--release 21`. If `maven.compiler.release` is not configured, it will try to use `maven.compiler.source` and `maven.compiler.target` instead:

    ```xml linenums="1"
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
    ```

    This will result in `--source 21 --target 21` as the default value for `compileArgs`. If none of those properties are configured, then the default value will be empty. Release is usually preferred over source and target since it will also ensure the correct API versions are used. See more details in the links below:

    - [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html#parameter-details)
    - [`-release` option](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html#option-release)
    - [`-source`](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html#option-source) and [`-target`](https://docs.oracle.com/en/java/javase/21/docs/specs/man/javac.html#option-target) options

!!! warning "Clean all before precompiling"

    The `precompile` goal cleans the directory containing the compiled template classes every time it runs. In your local development environment, it may make more sense to use [hot reloading](hot-reloading.md).

## `generate` goal { #generate-goal }

This goal generates all template classes in a source directory. This only generates `.java` files, but does not compile them to `.class` files.

```xml linenums="1"
<plugin>
    <groupId>gg.jte</groupId>
    <artifactId>jte-maven-plugin</artifactId>
    <version>${jte.version}</version>
    <configuration>
        <sourceDirectory>${project.basedir}/src/main/jte</sourceDirectory>
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

### Configurations { #generate-configurations }

The default phase for this goal is `generate-sources`, and it accepts the following configuration options:

| Parameter                 | Description                                                                       | Default                                                        |
|---------------------------|-----------------------------------------------------------------------------------|----------------------------------------------------------------|
| `sourceDirectory`         | The directory where template files are located                                    | None. Required configuration.                                  |
| `targetDirectory`         | Destination directory to store generated templates.                               | `${project.build.directory}/generated-sources/jte`             |
| `contentType`             | The content type of all templates. Either `Plain` or `Html`                       | None. Required configuration                                   |
| `trimControlStructures`   | Trims control structures, resulting in prettier output                            | `false`                                                        |
| `htmlTags`                | Intercepts the given html tags during template compilation                        | None                                                           |
| `htmlCommentsPreserved`   | If HTML comments should be preserved in the output                                | `false`                                                        |
| `binaryStaticContent`     | If to generate a [binary content](binary-rendering.md) resource for each template | `false`                                                        |
| `packageName`             | The package name, where template classes are generated to                         | [`Constants.PACKAGE_NAME_PRECOMPILED`][constants-package-name] |
| `targetResourceDirectory` | Directory in which to generate non-java files (resources)                         | None                                                           |
| `extensions`              | Extensions this template engine should load                                       | None                                                           |

### Extensions { #generate-extensions }

Currently, the following extensions exist:

#### `gg.jte.models.generator.ModelExtension` { #model-extension }

See details about it in the [jte-models documentation](jte-models.md).

##### Parameters { #model-extension-parameters }

The following parameters are available for this extension:

| Parameter                  | Description                                                               | Default |
|----------------------------|---------------------------------------------------------------------------|---------|
| `interfaceAnnotation`      | The FQCN of the annotation to add to the generated interface              | None    |
| `implementationAnnotation` | The FQCN of the annotation to add to the generated implementation classes | None    |
| `language`                 | The target language for the generated classes. Either `Java` or `Kotlin`  | `Java`  |
| `includePattern`           | A regular expression to only include certain templates                    | None    |
| `excludePattern`           | A regular expression to exclude certain templates                         | None    |

##### Example { #model-extension-example }

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
                <settings>
                    <interfaceAnnotation>@foo.bar.MyAnnotation</interfaceAnnotation>
                    <implementationAnnotation>@foo.bar.MyAnnotation</implementationAnnotation>
                    <language>Java</language>
                    <includePattern>\.pages\..*</includePattern>
                    <excludePattern>\.components\..*</excludePattern>
                </settings>
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
</plugin>
```

#### `gg.jte.nativeimage.NativeResourcesExtension` { #native-resources-extension }

!!! info "Version note"

    Available since jte ^^**1.10.0**^^.

An application jar with generated classes can be built into a native binary using [GraalVM native-image](https://www.graalvm.org/reference-manual/native-image/). To support this, this extension generates the necessary configuration files to tell `native-image` about classes loaded by reflection.

##### Example { #native-resources-extension-example }

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
                <className>gg.jte.nativeimage.NativeResourcesExtension</className>
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
</plugin>
```

[jte-maven-compiler-plugin]: https://search.maven.org/artifact/gg.jte/jte-maven-plugin
[html-policy]: https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg.jte.runtime/gg/jte/html/HtmlPolicy.html
[constants-package-name]: https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg/jte.runtime/gg/jte/Constants.html#PACKAGE_NAME_PRECOMPILED