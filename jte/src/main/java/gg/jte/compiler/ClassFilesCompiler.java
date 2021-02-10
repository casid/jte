package gg.jte.compiler;

import gg.jte.TemplateException;
import gg.jte.runtime.ClassInfo;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClassFilesCompiler {
    public static void compile(String[] files, List<String> compilePath, String[] compileArgs, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        List<String> args = new ArrayList<>(files.length);

        if (compileArgs != null) {
            args.addAll(Arrays.asList(compileArgs));
        }
        args.add("-parameters");

        if (compilePath != null && !compilePath.isEmpty()) {
            args.add("-classpath");
            args.add(String.join(File.pathSeparator, compilePath));
        }

        args.addAll(Arrays.asList(files));

        runCompiler(args.toArray(new String[0]), classDirectory, templateByClassName);
    }

    private static void runCompiler(String[] args, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
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
