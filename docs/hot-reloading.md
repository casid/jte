---
title: jte Hot Reloading
description: How to use jte hot reloading in development mode.
---

# Hot Reloading

## For a regular website

Hot reloading is supported out of the box when using the [`gg.jte.resolve.DirectoryCodeResolver`](https://www.javadoc.io/doc/gg.jte/jte/{{ latest-git-tag }}/gg.jte/gg/jte/resolve/DirectoryCodeResolver.html) (or the [`gt.jte.resolve.ResourceCodeResolver`](https://www.javadoc.io/doc/gg.jte/jte/{{ latest-git-tag }}/gg.jte/gg/jte/resolve/ResourceCodeResolver.html) with resources located outside of JAR files). Before a template is resolved, the modification timestamp of the template file and all of its dependencies is checked. If any modification is detected, the template is recompiled, and the old one is discarded to GC.

!!! warning

    It makes sense to do this in your local development environment only. When running in production, for maximum performance and security, [precompiled templates](pre-compiling.md) are recommended instead.

If you clone [jte repository](https://github.com/casid/jte), you can launch the [`gg.jte.benchmark.SimpleWebServer`](https://github.com/casid/jte/blob/{{ latest-git-tag }}/jte/src/test/java/gg/jte/benchmark/SimpleWebServer.java) example's main method. It will fire up a tiny webserver with one page to play with at <http://localhost:8080>.

## For a statically rendered website

If you're using jte to pre-render static websites as HTML files, you can also listen to template file changes during development and re-render affected static files. Add the `jte-watcher` module to your project:

=== "Maven"

    ```xml linenums="1"
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte-watcher</artifactId>
        <version>${jte.version}</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy linenums="1"
    implementation("gg.jte:jte-watcher:${jteVersion}")
    ```

The `gg.jte.watcher.DirectoryWatcher::start()` method starts a daemon thread listening to file changes within the jte template directory. Once file changes are detected, a listener is called with a list of changed templates.

=== "Java"

    ```java linenums="1"
    if (isDeveloperEnvironment()) {
        DirectoryWatcher watcher = new DirectoryWatcher(templateEngine, codeResolver);
        watcher.start(templates -> {
            for (String template : templates) {
                // Re-render the static HTML file
            }
        });
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    if (isDeveloperEnvironment()) {
        val watcher = DirectoryWatcher(templateEngine, codeResolver)
        watcher.start(templates -> templates.forEach { /* re-render the static HTML file */ });
    }
    ```

## Customizing generated class directory

By default, generated sources and classes are outputted into the subdirectory `jte-classes` under the current directory. It is possible to customize this directory when creating the template engine. But to have the hot reload feature working, **a custom class directory must not be on the classpath**. If it is on the classpath, generated classes will be visible to the default class loader, and once a generated class is loaded, it will not be possible to reload it after recompiling a template, thus making the hot reload effectively non-functional.
