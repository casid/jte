package gg.jte.internal;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateException;
import gg.jte.html.HtmlPolicy;
import gg.jte.html.OwaspHtmlPolicy;
import gg.jte.output.FileOutput;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TemplateCompiler extends TemplateLoader {

    public static final boolean DEBUG = false;

    private final CodeResolver codeResolver;
    private final ContentType contentType;

    private final ConcurrentHashMap<String, LinkedHashSet<String>> templateDependencies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ParamInfo>> paramOrder = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClassInfo> templateByClassName = new ConcurrentHashMap<>();
    private boolean nullSafeTemplateCode;
    private HtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
    private String[] htmlTags;
    private String[] htmlAttributes;

    public TemplateCompiler(CodeResolver codeResolver, Path classDirectory, ContentType contentType) {
        super(classDirectory);
        this.codeResolver = codeResolver;
        this.contentType = contentType;
    }

    @Override
    public Template load(String name) {
        precompile(List.of(name), null);
        return super.load(name);
    }

    @Override
    protected ClassInfo getClassInfo(ClassLoader classLoader, String className) {
        return templateByClassName.get(className);
    }

    @Override
    protected ClassLoader getClassLoader() {
        return createClassLoader();
    }

    public void cleanAll() {
        IoUtils.deleteDirectoryContent(classDirectory);
    }

    public int precompileAll(List<String> compilePath) {
        return precompile(codeResolver.resolveAllTemplateNames(), compilePath);
    }

    public int precompile(List<String> names, List<String> compilePath) {
        LinkedHashSet<ClassDefinition> classDefinitions = new LinkedHashSet<>();
        for (String name : names) {
            switch (getTemplateType(name)) {
                case Template:
                    generateTemplate(name, classDefinitions);
                    break;
                case Tag:
                    generateTemplateFromTag(name, classDefinitions);
                    break;
                case Layout:
                    generateTemplateFromLayout(name, classDefinitions);
                    break;
            }
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

        return files.length;
    }

    private void generateTemplate(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        String code = resolveCode(name, null);

        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = new ClassInfo(name, Constants.PACKAGE_NAME);

        CodeGenerator codeGenerator = new CodeGenerator(templateInfo, classDefinitions, templateDependencies);
        new TemplateParser(code, TemplateType.Template, codeGenerator, contentType, htmlPolicy, htmlTags, htmlAttributes).parse();

        this.templateDependencies.put(name, templateDependencies);

        ClassDefinition templateDefinition = new ClassDefinition(templateInfo.fullName);
        templateDefinition.setCode(codeGenerator.getCode());
        classDefinitions.add(templateDefinition);

        templateByClassName.put(templateDefinition.getName(), templateInfo);

        if (DEBUG) {
            System.out.println(templateDefinition.getCode());
        }
    }

    private void generateTemplateFromTag(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = generateTagOrLayout(TemplateType.Tag, name, classDefinitions, templateDependencies, null);

        this.templateDependencies.put(name, templateDependencies);

        templateByClassName.put(templateInfo.name, templateInfo);
    }

    private void generateTemplateFromLayout(String name, LinkedHashSet<ClassDefinition> classDefinitions) {
        LinkedHashSet<String> templateDependencies = new LinkedHashSet<>();

        ClassInfo templateInfo = generateTagOrLayout(TemplateType.Layout, name, classDefinitions, templateDependencies, null);

        this.templateDependencies.put(name, templateDependencies);

        templateByClassName.put(templateInfo.name, templateInfo);
    }

    private ClassInfo generateTagOrLayout(TemplateType type, String name, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies, DebugInfo debugInfo) {
        templateDependencies.add(name);
        ClassInfo classInfo = new ClassInfo(name, Constants.PACKAGE_NAME);

        ClassDefinition classDefinition = new ClassDefinition(classInfo.fullName);
        if (classDefinitions.contains(classDefinition)) {
            return classInfo;
        }

        String code = resolveCode(name, debugInfo);

        classDefinitions.add(classDefinition);

        CodeGenerator codeGenerator = new CodeGenerator(classInfo, classDefinitions, templateDependencies);
        new TemplateParser(code, type, codeGenerator, contentType, htmlPolicy, htmlTags, htmlAttributes).parse();

        classDefinition.setCode(codeGenerator.getCode());
        templateByClassName.put(classDefinition.getName(), classInfo);

        if (DEBUG) {
            System.out.println(classDefinition.getCode());
        }

        return classInfo;
    }

    private String resolveCode(String name, DebugInfo debugInfo) {
        String code = codeResolver.resolve(name);
        if (code == null) {
            String message = name + " not found";
            if (debugInfo != null) {
                message += ", referenced at " + debugInfo.name + ":" + debugInfo.line;
            }
            throw new TemplateException(message);
        }
        return code;
    }

    @Override
    public boolean hasChanged(String name) {
        if (codeResolver.hasChanged(name)) {
            return true;
        }

        LinkedHashSet<String> dependencies = templateDependencies.get(name);
        if (dependencies == null) {
            return false;
        }

        for (String dependency : dependencies) {
            if (codeResolver.hasChanged(dependency)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> getTemplatesUsing(String name) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> dependencies : templateDependencies.entrySet()) {
            if (dependencies.getValue().contains(name)) {
                result.add(dependencies.getKey());
            }
        }

        return result;
    }

    @Override
    public void setNullSafeTemplateCode(boolean nullSafeTemplateCode) {
        this.nullSafeTemplateCode = nullSafeTemplateCode;
    }

    @Override
    public void setHtmlPolicy(HtmlPolicy htmlPolicy) {
        this.htmlPolicy = htmlPolicy;
    }

    @Override
    public void setHtmlTags(String[] htmlTags) {
        this.htmlTags = htmlTags;
    }

    @Override
    public void setHtmlAttributes(String[] htmlAttributes) {
        this.htmlAttributes = htmlAttributes;
    }

    private class CodeGenerator implements TemplateParserVisitor {
        private final ClassInfo classInfo;
        private final CodeBuilder javaCode = new CodeBuilder();
        private final LinkedHashSet<ClassDefinition> classDefinitions;
        private final LinkedHashSet<String> templateDependencies;
        private final List<ParamInfo> parameters = new ArrayList<>();

        private boolean hasWrittenPackage;
        private boolean hasWrittenClass;

        private CodeGenerator(ClassInfo classInfo, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
            this.classInfo = classInfo;
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
                writeClass();
            }

            javaCode.append(", ").append(parameter.type).append(' ').append(parameter.name);

            parameters.add(parameter);
        }

        private void writeClass() {
            javaCode.append("public final class ").append(classInfo.className).append(" {\n");
            javaCode.markFieldsIndex();
            javaCode.append("\tpublic static void render(");
            writeTemplateOutputParam();
            javaCode.append(", gg.jte.html.HtmlInterceptor jteHtmlInterceptor");

            hasWrittenClass = true;
        }

        private String getContentClass() {
            if (contentType == ContentType.Html) {
                return "gg.jte.html.HtmlContent";
            } else {
                return "gg.jte.Content";
            }
        }

        private void writeTemplateOutputParam() {
            if (contentType == ContentType.Html) {
                javaCode.append("gg.jte.html.HtmlTemplateOutput jteOutput");
            } else {
                javaCode.append("gg.jte.TemplateOutput jteOutput");
            }
        }

        @Override
        public void onParamsComplete() {
            writePackageIfRequired();
            if (!hasWrittenClass) {
                writeClass();
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
            int lineCount = 2;
            javaCode.insertFieldLines(lineCount);

            StringBuilder fields = new StringBuilder(64 + 32 * lineCount);
            javaCode.addNameField(fields, classInfo.name);
            javaCode.addLineInfoField(fields);
            javaCode.insertFields(fields);

            javaCode.append("\t}\n");

            javaCode.append("\tpublic static void renderMap(");
            writeTemplateOutputParam();
            javaCode.append(", gg.jte.html.HtmlInterceptor jteHtmlInterceptor");

            javaCode.append(", java.util.Map<String, Object> params) {\n");
            for (ParamInfo parameter : parameters) {
                if (parameter.varargs) {
                    continue;
                }

                javaCode.append("\t\t").append(parameter.type).append(" ").append(parameter.name).append(" = (").append(parameter.type);
                if (parameter.defaultValue != null) {
                    javaCode.append(")params.getOrDefault(\"").append(parameter.name).append("\", ");
                    writeJavaCodeWithContentSupport(0, parameter.defaultValue);
                    javaCode.append(");\n");
                } else {
                    javaCode.append(")params.get(\"").append(parameter.name).append("\");\n");
                }
            }
            javaCode.append("\t\trender(jteOutput, jteHtmlInterceptor");

            for (ParamInfo parameter : parameters) {
                if (parameter.varargs) {
                    continue;
                }

                javaCode.append(", ").append(parameter.name);
            }
            javaCode.append(");\n");
            javaCode.append("\t}\n");

            javaCode.append("}\n");

            this.classInfo.lineInfo = javaCode.getLineInfo();
        }

        @Override
        public void onError( String message ) {
            DebugInfo debugInfo = getCurrentDebugInfo();
            throw new TemplateException("Failed to compile " + debugInfo.name + ", error at line " + debugInfo.line + ": " + message);
        }

        @Override
        public void onTextPart(int depth, String textPart) {
            if (textPart.isEmpty()) {
                return;
            }

            writeIndentation(depth);
            javaCode.append("jteOutput.writeContent(\"");
            appendEscaped(javaCode.getStringBuilder(), textPart);
            javaCode.append("\");\n");
        }

        @Override
        public void onCodePart(int depth, String codePart) {
            writeCodePart(depth, codePart);
        }

        @Override
        public void onHtmlTagBodyCodePart(int depth, String codePart, String tagName) {
            writeIndentation(depth);
            javaCode.append("jteOutput.setContext(\"").append(tagName).append("\", null);\n");

            writeCodePart(depth, codePart);
        }

        @Override
        public void onHtmlTagAttributeCodePart(int depth, String codePart, String tagName, String attributeName) {
            writeIndentation(depth);
            javaCode.append("jteOutput.setContext(\"").append(tagName).append("\", \"").append(attributeName).append("\");\n");

            writeCodePart(depth, codePart);
        }

        @Override
        public void onUnsafeCodePart(int depth, String codePart) {
            if (contentType == ContentType.Html) {
                writeIndentation(depth);
                javaCode.append("jteOutput.setContext(null, null);\n");
            }

            writeCodePart(depth, codePart);
        }

        private void writeCodePart(int depth, String codePart) {
            writeIndentation(depth);
            if (nullSafeTemplateCode) {
                javaCode.append("try {\n");
                writeIndentation(depth + 1);
            }
            javaCode.append("jteOutput.writeUserContent(");
            writeJavaCodeWithContentSupport(depth, codePart);
            javaCode.append(");\n");
            if (nullSafeTemplateCode) {
                writeIndentation(depth);
                javaCode.append("} catch (NullPointerException jteOutputNpe) {\n");
                writeIndentation(depth + 1);
                javaCode.append("gg.jte.internal.NullCheck.handleNullOutput(jteOutputNpe);\n");
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
                    javaCode.append("gg.jte.internal.NullCheck.evaluate(() -> ");
                    javaCode.append(codePart);
                    javaCode.append(")");
                } else {
                    javaCode.append(codePart, 0, index + 1);
                    javaCode.append(" gg.jte.internal.NullCheck.evaluate(() -> ");
                    javaCode.append(codePart, index + 2, codePart.length());
                    javaCode.append(")");
                }
            } else {
                writeJavaCodeWithContentSupport(depth, codePart);
            }
            javaCode.append(";\n");
        }

        @Override
        public void onConditionStart(int depth, String condition) {
            writeIndentation(depth);
            javaCode.append("if (");

            if (nullSafeTemplateCode) {
                javaCode.append("gg.jte.internal.NullCheck.evaluate(() -> ").append(condition).append(")");
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
                javaCode.append("gg.jte.internal.NullCheck.evaluate(() -> ").append(condition).append(")");
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
        public void onTag(int depth, TemplateType type, String name, List<String> params) {
            String directory = type == TemplateType.Layout ? Constants.LAYOUT_DIRECTORY : Constants.TAG_DIRECTORY;
            String tagName = directory + name.replace('.', '/') + Constants.TAG_EXTENSION;
            ClassInfo tagInfo = generateTagOrLayout(type, tagName, classDefinitions, templateDependencies, getCurrentDebugInfo());

            writeIndentation(depth);

            javaCode.append(tagInfo.fullName).append(".render(jteOutput, jteHtmlInterceptor");

            appendParams(depth, tagName, params);
            javaCode.append(");\n");
        }

        @Override
        public void onHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag) {
            writeIndentation(depth);
            javaCode.append("jteHtmlInterceptor.onHtmlTagOpened(\"").append(htmlTag.name).append("\", ");
            writeAttributeMap(htmlTag);
            javaCode.append(", jteOutput);\n");
        }

        @Override
        public void onHtmlAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
            writeIndentation(depth);
            javaCode.append("jteHtmlInterceptor.onHtmlAttributeStarted(\"").append(htmlAttribute.name).append("\", ");
            writeAttributeMap(currentHtmlTag);
            javaCode.append(", jteOutput);\n");
        }

        @Override
        public void onHtmlBooleanAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
            String javaExpression = extractJavaExpression(htmlAttribute.value);
            if (javaExpression == null) {
                onTextPart(depth, htmlAttribute.name);
            } else {
                onConditionStart(depth, javaExpression);
                onCodePart(depth + 1, "\"" + htmlAttribute.name + "\"");
                onConditionEnd(depth);
            }
        }

        @Override
        public void onHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag) {
            writeIndentation(depth);
            javaCode.append("jteHtmlInterceptor.onHtmlTagClosed(\"").append(htmlTag.name).append("\", jteOutput);\n");
        }

        private void writeAttributeMap(TemplateParser.HtmlTag htmlTag) {
            javaCode.append("gg.jte.internal.TemplateUtils.toMap(");
            boolean firstWritten = false;
            for (TemplateParser.HtmlAttribute attribute : htmlTag.attributes) {
                if (attribute.value == null) {
                    continue;
                }

                if (firstWritten) {
                    javaCode.append(",");
                } else {
                    firstWritten = true;
                }
                javaCode.append("\"").append(attribute.name).append("\",");
                String javaExpression = extractJavaExpression(attribute.value);
                if (javaExpression != null) {
                    javaCode.append(javaExpression);
                } else {
                    javaCode.append("\"").append(attribute.value).append("\"");
                }
            }
            javaCode.append(")");
        }

        private void writeJavaCodeWithContentSupport(int depth, String code) {
            if (code.contains("@`")) {
                new ContentProcessor(depth, code).process();
            } else {
                javaCode.append(code);
            }
        }

        private String extractJavaExpression(String value) {
            int startIndex = value.indexOf("${");
            if (startIndex == -1) {
                return null;
            }

            int endIndex = value.lastIndexOf('}');
            if (endIndex == -1) {
                return null;
            }

            return value.substring(startIndex + 2, endIndex);
        }

        private DebugInfo getCurrentDebugInfo() {
            return new DebugInfo(classInfo.name, javaCode.getCurrentTemplateLine() + 1);
        }

        private void appendParams(int depth, String name, List<String> params) {
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
                    appendParam(depth, paramCallInfo.data);
                } else {
                    ParamInfo paramInfo = paramInfos.get(i);
                    if (paramInfo.defaultValue != null) {
                        appendParam(depth, paramInfo.defaultValue);
                    }
                }
            }
        }

        private void appendParam(int depth, String param) {
            javaCode.append(", ");
            writeJavaCodeWithContentSupport(depth, param);
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

        class ContentProcessor {
            private final int depth;
            private final String param;

            private int startIndex = -1;
            private int endIndex = -1;
            private int lastWrittenIndex = -1;
            private int nestedCount;

            @SuppressWarnings("FieldCanBeLocal")
            private char previousChar0;
            private char currentChar;

            ContentProcessor(int depth, String param) {
                this.depth = depth;
                this.param = param;
            }

            public void process() {
                for (int i = 0; i < param.length(); ++i) {
                    previousChar0 = currentChar;
                    currentChar = param.charAt(i);

                    if (previousChar0 == '@' && currentChar == '`') {
                        if (startIndex == -1) {
                            startIndex = i + 1;
                        } else {
                            ++nestedCount;
                        }
                    } else if (currentChar == '`') {
                        if (nestedCount == 0) {
                            endIndex = i;
                            writeJavaCode();
                        } else {
                            --nestedCount;
                        }
                    }
                }

                if (lastWrittenIndex + 1 < param.length()) {
                    javaCode.append(param, lastWrittenIndex + 1, param.length());
                }
            }

            private void writeJavaCode() {
                javaCode.append(param, lastWrittenIndex + 1, startIndex - 2);

                javaCode.append("new ").append(getContentClass()).append("() {\n");

                writeIndentation(depth + 1);
                javaCode.append("public void writeTo(");
                writeTemplateOutputParam();
                javaCode.append(") {\n");

                TemplateParser parser = new TemplateParser(param, TemplateType.Content, CodeGenerator.this, contentType, htmlPolicy, htmlTags, htmlAttributes);
                parser.setStartIndex(startIndex);
                parser.setEndIndex(endIndex);
                parser.setParamsComplete(true);
                parser.parse(depth + 2);

                writeIndentation(depth + 1);
                javaCode.append("}\n");

                writeIndentation(depth);
                javaCode.append("}");

                lastWrittenIndex = endIndex;

                startIndex = -1;
                endIndex = -1;
            }
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
