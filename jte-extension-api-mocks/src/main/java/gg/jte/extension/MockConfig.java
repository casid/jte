package gg.jte.extension;

import gg.jte.ContentType;

import java.nio.file.Path;

/**
 * Mock implementation to help with testing extensions.
 */
public class MockConfig implements JteConfig {
    Path generatedSourcesRoot;
    Path generatedResourcesRoot;
    String projectNamespace;
    String packageName;
    ContentType contentType;

    public static MockConfig mockConfig() {
        return new MockConfig();
    }

    public MockConfig generatedSourcesRoot(Path value) {
        generatedSourcesRoot = value;
        return this;
    }

    public MockConfig generatedResourcesRoot(Path value) {
        generatedResourcesRoot = value;
        return this;
    }

    public MockConfig projectNamespace(String value) {
        projectNamespace = value;
        return this;
    }

    public MockConfig packageName(String value) {
        packageName = value;
        return this;
    }

    public MockConfig contentType(ContentType value) {
        contentType = value;
        return this;
    }

    @Override
    public Path generatedSourcesRoot() {
        return generatedSourcesRoot;
    }

    @Override
    public Path generatedResourcesRoot() {
        return generatedResourcesRoot;
    }

    @Override
    public String projectNamespace() {
        return projectNamespace;
    }

    @Override
    public String packageName() {
        return packageName;
    }

    @Override
    public ContentType contentType() {
        return contentType;
    }

    @Override
    public ClassLoader classLoader() {
        return getClass().getClassLoader();
    }
}
