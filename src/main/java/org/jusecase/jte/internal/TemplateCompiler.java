package org.jusecase.jte.internal;

import org.jusecase.jte.CodeResolver;
import org.jusecase.jte.output.FileOutput;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

public class TemplateCompiler {

    public static final String TAG_EXTENSION = ".jte";
    public static final String LAYOUT_EXTENSION = ".jte";
    public static final String CLASS_PREFIX = "Jte";
    public static final String CLASS_SUFFIX = "Generated";

    private final CodeResolver codeResolver;

    private final Path classDirectory;
    private final String templatePackageName;
    private final String tagPackageName;
    private final String layoutPackageName;
    private final boolean debug = false;

    public TemplateCompiler(CodeResolver codeResolver) {
        this(codeResolver, null);
    }

    public TemplateCompiler(CodeResolver codeResolver, Path classDirectory) {
        this(codeResolver, "org.jusecase.jte", classDirectory);
    }

    public TemplateCompiler(CodeResolver codeResolver, String packageName, Path classDirectory) {
        this.codeResolver = codeResolver;
        this.classDirectory = classDirectory;
        this.templatePackageName = packageName + ".templates";
        this.tagPackageName = packageName + ".tags";
        this.layoutPackageName = packageName + ".layouts";
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
        ClassDefinition templateDefinition = generateJavaCode(name, classDefinitions);
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
            ClassInfo templateInfo = new ClassInfo(name, templatePackageName);

            URLClassLoader classLoader = new URLClassLoader(new URL[]{classDirectory.toUri().toURL()});
            return (Template<?>) classLoader.loadClass(templateInfo.fullName).getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            if (firstAttempt) {
                generateJavaCode(List.of(name));
                return loadPrecompiled(name, false);
            } else {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void generateJavaCode(List<String> names) {
        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        for (String name : names) {
            generateJavaCode(name, classDefinitions);
        }

        for (ClassDefinition classDefinition : classDefinitions) {
            try (FileOutput fileOutput = new FileOutput(classDirectory.resolve(classDefinition.getFileName()))) {
                fileOutput.write(classDefinition.getCode());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        String[] files = new String[classDefinitions.size()];
        int i = 0;
        for (ClassDefinition classDefinition : classDefinitions) {
            files[i++] = classDirectory.resolve(classDefinition.getFileName()).toFile().getAbsolutePath();
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, files);

    }

    private ClassDefinition generateJavaCode(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        String templateCode = codeResolver.resolve(name);
        if (templateCode == null) {
            throw new RuntimeException("No code found for template " + name);
        }
        if (templateCode.isEmpty()) {
            return null;
        }

        ClassInfo templateInfo = new ClassInfo(name, templatePackageName);

        TemplateParameterParser attributeParser = new TemplateParameterParser();
        attributeParser.parse(templateCode);

        StringBuilder javaCode = new StringBuilder("package " + templateInfo.packageName + ";\n");
        for (String importClass : attributeParser.importClasses) {
            javaCode.append("import ").append(importClass).append(";\n");
        }

        javaCode.append("public final class ").append(templateInfo.className).append(" implements org.jusecase.jte.internal.Template<").append(attributeParser.className).append("> {\n");
        javaCode.append("\tpublic void render(").append(attributeParser.className).append(" ").append(attributeParser.instanceName).append(", org.jusecase.jte.TemplateOutput output) {\n");

        new TemplateParser(TemplateType.Template).parse(attributeParser.lastIndex, templateCode, new CodeGenerator(TemplateType.Template, javaCode, classDefinitions));
        javaCode.append("\t}\n");
        javaCode.append("}\n");

        ClassDefinition templateDefinition = new ClassDefinition(templateInfo.fullName);
        templateDefinition.setCode(javaCode.toString());
        classDefinitions.add(templateDefinition);

        if (debug) {
            System.out.println(templateDefinition.getCode());
        }

        return templateDefinition;
    }

    private ClassInfo compileTag(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        ClassInfo tagInfo = new ClassInfo(name, tagPackageName);

        ClassDefinition classDefinition = new ClassDefinition(tagInfo.fullName);
        if (classDefinitions.contains(classDefinition)) {
            return tagInfo;
        }

        String tagCode = codeResolver.resolve(name);
        if (tagCode == null) {
            throw new RuntimeException("No code found for tag " + name);
        }

        classDefinitions.add(classDefinition);

        TagOrLayoutParameterParser parameterParser = new TagOrLayoutParameterParser();
        int lastIndex = parameterParser.parse(tagCode);

        StringBuilder javaCode = new StringBuilder("package " + tagInfo.packageName + ";\n");
        for (String importClass : parameterParser.importClasses) {
            javaCode.append("import ").append(importClass).append(";\n");
        }
        javaCode.append("public final class ").append(tagInfo.className).append(" {\n");
        javaCode.append("\tpublic static void render(org.jusecase.jte.TemplateOutput output");
        for (String parameter : parameterParser.parameters) {
            javaCode.append(", ").append(parameter);
        }
        javaCode.append(") {\n");

        new TemplateParser(TemplateType.Tag).parse(lastIndex, tagCode, new CodeGenerator(TemplateType.Tag, javaCode, classDefinitions));

        javaCode.append("\t}\n");
        javaCode.append("}\n");

        classDefinition.setCode(javaCode.toString());

        if (debug) {
            System.out.println(classDefinition.getCode());
        }

        return tagInfo;
    }

    private ClassInfo compileLayout(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        ClassInfo layoutInfo = new ClassInfo(name, layoutPackageName);

        ClassDefinition classDefinition = new ClassDefinition(layoutInfo.fullName);
        if (classDefinitions.contains(classDefinition)) {
            return layoutInfo;
        }

        String layoutCode = codeResolver.resolve(name);
        if (layoutCode == null) {
            throw new RuntimeException("No code found for layout " + name);
        }

        classDefinitions.add(classDefinition);

        TagOrLayoutParameterParser parameterParser = new TagOrLayoutParameterParser();
        int lastIndex = parameterParser.parse(layoutCode);

        StringBuilder javaCode = new StringBuilder("package " + layoutInfo.packageName + ";\n");
        for (String importClass : parameterParser.importClasses) {
            javaCode.append("import ").append(importClass).append(";\n");
        }
        javaCode.append("public final class ").append(layoutInfo.className).append(" {\n");
        javaCode.append("\tpublic static void render(org.jusecase.jte.TemplateOutput output");
        for (String parameter : parameterParser.parameters) {
            javaCode.append(", ").append(parameter);
        }
        javaCode.append(", java.util.function.Function<String, Runnable> jteLayoutSectionLookup");
        javaCode.append(") {\n");

        new TemplateParser(TemplateType.Layout).parse(lastIndex, layoutCode, new CodeGenerator(TemplateType.Layout, javaCode, classDefinitions));

        javaCode.append("\t}\n");
        javaCode.append("}\n");

        classDefinition.setCode(javaCode.toString());

        if (debug) {
            System.out.println(classDefinition.getCode());
        }

        return layoutInfo;
    }


    private class CodeGenerator implements TemplateParserVisitor {
        private final TemplateType type;
        private final LinkedHashSet<ClassDefinition> classDefinitions;
        private final StringBuilder javaCode;

        private CodeGenerator(TemplateType type, StringBuilder javaCode, LinkedHashSet<ClassDefinition> classDefinitions) {
            this.type = type;
            this.javaCode = javaCode;
            this.classDefinitions = classDefinitions;
        }

        @Override
        public void onTextPart(int depth, String textPart) {
            if (textPart.isEmpty()) {
                return;
            }

            writeIndentation(depth);
            javaCode.append("output.write(\"");
            appendEscaped(textPart);
            javaCode.append("\");\n");
        }

        @Override
        public void onCodePart(int depth, String codePart) {
            writeIndentation(depth);
            javaCode.append("output.write(").append(codePart).append(");\n");
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
        public void onTag(int depth, String name, String params) {
            ClassInfo tagInfo = compileTag(name.replace('.', '/') + TAG_EXTENSION, classDefinitions);

            writeIndentation(depth);

            javaCode.append(tagInfo.fullName).append(".render(output");

            if (!params.isBlank()) {
                javaCode.append(", ").append(params);
            }
            javaCode.append(");\n");
        }

        @Override
        public void onLayout(int depth, String name, String params) {
            ClassInfo layoutInfo = compileLayout(name.replace('.', '/') + LAYOUT_EXTENSION, classDefinitions);

            writeIndentation(depth);
            javaCode.append(layoutInfo.fullName).append(".render(output");

            if (!params.isBlank()) {
                javaCode.append(", ").append(params);
            }

            javaCode.append(", new java.util.function.Function<String, Runnable>() {\n");
            writeIndentation(depth + 1);
            javaCode.append("public Runnable apply(String jteLayoutSection) {\n");
        }

        @Override
        public void onLayoutSection(int depth, String name) {
            if (type == TemplateType.Layout) {
                writeIndentation(depth);
                javaCode.append("jteLayoutSectionLookup.apply(\"").append(name.trim()).append("\").run();\n");
            } else {
                writeIndentation(depth + 2);
                javaCode.append("if (\"").append(name.trim()).append("\".equals(jteLayoutSection)) {\n");
                writeIndentation(depth + 3);
                javaCode.append("return new Runnable() {\n");
                writeIndentation(depth + 4);
                javaCode.append("public void run() {\n");
            }
        }

        @Override
        public void onLayoutSectionEnd(int depth) {
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
            javaCode.append("return null;\n");
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

            className = CLASS_PREFIX + name.substring(startIndex, endIndex) + CLASS_SUFFIX;
            if (startIndex == 0) {
                packageName = parentPackage;
            } else {
                packageName = parentPackage + "." + name.substring(0, startIndex - 1).replace('/', '.');
            }
            fullName = packageName + "." + className;
        }


    }
}
