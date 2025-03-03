package gg.jte.compiler.java;

import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.compiler.ClassCompiler;
import gg.jte.compiler.ClassUtils;
import gg.jte.runtime.ClassInfo;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaClassCompiler implements ClassCompiler {
    @Override
    public void compile(String[] files, List<String> classPath, TemplateConfig config, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        List<String> args = new ArrayList<>(files.length + classPath.size() + 8);

        if (config.compileArgs != null) {
            args.addAll(Arrays.asList(config.compileArgs));
        }
        args.add("-encoding");
        args.add("UTF-8");
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

        int result = compiler.run(null, null, new PrintStream(errorStream, true, StandardCharsets.UTF_8), args);
        if (result != 0) {
            String errors = errorStream.toString(StandardCharsets.UTF_8);
            throw new TemplateException(getErrorMessage(errors, classDirectory, templateByClassName));
        }
    }

    private static String getErrorMessage(String errors, Path classDirectory, Map<String, ClassInfo> templateByClassName) {
        try {
            String absolutePath = classDirectory.toAbsolutePath().toString();
            //Pattern matches '<absolutePath><separatorChar><relativeTemplatePath>.java:<Line>: error'
            Pattern pattern = Pattern.compile("^\\Q%s%s\\E(?<ClassName>.*?)\\.java:(?<LineNumber>\\d+?): error".formatted(absolutePath, File.separatorChar), Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(errors);
            if (!matcher.find()) {
                return "Failed to compile template, error at\n" + errors;
            }

            String className = matcher.group("ClassName").replace(File.separatorChar, '.');
            int javaLine = Integer.parseInt(matcher.group("LineNumber"));

            ClassInfo templateInfo = templateByClassName.get(className);
            int templateLine = templateInfo.lineInfo[javaLine - 1] + 1;

            return "Failed to compile template, error at " + templateInfo.name + ":" + templateLine + "\n" + errors;
        } catch (Exception e) {
            return "Failed to compile template, error at\n" + errors;
        }
    }
}
