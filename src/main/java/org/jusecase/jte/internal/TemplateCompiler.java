package org.jusecase.jte.internal;

import org.jusecase.jte.CodeResolver;
import org.jusecase.jte.TemplateException;
import org.jusecase.jte.TemplateMode;
import org.jusecase.jte.output.FileOutput;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateCompiler {

    public static final String TAG_EXTENSION = ".jte";
    public static final String LAYOUT_EXTENSION = ".jte";
    public static final String TAG_DIRECTORY = "tag/";
    public static final String LAYOUT_DIRECTORY = "layout/";
    public static final String CLASS_PREFIX = "Jte";
    public static final String CLASS_SUFFIX = "Generated";
    public static final String PACKAGE_NAME = "org.jusecase.jte.generated";
    public static final String LINE_INFO_FIELD = "LINE_INFO";
    public static final String TEXT_PART_STRING = "TEXT_PART_STRING_";
    public static final String TEXT_PART_BINARY = "TEXT_PART_BINARY_";
    public static final boolean DEBUG = false;

    private final CodeResolver codeResolver;

    private final Path classDirectory;
    private final TemplateMode templateMode;
    private final ClassLoader singleClassLoader;
    private final ConcurrentHashMap<String, LinkedHashSet<String>> templateDependencies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ParamInfo>> paramOrder = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClassInfo> templateByClassName = new ConcurrentHashMap<>();
    private boolean nullSafeTemplateCode;

    public TemplateCompiler(CodeResolver codeResolver, Path classDirectory, TemplateMode templateMode) {
        this.codeResolver = codeResolver;
        this.classDirectory = classDirectory;
        this.templateMode = templateMode;

        if (templateMode == TemplateMode.Precompiled) {
            singleClassLoader = createClassLoader();
        } else {
            singleClassLoader = null;
        }
    }

    public Template<?> compile(String name) {
        if (templateMode == TemplateMode.Dynamic) {
            precompile(List.of(name), null);
        }

        try {
            ClassInfo templateInfo = new ClassInfo(name, PACKAGE_NAME);
            return (Template<?>) getClassLoader().loadClass(templateInfo.fullName).getConstructor().newInstance();
        } catch (Exception e) {
            throw new TemplateException("Failed to load " + name, e);
        }
    }

    private ClassLoader getClassLoader() {
        if (singleClassLoader == null) {
            return createClassLoader();
        }
        return singleClassLoader;
    }

    private ClassLoader createClassLoader() {
        try {
            return new URLClassLoader(new URL[]{classDirectory.toUri().toURL()});
        } catch (MalformedURLException e) {
            throw new TemplateException("Failed to create class loader for " + classDirectory, e);
        }
    }

    public void cleanAll() {
        IoUtils.deleteDirectoryContent(classDirectory);
    }

    public void precompileAll(List<String> compilePath) {
        precompile(codeResolver.resolveAllTemplateNames(), compilePath);
    }

    public void precompile(List<String> names, List<String> compilePath) {
        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        for (String name : names) {
            generateTemplate(name, classDefinitions);
        }

        for (ClassDefinition classDefinition : classDefinitions) {
            try (FileOutput fileOutput = new FileOutput(classDirectory.resolve(classDefinition.getJavaFileName()))) {
                fileOutput.writeContent(classDefinition.getCode());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        String[] files = new String[classDefinitions.size()];
        int i = 0;
        for (ClassDefinition classDefinition : classDefinitions) {
            files[i++] = classDirectory.resolve(classDefinition.getJavaFileName()).toFile().getAbsolutePath();
        }

        ClassFilesCompiler.compile(files, compilePath, classDirectory, templateByClassName);
    }

    private void generateTemplate(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        String templateCode = resolveCode(TemplateType.Template, name, null);

        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = new ClassInfo(name, PACKAGE_NAME);

        CodeGenerator codeGenerator = new CodeGenerator(templateInfo, TemplateType.Template, classDefinitions, templateDependencies);
        new TemplateParser(TemplateType.Template, codeGenerator).parse(templateCode);

        this.templateDependencies.put(name, templateDependencies);

        ClassDefinition templateDefinition = new ClassDefinition(templateInfo.fullName);
        templateDefinition.setCode(codeGenerator.getCode());
        classDefinitions.add(templateDefinition);

        templateByClassName.put(templateDefinition.getName(), templateInfo);

        if (DEBUG) {
            System.out.println(templateDefinition.getCode());
        }
    }

    private ClassInfo generateTag(String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies, DebugInfo debugInfo) {
        return generateTagOrLayout(TemplateType.Tag, name, classDefinitions, templateDependencies, debugInfo);
    }

    private ClassInfo generateLayout(String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies, DebugInfo debugInfo) {
        return generateTagOrLayout(TemplateType.Layout, name, classDefinitions, templateDependencies, debugInfo);
    }

    private ClassInfo generateTagOrLayout(TemplateType type, String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies, DebugInfo debugInfo) {
        templateDependencies.add(name);
        ClassInfo classInfo = new ClassInfo(name, PACKAGE_NAME);

        ClassDefinition classDefinition = new ClassDefinition(classInfo.fullName);
        if (classDefinitions.contains(classDefinition)) {
            return classInfo;
        }

        String code = resolveCode(type, name, debugInfo);

        classDefinitions.add(classDefinition);

        CodeGenerator codeGenerator = new CodeGenerator(classInfo, type, classDefinitions, templateDependencies);
        new TemplateParser(type, codeGenerator).parse(code);

        classDefinition.setCode(codeGenerator.getCode());
        templateByClassName.put(classDefinition.getName(), classInfo);

        if (DEBUG) {
            System.out.println(classDefinition.getCode());
        }

        return classInfo;
    }

    private String resolveCode(TemplateType type, String name, DebugInfo debugInfo) {
        String code = codeResolver.resolve(name);
        if (code == null) {
            String message = type + " not found: " + name;
            if (debugInfo != null) {
                message += ", referenced at " + debugInfo.name + ":" + debugInfo.line;
            }
            throw new TemplateException(message);
        }
        return code;
    }

    public void clean(String name) {
        if (classDirectory == null) {
            return;
        }

        ClassInfo classInfo = new ClassInfo(name, PACKAGE_NAME);
        ClassDefinition classDefinition = new ClassDefinition(classInfo.fullName);

        deleteFile(classDirectory.resolve(classDefinition.getJavaFileName()));
        deleteFile(classDirectory.resolve(classDefinition.getClassFileName()));

        paramOrder.remove(name);
    }

    private void deleteFile(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file " + file, e);
        }
    }

    public List<String> getTemplatesUsing(String name) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> dependencies : templateDependencies.entrySet()) {
            if (dependencies.getValue().contains(name)) {
                result.add(dependencies.getKey());
            }
        }

        return result;
    }

    public void setNullSafeTemplateCode(boolean nullSafeTemplateCode) {
        this.nullSafeTemplateCode = nullSafeTemplateCode;
    }

    public DebugInfo resolveDebugInfo(ClassLoader classLoader, StackTraceElement[] stackTrace) {
        if (stackTrace.length == 0) {
            return null;
        }

        for (StackTraceElement stackTraceElement : stackTrace) {
           if (stackTraceElement.getClassName().startsWith(PACKAGE_NAME)) {
               ClassInfo classInfo = templateByClassName.get(stackTraceElement.getClassName());
               if (classInfo != null) {
                   return new DebugInfo(classInfo.name, resolveLineNumber(classLoader, classInfo, stackTraceElement.getLineNumber()));
               }
           }
        }

        return null;
    }

    private int resolveLineNumber(ClassLoader classLoader, ClassInfo classInfo, int lineNumber) {
        try {
            Class<?> clazz = classLoader.loadClass(classInfo.fullName);
            Field lineInfoField = clazz.getField(LINE_INFO_FIELD);
            int[] javaLineToTemplateLine = (int[]) lineInfoField.get(null);
            return javaLineToTemplateLine[lineNumber - 1] + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private class CodeGenerator implements TemplateParserVisitor {
        private final ClassInfo classInfo;
        private final TemplateType type;
        private final CodeBuilder javaCode = new CodeBuilder();
        private final LinkedHashSet<ClassDefinition> classDefinitions;
        private final LinkedHashSet<String> templateDependencies;
        private final List<ParamInfo> parameters = new ArrayList<>();
        private final List<String> textParts = new ArrayList<>();
        private final Deque<LayoutStack> layoutStack = new ArrayDeque<>();

        private boolean hasWrittenPackage;
        private boolean hasWrittenClass;

        private CodeGenerator(ClassInfo classInfo, TemplateType type, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
            this.classInfo = classInfo;
            this.type = type;
            this.classDefinitions = classDefinitions;
            this.templateDependencies = templateDependencies;
        }

        @Override
        public void onImport(String importClass) {
            writePackageIfRequired();
            javaCode.append("import ").append(importClass).append(";\n");
        }

        private void writePackageIfRequired() {
            if (!hasWrittenPackage) {
                javaCode.append("package " + classInfo.packageName + ";\n");
                hasWrittenPackage = true;
            }
        }

        @Override
        public void onParam(ParamInfo parameter) {
            writePackageIfRequired();
            if (!hasWrittenClass) {
                if (type == TemplateType.Template) {
                    writeTemplateClass(parameter);
                } else {
                    writeTagOrLayoutClass();
                }
            }

            javaCode.append(", ").append(parameter.type).append(' ').append(parameter.name);

            parameters.add(parameter);
        }

        private void writeTemplateClass(ParamInfo parameter) {
            javaCode.append("public final class ").append(classInfo.className).append(" implements org.jusecase.jte.internal.Template<").append(parameter.type).append("> {\n");
            javaCode.markFieldsIndex();
            javaCode.append("\tpublic void render(org.jusecase.jte.TemplateOutput output");

            hasWrittenClass = true;
        }

        private void writeTagOrLayoutClass() {
            javaCode.append("public final class ").append(classInfo.className).append(" {\n");
            javaCode.markFieldsIndex();
            javaCode.append("\tpublic static void render(org.jusecase.jte.TemplateOutput output");

            if (type == TemplateType.Layout) {
                javaCode.append(", java.util.function.Function<String, Runnable> jteLayoutDefinitionLookup");
            }

            hasWrittenClass = true;
        }

        @Override
        public void onParamsComplete() {
            writePackageIfRequired();
            if (!hasWrittenClass) {
                if (type == TemplateType.Template) {
                    // The user wrote a base template without any parameters
                    ParamInfo dummyParameter = new ParamInfo("Object model");
                    writeTemplateClass(dummyParameter);
                    onParam(dummyParameter);
                } else {
                    writeTagOrLayoutClass();
                }
            }

            javaCode.append(") {\n");

            paramOrder.put(classInfo.name, parameters);
        }

        @Override
        public void onLineFinished() {
            javaCode.finishTemplateLine();
        }

        @Override
        public void onComplete() {
            int lineCount = textParts.size() * 2 + 1;
            javaCode.insertFieldLines(lineCount);

            StringBuilder fields = new StringBuilder(64 + 32 * lineCount);
            javaCode.addLineInfoField(fields);
            for (int i = 0; i < textParts.size(); i++) {
                fields.append("\tprivate static final String ").append(TEXT_PART_STRING).append(i).append(" = \"");
                appendEscaped(fields, textParts.get(i));
                fields.append("\";\n");
                fields.append("\tprivate static final byte[] ").append(TEXT_PART_BINARY).append(i).append(" = org.jusecase.jte.internal.IoUtils.getUtf8Bytes(").append(TEXT_PART_STRING).append(i).append(");\n");
            }

            javaCode.insertFields(fields);

            javaCode.append("\t}\n");
            javaCode.append("}\n");

            this.classInfo.lineInfo = javaCode.getLineInfo();
        }

        @Override
        public void onTextPart(int depth, String textPart) {
            if (textPart.isEmpty()) {
                return;
            }

            writeIndentation(depth);
            javaCode.append("output.writeStaticContent(");
            javaCode.append(TEXT_PART_STRING).append(textParts.size()).append(", ");
            javaCode.append(TEXT_PART_BINARY).append(textParts.size());
            javaCode.append(");\n");

            textParts.add(textPart);
        }

        @Override
        public void onCodePart(int depth, String codePart) {
            writeCodePart(depth, codePart, "output.writeSafe(");
        }

        @Override
        public void onUnsafeCodePart(int depth, String codePart) {
            writeCodePart(depth, codePart, "output.writeUnsafe(");
        }

        private void writeCodePart(int depth, String codePart, String method) {
            writeIndentation(depth);
            if (nullSafeTemplateCode) {
                javaCode.append("try {\n");
                writeIndentation(depth + 1);
            }
            javaCode.append(method).append(codePart).append(");\n");
            if (nullSafeTemplateCode) {
                writeIndentation(depth);
                javaCode.append("} catch (NullPointerException outputNpe) {\n");
                writeIndentation(depth + 1);
                javaCode.append("org.jusecase.jte.internal.NullCheck.handleNullOutput(outputNpe);\n");
                writeIndentation(depth);
                javaCode.append("}\n");
            }
        }

        @Override
        public void onCodeStatement(int depth, String codePart) {
            writeIndentation(depth);
            if (nullSafeTemplateCode) {
                int index = codePart.indexOf('=');
                if (index == -1) {
                    javaCode.append("org.jusecase.jte.internal.NullCheck.evaluate(() -> ");
                    javaCode.append(codePart);
                    javaCode.append(")");
                } else {
                    javaCode.append(codePart, 0, index + 1);
                    javaCode.append(" org.jusecase.jte.internal.NullCheck.evaluate(() -> ");
                    javaCode.append(codePart, index + 2, codePart.length());
                    javaCode.append(")");
                }
            } else {
                javaCode.append(codePart);
            }
            javaCode.append(";\n");
        }

        @Override
        public void onConditionStart(int depth, String condition) {
            writeIndentation(depth);
            javaCode.append("if (");

            if (nullSafeTemplateCode) {
                javaCode.append("org.jusecase.jte.internal.NullCheck.evaluate(() -> ").append(condition).append(")");
            } else {
                javaCode.append(condition);
            }

            javaCode.append(") {\n");
        }

        @Override
        public void onConditionElse(int depth, String condition) {
            writeIndentation(depth);
            javaCode.append("} else if (");

            if (nullSafeTemplateCode) {
                javaCode.append("org.jusecase.jte.internal.NullCheck.evaluate(() -> ").append(condition).append(")");
            } else {
                javaCode.append(condition);
            }

            javaCode.append(") {\n");
        }

        @Override
        public void onConditionElse(int depth) {
            writeIndentation(depth);
            javaCode.append("} else {\n");
        }

        @Override
        public void onConditionEnd(int depth) {
            writeIndentation(depth);
            javaCode.append("}\n");
        }

        @Override
        public void onForLoopStart(int depth, String codePart) {
            writeIndentation(depth);
            javaCode.append("for (").append(codePart).append(") {\n");
        }

        @Override
        public void onForLoopEnd(int depth) {
            writeIndentation(depth);
            javaCode.append("}\n");
        }

        @Override
        public void onTag(int depth, String name, List<String> params) {
            String tagName = TAG_DIRECTORY + name.replace('.', '/') + TAG_EXTENSION;
            ClassInfo tagInfo = generateTag(tagName, classDefinitions, templateDependencies, getCurrentDebugInfo());

            writeIndentation(depth);

            javaCode.append(tagInfo.fullName).append(".render(output");

            appendParams(tagName, params);
            javaCode.append(");\n");
        }

        @Override
        public void onLayout(int depth, String name, List<String> params) {
            String layoutName = LAYOUT_DIRECTORY + name.replace('.', '/') + LAYOUT_EXTENSION;
            ClassInfo layoutInfo = generateLayout(layoutName, classDefinitions, templateDependencies, getCurrentDebugInfo());

            writeIndentation(depth);
            javaCode.append(layoutInfo.fullName).append(".render(output");

            javaCode.append(", jteLayoutDefinition -> {\n");

            layoutStack.push(new LayoutStack(layoutName, params));
        }

        private DebugInfo getCurrentDebugInfo() {
            return new DebugInfo(classInfo.name, javaCode.getCurrentTemplateLine() + 1);
        }

        private void appendParams(String name, List<String> params) {
            List<ParamInfo> paramInfos = paramOrder.get(name);
            if (paramInfos == null) {
                throw new IllegalStateException("No parameter information for " + name);
            }

            if (paramInfos.isEmpty()) {
                return;
            }

            int index = 0;
            ParamCallInfo[] paramCallInfos = new ParamCallInfo[Math.max(params.size(), paramInfos.size())];
            for (String param : params) {
                ParamCallInfo paramCallInfo = new ParamCallInfo(param);
                int parameterIndex = getParameterIndex(name, paramInfos, paramCallInfo);
                if (parameterIndex == -1) {
                    parameterIndex = index;
                }
                paramCallInfos[parameterIndex] = paramCallInfo;

                ++index;
            }

            for (int i = 0; i < paramCallInfos.length; i++) {
                ParamCallInfo paramCallInfo = paramCallInfos[i];
                if (paramCallInfo != null) {
                    javaCode.append(", ").append(paramCallInfo.data);
                } else {
                    ParamInfo paramInfo = paramInfos.get(i);
                    if (paramInfo.defaultValue != null) {
                        javaCode.append(", ").append(paramInfo.defaultValue);
                    }
                }
            }
        }

        private int getParameterIndex(String name, List<ParamInfo> paramInfos, ParamCallInfo paramCallInfo) {
            if (paramCallInfo.name == null) {
                return -1;
            }

            for (int i = 0; i < paramInfos.size(); ++i) {
                if (paramInfos.get(i).name.equals(paramCallInfo.name)) {
                    return i;
                }
            }
            throw new IllegalStateException("No parameter with name " + paramCallInfo.name + " is defined in " + name);
        }

        @Override
        public void onLayoutRender(int depth, String name) {
            writeIndentation(depth);
            javaCode.append("jteLayoutDefinitionLookup.apply(\"").append(name.trim()).append("\").run();\n");
        }

        @Override
        public void onLayoutDefine(int depth, String name) {
            writeIndentation(depth + 1);
            javaCode.append("if (\"").append(name.trim()).append("\".equals(jteLayoutDefinition)) {\n");
            writeIndentation(depth + 2);
            javaCode.append("return () -> {\n");
        }

        @Override
        public void onLayoutDefineEnd(int depth) {
            writeIndentation(depth + 2);
            javaCode.append("};\n");
            writeIndentation(depth + 1);
            javaCode.append("}\n");
        }

        @Override
        public void onLayoutEnd(int depth) {
            writeIndentation(depth + 1);
            if (type == TemplateType.Layout) {
                javaCode.append("return jteLayoutDefinitionLookup.apply(jteLayoutDefinition);\n");
            } else {
                javaCode.append("return () -> {};\n");
            }
            writeIndentation(depth);
            javaCode.append("}");

            if (!layoutStack.isEmpty()) {
                LayoutStack stack = layoutStack.pop();
                appendParams(stack.name, stack.params);
            }

            javaCode.append(");\n");
        }

        private void writeIndentation(int depth) {
            for (int i = 0; i < depth + 2; ++i) {
                javaCode.append('\t');
            }
        }

        private void appendEscaped(StringBuilder javaCode, String text) {
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                if (c == '\"') {
                    javaCode.append("\\\"");
                } else if (c == '\n') {
                    javaCode.append("\\n");
                } else if (c == '\t') {
                    javaCode.append("\\t");
                } else if (c == '\r') {
                    javaCode.append("\\r");
                } else if (c == '\f') {
                    javaCode.append("\\f");
                } else if (c == '\b') {
                    javaCode.append("\\b");
                } else if (c == '\\') {
                    javaCode.append("\\\\");
                } else {
                    javaCode.append(c);
                }
            }
        }

        public String getCode() {
            return javaCode.getCode();
        }
    }

    private static final class ParamCallInfo {
        final String name;
        final String data;

        public ParamCallInfo(String param) {
            param = param.trim();

            int nameEndIndex = -1;
            int dataStartIndex = -1;

            for (int i = 0; i < param.length(); ++i) {
                char character = param.charAt(i);
                if (nameEndIndex == -1) {
                    if (character == '"' || character == '\'') {
                        break;
                    }
                    if (character == '=') {
                        nameEndIndex = i;
                    }
                } else if (dataStartIndex == -1) {
                    if (!Character.isWhitespace(character)) {
                        dataStartIndex = i;
                    }
                }
            }

            if (nameEndIndex != -1 && dataStartIndex != -1) {
                name = param.substring(0, nameEndIndex).trim();
                data = param.substring(dataStartIndex).trim();
            } else {
                name = null;
                data = param;
            }
        }
    }

    private static class LayoutStack {
        public final String name;
        public final List<String> params;

        public LayoutStack(String name, List<String> params) {
            this.name = name;
            this.params = params;
        }
    }
}
