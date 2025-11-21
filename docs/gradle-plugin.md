---
title: Gradle Plugin
description: How to use the jte Gradle plugin.
---

jte provides a [Gradle Plugin][jte-gradle-plugin] that you can integrate in your build process to either [precompile templates](pre-compiling.md), or to generate the Java/Kotlin code for your templates.


!!! tip "Versions alignment"

    Make sure the jte gradle plugin version matches the jte dependency version. You can create a `jteVersion` property in `gradle.properties` to sync the versions easily.

The plugin provides two tasks:

## `precompileJte` task { #precompile-task }

See [Precompiling Templates](pre-compiling.md) for more information. To use and configure this task, you would use:

=== "Groovy"

    ```groovy linenums="1"
    plugins {
        id 'java'
        id 'gg.jte.gradle' version '${jteVersion}'
    }

    dependencies {
        implementation('gg.jte:jte:${jteVersion}')
    }

    jte {
        precompile()
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    plugins {
        java
        id("gg.jte.gradle") version("${jteVersion}")
    }

    dependencies {
        implementation("gg.jte:jte:${jteVersion}")
    }

    jte {
        precompile()
    }
    ```

### Task inputs { #precompile-inputs }

The follow inputs will be picked by the `precompileJte` task:

| Parameter               | Description                                                                       | Default                                                        |
|-------------------------|-----------------------------------------------------------------------------------|----------------------------------------------------------------|
| `sourceDirectory`       | The directory where template files are located                                    | `src/main/jte`                                                 |
| `targetDirectory`       | The directory where compiled classes should be written to                         | `jte-classes`                                                  |
| `compilePath`           | The compile-classpath to use                                                      | `sourceSets.main.runtimeClasspath`                             |
| `contentType`           | The content type of all templates. Either `Plain` or `Html`                       | `Html`                                                         |
| `trimControlStructures` | Trims control structures, resulting in prettier output                            | `false`                                                        |
| `htmlTags`              | Intercepts the given html tags during template compilation                        | None                                                           |
| `htmlPolicyClass`       | An [`HtmlPolicy`][html-policy] used to check the parsed HTML at compile time      | None                                                           |
| `htmlCommentsPreserved` | If HTML comments should be preserved in the output                                | `false`                                                        |
| `binaryStaticContent`   | If to generate a [binary content](binary-rendering.md) resource for each template | `false`                                                        |
| `compileArgs`           | Sets additional compiler arguments for `jte` templates.                           | None                                                           |
| `kotlinCompileArgs`     | Sets additional compiler arguments for `kte` templates                            | None                                                           |
| `packageName`           | The package name, where template classes are generated to                         | [`Constants.PACKAGE_NAME_PRECOMPILED`][constants-package-name] |

!!! info "About `htmlPolicyClass`"

    The `htmlPolicyClass` will default to `gg.jte.html.OwaspHtmlPolicy` if the content type is `Html`.

!!! warning "Clean all before precompiling"

    The `precompileJte` task cleans the directory containing the compiled template classes every time it runs. In your local development environment, it may make more sense to use [hot reloading](hot-reloading.md).

## `generateJte` task { #generate-task }

This task generates all template classes in a sources directory. This only generates `.java`/`.kt` files, but does not compile them to `.class` files.

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

### Task Inputs { #generate-inputs }

The follow inputs will be picked by the `generateJte` task:

| Parameter                 | Description                                                                       | Default                                                        |
|---------------------------|-----------------------------------------------------------------------------------|----------------------------------------------------------------|
| `sourceDirectory`         | The directory where template files are located                                    | `src/main/jte`                                                 |
| `targetDirectory`         | Destination directory to store generated templates.                               | `${project.build.directory}/generated-sources/jte`             |
| `contentType`             | The content type of all templates. Either `Plain` or `Html`                       | `Html`                                                         |
| `trimControlStructures`   | Trims control structures, resulting in prettier output                            | `false`                                                        |
| `htmlTags`                | Intercepts the given html tags during template compilation                        | None                                                           |
| `htmlCommentsPreserved`   | If HTML comments should be preserved in the output                                | `false`                                                        |
| `binaryStaticContent`     | If to generate a [binary content](binary-rendering.md) resource for each template | `false`                                                        |
| `packageName`             | The package name, where template classes are generated to                         | [`Constants.PACKAGE_NAME_PRECOMPILED`][constants-package-name] |
| `targetResourceDirectory` | Directory in which to generate non-java files (resources)                         | None                                                           |

### Extensions { #generate-extensions }

