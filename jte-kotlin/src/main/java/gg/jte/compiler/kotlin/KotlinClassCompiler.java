package gg.jte.compiler.kotlin;

import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.compiler.ClassCompiler;
import gg.jte.runtime.ClassInfo;
import gg.jte.runtime.Constants;
import org.jetbrains.kotlin.cli.common.ExitCode;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.config.Services;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused") // Used by gg.jte.compiler.TemplateCompiler
public class KotlinClassCompiler implements ClassCompiler {
    @Override
    public void compile(String[] files, List<String> compilePath, TemplateConfig config, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        K2JVMCompilerArguments compilerArguments = new K2JVMCompilerArguments();
        //compilerArguments.setJvmTarget("");
        compilerArguments.setJavaParameters(true);
        compilerArguments.setNoStdlib(true);
        compilerArguments.setDestination(classDirectory.toFile().getAbsolutePath());

        compilerArguments.setFreeArgs(Arrays.asList(files));

        compilerArguments.setClasspath(resolveClasspath(compilePath));

        K2JVMCompiler compiler = new K2JVMCompiler();

        SimpleKotlinCompilerMessageCollector messageCollector = new SimpleKotlinCompilerMessageCollector(templateByClassName, config.packageName);
        ExitCode exitCode = compiler.exec(messageCollector, new Services.Builder().build(), compilerArguments);

        if (exitCode != ExitCode.OK && exitCode != ExitCode.COMPILATION_ERROR) {
            throw new TemplateException(messageCollector.getErrorMessage());
        }

        if (messageCollector.hasErrors()) {
            throw new TemplateException(messageCollector.getErrorMessage());
        }
    }

    private String resolveClasspath(List<String> compilePath) {
        if (compilePath != null) {
            return String.join(File.pathSeparator, compilePath);
        } else {
            return resolveClasspathFromClassLoader();
        }
    }

    private String resolveClasspathFromClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        StringBuilder classpath = new StringBuilder();
        String separator = System.getProperty("path.separator");
        String prop = System.getProperty("java.class.path");

        if (prop != null && !"".equals(prop)) {
            classpath.append(prop);
        }

        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                if (classpath.length() > 0) {
                    classpath.append(separator);
                }

                if ("file".equals(url.getProtocol())) {
                    try {
                        classpath.append(new File(url.toURI()));
                    } catch (URISyntaxException e) {
                        throw new TemplateException("Failed to append classpath for " + url, e);
                    }
                }
            }
        }

        return classpath.toString();
    }

    private static class SimpleKotlinCompilerMessageCollector implements MessageCollector {

        private final Map<String, ClassInfo> templateByClassName;
        private final List<String> errors = new ArrayList<>();
        private final String packageName;

        private String className;
        private int line;

        private SimpleKotlinCompilerMessageCollector(Map<String, ClassInfo> templateByClassName, String packageName) {
            this.templateByClassName = templateByClassName;
            this.packageName = packageName;
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        @Override
        public void report(CompilerMessageSeverity severity, @SuppressWarnings("NullableProblems") String s, CompilerMessageSourceLocation location) {
            if (severity.isError()) {
                if ((location != null) && (location.getLineContent() != null)) {
                    if (className == null) {
                        className = extractClassName(location);
                        line = location.getLine();
                    }

                    errors.add(String.format("%s%n%s:%d:%d%nReason: %s", location.getLineContent(), location.getPath(),
                            location.getLine(),
                            location.getColumn(), s));
                } else {
                    errors.add(s);
                }
            }
        }

        private String extractClassName(CompilerMessageSourceLocation location) {
            String path = location.getPath();
            path = path.replace('/', '.').replace('\\', '.');
            int packageIndex = path.indexOf(packageName);

            path = path.substring(packageIndex);

            // Remove .kt extension
            path = path.substring(0, path.length() - 3);

            return path;
        }

        public String getErrorMessage() {
            String allErrors = String.join("\n", errors);

            if (className != null) {
                ClassInfo templateInfo = templateByClassName.get(className);
                int templateLine = templateInfo.lineInfo[line - 1] + 1;

                return "Failed to compile template, error at " + templateInfo.name + ":" + templateLine + "\n" + allErrors;
            } else {
                return "Failed to compile template, error at\n" + errors;
            }
        }
    }
}
