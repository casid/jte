package gg.jte.extension;

import gg.jte.ContentType;

import java.nio.file.Path;

public interface JteConfig {
    Path generatedSourcesRoot();
    Path generatedResourcesRoot();

    String projectNamespace();

    String packageName();

    ContentType contentType();

    ClassLoader classLoader();
}
