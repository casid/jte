package gg.jte.compiler;

import gg.jte.TemplateException;
import gg.jte.runtime.StringUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Consumer;

public class ClassUtils {
    public static void resolveClasspathFromClassLoader(Consumer<String> pathConsumer) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String separator = System.getProperty("path.separator");
        String prop = System.getProperty("java.class.path");

        if (!StringUtils.isBlank(prop)) {
            String[] paths = prop.split(separator);
            for (String path : paths) {
                pathConsumer.accept(path);
            }
        }

        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                if ("file".equals(url.getProtocol())) {
                    try {
                        pathConsumer.accept(new File(url.toURI()).toString());
                    } catch (URISyntaxException e) {
                        throw new TemplateException("Failed to append classpath for " + url, e);
                    }
                }
            }
        }
    }
}