Currently, the following extensions exist:

#### `gg.jte.models.generator.ModelExtension` { #model-extension }

See details about it in the [jte-models documentation](jte-models.md).

##### Properties { #model-extension-properties }

The following parameters are available for this extension:

| Parameter                  | Description                                                               | Default |
|----------------------------|---------------------------------------------------------------------------|---------|
| `interfaceAnnotation`      | The FQCN of the annotation to add to the generated interface              | None    |
| `implementationAnnotation` | The FQCN of the annotation to add to the generated implementation classes | None    |
| `language`                 | The target language for the generated classes. Either `Java` or `Kotlin`  | `Java`  |
| `includePattern`           | A regular expression to only include certain templates                    | None    |
| `excludePattern`           | A regular expression to exclude certain templates                         | None    |

##### Example { #model-extension-example }

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

        // Remove the properties that don't make sense to your project
        jteExtension('gg.jte.models.generator.ModelExtension') {
            // Target language ("Java" and "Kotlin" are supported). "Java" is the default.
            language = 'Java'
    
            // Annotations to add to generated interfaces and classes
            interfaceAnnotation = '@foo.bar.MyAnnotation'
            implementationAnnotation = '@foo.bar.MyAnnotation'
    
            // Patterns to include (or exclude) certain templates
            includePattern = '\.pages\..*'
            excludePattern = '\.components\..*'
        }
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

        // Remove the properties that don't make sense to your project
        jteExtension("gg.jte.models.generator.ModelExtension") {
            // Target language ("Java" and "Kotlin" are supported). "Java" is the default.
            property("language", "Java")
    
            // Annotations to add to generated interfaces and classes
            property("interfaceAnnotation", "@foo.bar.MyAnnotation")
            property("implementationAnnotation", "@foo.bar.MyAnnotation")
    
            // Patterns to include (or exclude) certain templates
            property("includePattern", "\.pages\..*")
            property("excludePattern", '\.components\..*")
        }
    }
    ```

#### `gg.jte.nativeimage.NativeResourcesExtension` { #native-resources-extension }

!!! info "Version note"

    Available since jte ^^**1.10.0**^^.

An application jar with generated classes can be built into a native binary using [GraalVM native-image](https://www.graalvm.org/reference-manual/native-image/). To support this, this extension generates the necessary configuration files to tell `native-image` about classes loaded by reflection.

##### Example { #native-resources-extension-example }

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
        jteExtension('gg.jte.nativeimage.NativeResourcesExtension')
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
        jteExtension("gg.jte.nativeimage.NativeResourcesExtension")
    }
    ```

!!! tip "Sample project"

    There's an example [Gradle test project](https://github.com/casid/jte/blob/{{ latest-git-tag }}/test/jte-runtime-cp-test-gradle-convention/build.gradle) using `native-image` compilation.

!!! warning
    
    An older task-based setup is supported for backward compatibility. 
    This may be removed in a future version of JTE.
    
    Old:

    ```groovy
    tasks.generateJte { // or precompileJte
        sourceDirectory = Paths.get(project.projectDir.absolutePath, "src", "main", "jte")
        contentType = ContentType.Html
    }
    ```

    New:

    ```groovy
    jte {
        generate() // or precompile()
        // source directory and contentType follow conventions
    }
    ```

## Advanced - additional tasks

In cases where a project has different JTE templates that need different settings, additional tasks can be registered.
An example:

```groovy
import gg.jte.ContentType
import gg.jte.gradle.GenerateJteTask

plugins {
    id 'java'
    id 'gg.jte.gradle' version '${jte.version}'
}

dependencies {
    implementation('gg.jte:jte:${jte.version}')
}

def additionalGenerateTask = tasks.register("additionalGenerateTask", GenerateJteTask) {
    contentType = ContentType.Plain
    sourceDirectory = file("src/main/otherJte").toPath()
    targetDirectory = file("build/additionalGenerateTask").toPath()
    packageName = "gg.jte.additionalgeneratetask"
}

tasks.named("compileJava") {
    dependsOn(additionalGenerateTask)
}

sourceSets.named("main") {
    java.srcDir(additionalGenerateTask.map {it.targetDirectory})
}
```

[jte-gradle-plugin]: https://plugins.gradle.org/plugin/gg.jte.gradle
[html-policy]: https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg.jte.runtime/gg/jte/html/HtmlPolicy.html
[constants-package-name]: https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg/jte.runtime/gg/jte/Constants.html#PACKAGE_NAME_PRECOMPILED