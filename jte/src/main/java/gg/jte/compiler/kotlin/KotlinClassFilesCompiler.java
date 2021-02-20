package gg.jte.compiler.kotlin;

import org.jetbrains.kotlin.cli.common.ExitCode;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
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

public class KotlinClassFilesCompiler {
    public static void compile(Path outputDirectory, String[] files) {
        K2JVMCompilerArguments compilerArguments = new K2JVMCompilerArguments();
        //compilerArguments.setJvmTarget("");
        compilerArguments.setJavaParameters(true);
        compilerArguments.setNoStdlib(true);
        compilerArguments.setDestination(outputDirectory.toFile().getAbsolutePath());

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
                        e.printStackTrace(); // TODO
                    }
                }
            }
        }

        compilerArguments.setFreeArgs(Arrays.asList(files));

        compilerArguments.setClasspath(classpath.toString());

        K2JVMCompiler compiler = new K2JVMCompiler();

        SimpleKotlinCompilerMessageCollector messageCollector = new SimpleKotlinCompilerMessageCollector();
        ExitCode exitCode = compiler.exec(messageCollector, new Services.Builder().build(), compilerArguments);

        if (exitCode != ExitCode.OK && exitCode != ExitCode.COMPILATION_ERROR) {
            throw new RuntimeException("Unable to invoke Kotlin compiler. " + String.join("\n", messageCollector.getErrors()));
        }

        if (messageCollector.hasErrors()) {
            throw new RuntimeException("Compilation failed. " + String.join("\n", messageCollector.getErrors()));
        }
    }

    private static class SimpleKotlinCompilerMessageCollector implements MessageCollector {

        private final List<String> errors = new ArrayList<>();

        @Override
        public void clear() {
        }

        @Override
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        @Override
        public void report(CompilerMessageSeverity severity, String s, CompilerMessageLocation location) {
            if (severity.isError()) {
                if ((location != null) && (location.getLineContent() != null)) {
                    errors.add(String.format("%s%n%s:%d:%d%nReason: %s", location.getLineContent(), location.getPath(),
                            location.getLine(),
                            location.getColumn(), s));
                } else {
                    errors.add(s);
                }
            }
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
