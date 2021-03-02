package gg.jte.compiler;

import gg.jte.TemplateException;
import gg.jte.runtime.StringUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.function.Consumer;

public class ClassUtils {

    public static String join(List<String> classPath) {
        return String.join(File.pathSeparator, classPath);
    }

    public static void resolveClasspathFromClassLoader(ClassLoader classLoader, Consumer<String> pathConsumer) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        String separator = File.pathSeparator;
        String classPath = System.getProperty("java.class.path");

        if (!StringUtils.isBlank(classPath)) {
            String[] paths = classPath.split(separator);
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
