---
title: Command Line Interface
description: How to use the jte command line tool to generate templates without Maven or Gradle.
---

If your project doesn't use Maven or Gradle (for example, a Clojure or Kotlin script project built with a different tool), you can still generate Java sources from your jte templates using the standalone `jte-cli` executable jar.

!!! info "Generate only"

    `jte-cli` only generates `.java` source files from your templates (equivalent to the Maven plugin's [`generate` goal](maven-plugin.md#generate-goal)). It does not compile them to `.class` files - your own build must compile the generated sources, the same way it compiles the rest of your project's Java code.

!!! info "Java templates only"

    `jte-cli` currently supports `.jte` (Java) templates only. `.kte` (Kotlin) templates aren't supported yet, since bundling the Kotlin compiler would make the jar much larger.

## Download

Download the executable jar directly from [Maven Central][jte-cli-central]:

```shell linenums="1"
curl -O https://repo1.maven.org/maven2/gg/jte/jte-cli/{{ latest-git-tag }}/jte-cli-{{ latest-git-tag }}-executable.jar
```

No Maven installation is required to fetch it - any tool that can download a file over HTTPS will do (`wget`, your browser, etc.). If your project happens to use Maven or Gradle for something else, you can alternatively declare it as a regular dependency with classifier `executable` (`gg.jte:jte-cli:{{ latest-git-tag }}:jar:executable`) and copy it out with the respective dependency plugin.

## Usage

```shell linenums="1"
java -jar jte-cli-{{ latest-git-tag }}-executable.jar \
    --source-directory src/main/jte \
    --target-directory target/generated-sources/jte \
    --content-type Html
```

Run with `--help` to see all options.

### Options { #options }

| Option                        | Description                                                                       | Default                                                        |
|--------------------------------|------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `-s, --source-directory`        | The directory where template files are located                                    | None. Required.                                                 |
| `-t, --target-directory`        | Destination directory to store generated sources                                  | None. Required.                                                 |
| `-c, --content-type`            | The content type of all templates. Either `Plain` or `Html`                       | None. Required.                                                 |
| `--trim-control-structures`     | Trims control structures, resulting in prettier output                            | `false`                                                         |
| `--html-tags`                   | Intercepts the given (comma-separated) html tags during template compilation      | None                                                            |
| `--html-comments-preserved`     | If HTML comments should be preserved in the output                                | `false`                                                         |
| `--binary-static-content`       | If to generate a [binary content](binary-rendering.md) resource for each template | `false`                                                         |
| `--package-name`                | The package name, where template classes are generated to                         | [`Constants.PACKAGE_NAME_PRECOMPILED`][constants-package-name]  |
| `--target-resource-directory`   | Directory in which to generate non-java files (resources)                         | None                                                            |

!!! info "About `--binary-static-content` without `--target-resource-directory`"

    When `--binary-static-content` is enabled, each template with static content gets a `.bin` resource file alongside its generated `.java` file. If `--target-resource-directory` isn't set, these `.bin` files are written into `--target-directory` itself, next to the generated sources. Either way, make sure your build copies them onto the runtime classpath - [`Utf8ByteOutput`](binary-rendering.md) loads them at render time.

!!! info "No extension support yet"

    Unlike the Maven/Gradle plugins, `jte-cli` doesn't support [extensions](maven-plugin.md#generate-extensions) (such as `ModelExtension`) yet. This may be added in a future version.

[jte-cli-central]: https://central.sonatype.com/artifact/gg.jte/jte-cli
[constants-package-name]: https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg/jte.runtime/gg/jte/Constants.html#PACKAGE_NAME_PRECOMPILED
