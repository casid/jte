package org.jusecase.jte.internal;

import org.jusecase.jte.CodeResolver;

import java.util.LinkedHashSet;

public class TemplateCompiler {

    private final CodeResolver codeResolver;

    private final String templatePackageName;
    private final String templatePackagePrefix;

    private final String tagPackageName;
    private final String tagPackagePrefix;

    public TemplateCompiler(CodeResolver codeResolver) {
        this(codeResolver, "org.jusecase.jte");

    }

    public TemplateCompiler(CodeResolver codeResolver, String packageName) {
        this.codeResolver = codeResolver;
        this.templatePackageName = packageName + ".templates";
        this.templatePackagePrefix = templatePackageName + ".";
        this.tagPackageName = packageName + ".tags";
        this.tagPackagePrefix = tagPackageName + ".";
    }


    public Template<?> compile(String name) {
        String templateCode = codeResolver.resolveTemplate(name);
        if (templateCode == null) {
            throw new RuntimeException("No code found for template " + name);
        }

        TemplateParameterParser attributeParser = new TemplateParameterParser();
        attributeParser.parse(templateCode);

        StringBuilder javaCode = new StringBuilder("package " + templatePackageName + ";\n");
        javaCode.append("public final class ").append(name).append(" implements org.jusecase.jte.internal.Template<").append(attributeParser.className).append("> {\n");
        javaCode.append("\tpublic void render(").append(attributeParser.className).append(" ").append(attributeParser.instanceName).append(", org.jusecase.jte.TemplateOutput output) {\n");

        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        new TemplateParser().parse(attributeParser.lastIndex, templateCode, new CodeGenerator(javaCode, classDefinitions));
        javaCode.append("\t}\n");
        javaCode.append("}\n");

        ClassDefinition templateDefinition = new ClassDefinition(templatePackagePrefix + name);
        templateDefinition.setCode(javaCode.toString());
        classDefinitions.add(templateDefinition);

        System.out.println(templateDefinition.getCode());

        try {
            ClassCompiler classCompiler = new ClassCompiler();
            return (Template<?>) classCompiler.compile(templateDefinition.getName(), classDefinitions).getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void compileTag(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        ClassDefinition classDefinition = new ClassDefinition(tagPackagePrefix + name);
        if (classDefinitions.contains(classDefinition)) {
            return;
        }

        String tagCode = codeResolver.resolveTag(name);
        if (tagCode == null) {
            throw new RuntimeException("No code found for tag " + name);
        }

        classDefinitions.add(classDefinition);

        StringBuilder javaCode = new StringBuilder("package " + tagPackageName + ";\n");
        javaCode.append("public final class ").append(name).append(" {\n");
        javaCode.append("\tpublic static void render(org.jusecase.jte.TemplateOutput output");
        ParameterParser parameterParser = new ParameterParser();
        int lastIndex = parameterParser.parse(tagCode, parameter -> javaCode.append(", ").append(parameter));
        javaCode.append(") {\n");

        new TemplateParser().parse(lastIndex, tagCode, new CodeGenerator(javaCode, classDefinitions));

        javaCode.append("\t}\n");
        javaCode.append("}\n");

        classDefinition.setCode(javaCode.toString());

        System.out.println(classDefinition.getCode());
    }


    private class CodeGenerator implements TemplateParserVisitor {
        private final LinkedHashSet<ClassDefinition> classDefinitions;
        private final StringBuilder javaCode;

        private CodeGenerator(StringBuilder javaCode, LinkedHashSet<ClassDefinition> classDefinitions) {
            this.javaCode = javaCode;
            this.classDefinitions = classDefinitions;
        }

        @Override
        public void onTextPart(int depth, String textPart) {
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
            compileTag(name, classDefinitions);

            writeIndentation(depth);
            javaCode.append(tagPackagePrefix).append(name).append(".render(output");

            if (!params.isBlank()) {
                javaCode.append(", ").append(params);
            }
            javaCode.append(");\n");
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
                if (c == '\n') {
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
}
