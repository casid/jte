package org.jusecase.jte.internal;

import org.jusecase.jte.CodeResolver;
import org.jusecase.jte.internal.TagOrLayoutParameterParser.ParamInfo;
import org.jusecase.jte.output.FileOutput;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    private final CodeResolver codeResolver;

    private final Path classDirectory;
    private final String packageName;
    private final boolean debug = false;
    private final ConcurrentHashMap<String, LinkedHashSet<String>> templateDependencies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ParamInfo>> paramOrder = new ConcurrentHashMap<>();

    public TemplateCompiler(CodeResolver codeResolver, Path classDirectory) {
        this(codeResolver, "org.jusecase.jte", classDirectory);
    }

    public TemplateCompiler(CodeResolver codeResolver, String packageName, Path classDirectory) {
        this.codeResolver = codeResolver;
        this.classDirectory = classDirectory;
        this.packageName = packageName;
    }

    public Template<?> compile(String name) {
        if (classDirectory == null) {
            return compileInMemory(name);
        } else {
            return loadPrecompiled(name, true);
        }
    }

    private Template<?> compileInMemory(String name) {
        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        ClassDefinition templateDefinition = generateTemplate(name, classDefinitions);
        if (templateDefinition == null) {
            return EmptyTemplate.INSTANCE;
        }

        try {
            ClassCompiler classCompiler = new ClassCompiler();
            return (Template<?>) classCompiler.compile(templateDefinition.getName(), classDefinitions).getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Template<?> loadPrecompiled(String name, boolean firstAttempt) {
        try {
            ClassInfo templateInfo = new ClassInfo(name, packageName);

            URLClassLoader classLoader = new URLClassLoader(new URL[]{classDirectory.toUri().toURL()});
            return (Template<?>) classLoader.loadClass(templateInfo.fullName).getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            if (firstAttempt) {
                precompile(List.of(name));
                return loadPrecompiled(name, false);
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanAll() {
        IoUtils.deleteDirectoryContent(classDirectory);
    }

    public void precompileAll() {
        precompile(codeResolver.resolveAllTemplateNames());
    }

    public void precompile(List<String> names) {
        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        for (String name : names) {
            generateTemplate(name, classDefinitions);
        }

        for (ClassDefinition classDefinition : classDefinitions) {
            try (FileOutput fileOutput = new FileOutput(classDirectory.resolve(classDefinition.getJavaFileName()))) {
                fileOutput.writeSafeContent(classDefinition.getCode());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        String[] files = new String[classDefinitions.size()];
        int i = 0;
        for (ClassDefinition classDefinition : classDefinitions) {
            files[i++] = classDirectory.resolve(classDefinition.getJavaFileName()).toFile().getAbsolutePath();
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, files);
    }

    private ClassDefinition generateTemplate(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        String templateCode = codeResolver.resolve(name);
        if (templateCode == null) {
            throw new RuntimeException("No code found for template " + name);
        }
        if (templateCode.isEmpty()) {
            return null;
        }

        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = new ClassInfo(name, packageName);

        TemplateParameterParser attributeParser = new TemplateParameterParser();
        attributeParser.parse(templateCode);

        StringBuilder javaCode = new StringBuilder("package " + templateInfo.packageName + ";\n");
        for (String importClass : attributeParser.importClasses) {
            javaCode.append("import ").append(importClass).append(";\n");
        }

        javaCode.append("public final class ").append(templateInfo.className).append(" implements org.jusecase.jte.internal.Template<").append(attributeParser.className).append("> {\n");
        javaCode.append("\tpublic void render(").append(attributeParser.className).append(" ").append(attributeParser.instanceName).append(", org.jusecase.jte.TemplateOutput output) {\n");

        new TemplateParser(TemplateType.Template).parse(attributeParser.lastIndex, templateCode, new CodeGenerator(TemplateType.Template, javaCode, classDefinitions, templateDependencies));
        javaCode.append("\t}\n");
        javaCode.append("}\n");

        this.templateDependencies.put(name, templateDependencies);

        ClassDefinition templateDefinition = new ClassDefinition(templateInfo.fullName);
        templateDefinition.setCode(javaCode.toString());
        classDefinitions.add(templateDefinition);

        if (debug) {
            System.out.println(templateDefinition.getCode());
        }

        return templateDefinition;
    }

    private ClassInfo generateTag(String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
        return generateTagOrLayout(TemplateType.Tag, name, classDefinitions, templateDependencies);
    }

    private ClassInfo generateLayout(String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
        return generateTagOrLayout(TemplateType.Layout, name, classDefinitions, templateDependencies);
    }

    private ClassInfo generateTagOrLayout(TemplateType type, String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
        templateDependencies.add(name);
        ClassInfo classInfo = new ClassInfo(name, packageName);

        ClassDefinition classDefinition = new ClassDefinition(classInfo.fullName);
        if (classDefinitions.contains(classDefinition)) {
            return classInfo;
        }

        String code = codeResolver.resolve(name);
        if (code == null) {
            throw new RuntimeException("No code found for " + type + ": " + name);
        }

        classDefinitions.add(classDefinition);

        TagOrLayoutParameterParser parameterParser = new TagOrLayoutParameterParser();
        int lastIndex = parameterParser.parse(code);

        StringBuilder javaCode = new StringBuilder("package " + classInfo.packageName + ";\n");
        for (String importClass : parameterParser.importClasses) {
            javaCode.append("import ").append(importClass).append(";\n");
        }

        javaCode.append("public final class ").append(classInfo.className).append(" {\n");
        javaCode.append("\tpublic static void render(org.jusecase.jte.TemplateOutput output");
        for (ParamInfo parameter : parameterParser.parameters) {
            javaCode.append(", ").append(parameter.type).append(' ').append(parameter.name);
        }
        paramOrder.put(name, parameterParser.parameters);
        if (type == TemplateType.Layout) {
            javaCode.append(", java.util.function.Function<String, Runnable> jteLayoutDefinitionLookup");
        }
        javaCode.append(") {\n");

        new TemplateParser(type).parse(lastIndex, code, new CodeGenerator(type, javaCode, classDefinitions, templateDependencies));

        javaCode.append("\t}\n");
        javaCode.append("}\n");

        classDefinition.setCode(javaCode.toString());

        if (debug) {
            System.out.println(classDefinition.getCode());
        }

        return classInfo;
    }

    public void clean(String name) {
        if (classDirectory == null) {
            return;
        }

        ClassInfo classInfo = new ClassInfo(name, packageName);
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


    private class CodeGenerator implements TemplateParserVisitor {
        private final TemplateType type;
        private final StringBuilder javaCode;
        private final LinkedHashSet<ClassDefinition> classDefinitions;
        private final LinkedHashSet<String> templateDependencies;

        private CodeGenerator(TemplateType type, StringBuilder javaCode, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
            this.type = type;
            this.javaCode = javaCode;
            this.classDefinitions = classDefinitions;
            this.templateDependencies = templateDependencies;
        }

        @Override
        public void onTextPart(int depth, String textPart) {
            if (textPart.isEmpty()) {
                return;
            }

            writeIndentation(depth);
            javaCode.append("output.writeSafeContent(\"");
            appendEscaped(textPart);
            javaCode.append("\");\n");
        }

        @Override
        public void onCodePart(int depth, String codePart) {
            writeIndentation(depth);
            javaCode.append("output.writeUnsafe(").append(codePart).append(");\n");
        }

        @Override
        public void onSafeCodePart(int depth, String codePart) {
            writeIndentation(depth);
            javaCode.append("output.writeSafe(").append(codePart).append(");\n");
        }

        @Override
        public void onCodeStatement(int depth, String codePart) {
            writeIndentation(depth);
            javaCode.append(codePart).append(";\n");
        }

        @Override
        public void onConditionStart(int depth, String condition) {
            writeIndentation(depth);
            javaCode.append("if (").append(condition).append(") {\n");
        }

        @Override
        public void onConditionElse(int depth, String condition) {
            writeIndentation(depth);
            javaCode.append("} else if (").append(condition).append(") {\n");
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
            ClassInfo tagInfo = generateTag(tagName, classDefinitions, templateDependencies);

            writeIndentation(depth);

            javaCode.append(tagInfo.fullName).append(".render(output");

            appendParams(tagName, params);
            javaCode.append(");\n");
        }

        @Override
        public void onLayout(int depth, String name, List<String> params) {
            String layoutName = LAYOUT_DIRECTORY + name.replace('.', '/') + LAYOUT_EXTENSION;
            ClassInfo layoutInfo = generateLayout(layoutName, classDefinitions, templateDependencies);

            writeIndentation(depth);
            javaCode.append(layoutInfo.fullName).append(".render(output");

            appendParams(layoutName, params);

            javaCode.append(", new java.util.function.Function<String, Runnable>() {\n");
            writeIndentation(depth + 1);
            javaCode.append("public Runnable apply(String jteLayoutDefinition) {\n");
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
            ParamCallInfo[] paramCallInfos = new ParamCallInfo[paramInfos.size()];
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
            writeIndentation(depth + 2);
            javaCode.append("if (\"").append(name.trim()).append("\".equals(jteLayoutDefinition)) {\n");
            writeIndentation(depth + 3);
            javaCode.append("return new Runnable() {\n");
            writeIndentation(depth + 4);
            javaCode.append("public void run() {\n");
        }

        @Override
        public void onLayoutDefineEnd(int depth) {
            writeIndentation(depth + 4);
            javaCode.append("}\n");
            writeIndentation(depth + 3);
            javaCode.append("};\n");
            writeIndentation(depth + 2);
            javaCode.append("}\n");
        }

        @Override
        public void onLayoutEnd(int depth) {
            writeIndentation(depth + 2);
            if (type == TemplateType.Layout) {
                javaCode.append("return jteLayoutDefinitionLookup.apply(jteLayoutDefinition);\n");
            } else {
                javaCode.append("return () -> {};\n");
            }
            writeIndentation(depth + 1);
            javaCode.append("}\n");
            writeIndentation(depth);
            javaCode.append("});\n");
        }

        @SuppressWarnings("StringRepeatCanBeUsed")
        private void writeIndentation(int depth) {
            for (int i = 0; i < depth + 2; ++i) {
                javaCode.append('\t');
            }
        }

        private void appendEscaped(String text) {
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
    }

    private static final class ClassInfo {
        final String className;
        final String packageName;
        final String fullName;

        ClassInfo(String name, String parentPackage) {
            int endIndex = name.lastIndexOf('.');
            if (endIndex == -1) {
                endIndex = name.length();
            }

            int startIndex = name.lastIndexOf('/');
            if (startIndex == -1) {
                startIndex = 0;
            } else {
                startIndex += 1;
            }

            className = CLASS_PREFIX + name.substring(startIndex, endIndex).replace("-", "") + CLASS_SUFFIX;
            if (startIndex == 0) {
                packageName = parentPackage;
            } else {
                packageName = parentPackage + "." + name.substring(0, startIndex - 1).replace('/', '.');
            }
            fullName = packageName + "." + className;
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
}
