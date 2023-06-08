# jte-models

jte-models is a generator extension for jte which creates a typesafe facade for rendering templates.

## Setup

To use jte-models, set up your build script to include one of these:

<details>
<summary>Maven</summary>

```xml
            <plugin>
                <groupId>gg.jte</groupId>
                <artifactId>jte-maven-plugin</artifactId>
                <version>${jte.version}</version>
                <configuration>
                    <sourceDirectory>${basedir}/src/main/jte</sourceDirectory>
                    <contentType>Html</contentType>
                    <extensions>
                        <extension>
                            <className>gg.jte.models.generator.ModelExtension</className>
                            <!-- optional settings to include annotations on generated classes:
                            <settings>
                                <interfaceAnnotation>@foo.bar.MyAnnotation</interfaceAnnotation>
                                <implementationAnnotation>@foo.bar.MyAnnotation</implementationAnnotation>
                            </settings>
                            -->
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
</details>

<details>
<summary>Gradle (Groovy)</summary>

```groovy
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
    // or to add annotations to generated classes:
    /*
    jteExtension('gg.jte.models.generator.ModelExtension') {
        interfaceAnnotation = '@foo.bar.MyAnnotation'
        implementationAnnotation = '@foo.bar.MyAnnotation'
    }
     */
}

```
</details>

<details>
<summary>Gradle (Kotlin)</summary>

```kotlin
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
    // or to add annotations to generated classes:
    /*
    jteExtension("gg.jte.models.generator.ModelExtension") {
        property("interfaceAnnotation", "@foo.bar.MyAnnotation")
        property("implementationAnnotation", "@foo.bar.MyAnnotation")
    }
     */
}

```
</details>

Run the build to generate classes.

## Output
Additional generated classes will include a facade interface named `gg.jte.generated.precompiled.Templates`, with implementations `...StaticTemplates` and `...DynamicTemplates`.

`Templates` has a method for each of your templates, for example:

```java
public interface Templates {
    JteModel helloWorld(String greeting);
}
```

(The package name can be changed by setting the packageName option in the build).

## Usage

First, construct an instance of `Templates`. If you are using a dependency injection framework, you could make a factory for this. Otherwise use `new StaticTemplates()` or `new DynamicTemplates(templateEngine)`.

To use, call a template method to get a `JteModel` object, then call one of its `render` methods. For example:

```java
OutputStream output = ...
Templates templates = new StaticTemplates();
templates.helloWorld("Hi!").render(output);
```

### Static vs. Dynamic

`StaticTemplates` is built so that it calls directly to jte generated render classes, with no reflection used. This is good for a production build.

`DynamicTemplates` delegates to a `TemplateEngine`, so it can be set up to hot-reload templates. This is good for development. You will still have to rerun the build if @params of a template are changed.