package gg.jte.compiler.java;

import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.compiler.ClassCompiler;
import gg.jte.compiler.ClassUtils;
import gg.jte.runtime.ClassInfo;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JavaClassCompiler implements ClassCompiler {
    @Override
    public void compile(String[] files, List<String> classPath, TemplateConfig config, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        List<String> args = new ArrayList<>(files.length + classPath.size() + 8);

        if (config.compileArgs != null) {
            args.addAll(Arrays.asList(config.compileArgs));
        }
        args.add("-parameters");

        if (!classPath.isEmpty()) {
            args.add("-classpath");
            args.add(ClassUtils.join(classPath));
        }

        args.addAll(Arrays.asList(files));

        runCompiler(args.toArray(new String[0]), classDirectory, templateByClassName);
    }

    private void runCompiler(String[] args, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        try {
            int result = compiler.run(null, null, new PrintStream(errorStream, true, "UTF-8"), args);
            if (result != 0) {
                String errors = errorStream.toString("UTF-8");
                throw new TemplateException(getErrorMessage(errors, classDirectory, templateByClassName));
            }
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException("UTF-8 encoding not found, this is very unexpected!", e);
        }
    }

    private static String getErrorMessage(String errors, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        try {
            String absolutePath = classDirectory.toAbsolutePath().toString();
            int classBeginIndex = errors.indexOf(absolutePath) + absolutePath.length() + 1;
            int classEndIndex = errors.indexOf(".java:");
            String className = errors.substring(classBeginIndex, classEndIndex).replace(File.separatorChar, '.');

            int lineStartIndex = classEndIndex + 6;
            int lineEndIndex = errors.indexOf(':', lineStartIndex);
            int javaLine = Integer.parseInt(errors.substring(lineStartIndex, lineEndIndex));

            ClassInfo templateInfo = templateByClassName.get(className);
            int templateLine = templateInfo.lineInfo[javaLine - 1] + 1;

            return "Failed to compile template, error at " + templateInfo.name + ":" + templateLine + "\n" + errors;
        } catch (Exception e) {
            return "Failed to compile template, error at\n" + errors;
        }
    }
}
