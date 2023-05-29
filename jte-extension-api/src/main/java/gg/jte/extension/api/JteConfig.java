package gg.jte.extension.api;

import gg.jte.ContentType;

import java.nio.file.Path;

/**
 * An instance of this type will be given to an extension, so that it can access configuration of the jte compiler.
 */
public interface JteConfig {
    Path generatedSourcesRoot();
    Path generatedResourcesRoot();

    String projectNamespace();

    String packageName();

    ContentType contentType();

    ClassLoader classLoader();
}
