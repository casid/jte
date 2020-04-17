package org.jusecase.jte.internal;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

final class ClassCompiler {
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
    private final ByteArrayClassLoader classLoader = new ByteArrayClassLoader();
    private final Map<String, JavaFileObject> fileObjectMap = new HashMap<>();

    Class<?> compile(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        ClassLoader cl = lookup.lookupClass().getClassLoader();


        try {
            List<CharSequenceJavaFileObject> files = new ArrayList<>();
            for (ClassDefinition classDefinition : classDefinitions) {
                files.add(new CharSequenceJavaFileObject(classDefinition.getName(), classDefinition.getCode()));
            }
            StringWriter out = new StringWriter();

            StringBuilder classpath = new StringBuilder();
            String separator = System.getProperty("path.separator");
            String prop = System.getProperty("java.class.path");

            if (prop != null && !"".equals(prop))
                classpath.append(prop);

            if (cl instanceof URLClassLoader) {
                for (URL url : ((URLClassLoader) cl).getURLs()) {
                    if (classpath.length() > 0)
                        classpath.append(separator);

                    if ("file".equals(url.getProtocol()))
                        classpath.append(new File(url.toURI()));
                }
            }

            List<String> options = new ArrayList<>(Arrays.asList("-classpath", classpath.toString()));


            JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, null, options, null, files);

            task.call();

            if (fileManager.isEmpty()) {
                throw new RuntimeException("Compilation error: " + out);
            }


            return classLoader.loadClass(name);
        }
        catch (Exception e) {
            throw new RuntimeException("Error while compiling " + classDefinitions, e);
        }
    }

    final class ByteArrayClassLoader extends ClassLoader {
        ByteArrayClassLoader() {
        }

        @Override
        protected Class<?> findClass(String name) {
            byte[] bytes = fileObjectMap.get(name).getBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    static final class JavaFileObject extends SimpleJavaFileObject {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        JavaFileObject(String name, JavaFileObject.Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        byte[] getBytes() {
            return os.toByteArray();
        }

        @Override
        public OutputStream openOutputStream() {
            return os;
        }
    }

    final class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        ClassFileManager(StandardJavaFileManager standardManager) {
            super(standardManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
                JavaFileManager.Location location,
                String className,
                JavaFileObject.Kind kind,
                FileObject sibling
        ) {
            JavaFileObject result = new JavaFileObject(className, kind);
            fileObjectMap.put(className, result);
            return result;
        }

        boolean isEmpty() {
            return fileObjectMap.isEmpty();
        }
    }

    static final class CharSequenceJavaFileObject extends SimpleJavaFileObject {
        final CharSequence content;

        public CharSequenceJavaFileObject(String className, CharSequence content) {
            super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }
}
