package gg.jte.compiler.kotlin;

import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.compiler.ClassCompiler;
import gg.jte.compiler.ClassUtils;
import gg.jte.runtime.ClassInfo;
import org.jetbrains.kotlin.cli.common.ExitCode;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.config.Services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused") // Used by gg.jte.compiler.TemplateCompiler
public class KotlinClassCompiler implements ClassCompiler {
    @Override
    public void compile(String[] files, List<String> classPath, TemplateConfig config, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        K2JVMCompilerArguments compilerArguments = new K2JVMCompilerArguments();
        compilerArguments.setJavaParameters(true);
        compilerArguments.setNoStdlib(true);
        compilerArguments.setDestination(classDirectory.toFile().getAbsolutePath());

        compilerArguments.setFreeArgs(Arrays.asList(files));

        compilerArguments.setClasspath(ClassUtils.join(classPath));

        if (config.kotlinCompileArgs != null) {
            applyCompileArgs(compilerArguments, config.kotlinCompileArgs);
        }

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

    private void applyCompileArgs(K2JVMCompilerArguments compilerArguments, String[] kotlinCompileArgs) {
        for (int i = 0; i < kotlinCompileArgs.length; i++) {
            if ("-jvm-target".equals(kotlinCompileArgs[i])) {
                i++;
                compilerArguments.setJvmTarget(kotlinCompileArgs[i]);
            }
        }
    }

    private static class SimpleKotlinCompilerMessageCollector implements MessageCollector {

        private final Map<String, ClassInfo> templateByClassName;
        private final List<ErrorInfo> errors = new ArrayList<>();
        private final String packageName;

        private record ErrorInfo(String className, int line, String message) {}

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
                    String className = extractClassName(location);
                    int line = location.getLine();

                    errors.add(new ErrorInfo(className, line, "%s%n%s:%d:%d%nReason: %s".formatted(location.getLineContent(), location.getPath(),
                            location.getLine(),
                            location.getColumn(), s)));
                } else {
                    errors.add(new ErrorInfo(null, -1, s));
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
            String errorMessage = errors.stream()
                    .map(e -> {
                        if(e.className != null) {
                            ClassInfo templateInfo = templateByClassName.get(e.className);
                            int templateLine = templateInfo.lineInfo[e.line - 1] + 1;
                            return "%s:%d\n%s".formatted(templateInfo.name, templateLine, e.message.toString());
                        } else {
                            return e.toString();
                        }
                    })
                    .collect(Collectors.joining("\n"));
            return "Failed to compile template:\n" + errorMessage;
        }
    }
}
