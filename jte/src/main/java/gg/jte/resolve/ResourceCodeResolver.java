package gg.jte.resolve;

import gg.jte.CodeResolver;
import gg.jte.compiler.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Resolves template code within a given resources root.
 */
public class ResourceCodeResolver implements CodeResolver {
    private final String root;
    private final ClassLoader classLoader;

    public ResourceCodeResolver(String root) {
        this(root, null);
    }

    public ResourceCodeResolver(String root, ClassLoader classLoader) {
        if (root.isEmpty()) {
            this.root = "";
        } else {
            this.root = root + "/";
        }
        this.classLoader = classLoader;
    }

    @Override
    public String resolve(String name) {
        try (InputStream is = getClassLoader().getResourceAsStream(root + name)) {
            if (is == null) {
                return null;
            }
            return IoUtils.toString(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean exists(String name) {
        return getClassLoader().getResource(root + name) != null;
    }

    @Override
    public long getLastModified(String name) {
        URL res = getClassLoader().getResource(root + name);
        if (res == null) {
            return 0;
        }

        try {
            // Returns 0 if the file does not exist or an I/O error occurs.
            return new File(res.toURI()).lastModified();
        } catch (IllegalArgumentException | URISyntaxException e) {
            return 0;
        }
    }

    private ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        return Thread.currentThread().getContextClassLoader();
    }
}
