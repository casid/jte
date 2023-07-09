package gg.jte.compiler.java;

import gg.jte.ContentType;
import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.compiler.*;
import gg.jte.compiler.CodeBuilder.CodeMarker;
import gg.jte.runtime.ClassInfo;
import gg.jte.runtime.Constants;
import gg.jte.runtime.DebugInfo;
import gg.jte.compiler.TemplateType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static gg.jte.runtime.Constants.TEXT_PART_BINARY;

public class JavaCodeGenerator implements CodeGenerator {
    private final TemplateCompiler compiler;
    private final TemplateConfig config;
    private final ConcurrentHashMap<String, List<ParamInfo>> paramOrder;
    private final ClassInfo classInfo;
    private final CodeBuilder javaCode = new CodeBuilder(CodeType.Java);
    private final LinkedHashSet<ClassDefinition> classDefinitions;
    private final LinkedHashSet<TemplateDependency> templateDependencies;
    private final List<ParamInfo> parameters = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();
    private final List<byte[]> binaryTextParts = new ArrayList<>();
    private final Deque<ForLoopStart> forLoopStack = new ArrayDeque<>();

    private boolean hasWrittenPackage;
    private boolean hasWrittenClass;
    private CodeMarker fieldsMarker;

    public JavaCodeGenerator(TemplateCompiler compiler, TemplateConfig config, ConcurrentHashMap<String, List<ParamInfo>> paramOrder, ClassInfo classInfo, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<TemplateDependency> templateDependencies) {
        this.compiler = compiler;
        this.config = config;
        this.paramOrder = paramOrder;
        this.classInfo = classInfo;
        this.classDefinitions = classDefinitions;
        this.templateDependencies = templateDependencies;
    }

    @Override
    public void onImport(String importClass) {
        writePackageIfRequired();
        imports.add(importClass);
        javaCode.append("import ").append(importClass).append(";\n");
    }

    private void writePackageIfRequired() {
        if (!hasWrittenPackage) {
            javaCode.append("package " + classInfo.packageName + ";\n");
            hasWrittenPackage = true;
        }
    }

    @Override
    public void onParam(String parameter) {
        ParamInfo paramInfo = JavaParamInfo.parse(parameter, this, getCurrentTemplateLine());

        writePackageIfRequired();
        if (!hasWrittenClass) {
            writeClass();
        }

        javaCode.append(", ").append(paramInfo.type).append(' ').append(paramInfo.name);

        parameters.add(paramInfo);
    }

    private void writeClass() {
        javaCode.append("public final class ").append(classInfo.className).append(" {\n");
        fieldsMarker = javaCode.getMarkerOfCurrentPosition();
        javaCode.append("\tpublic static void render(");
        writeTemplateOutputParam();
        javaCode.append(", gg.jte.html.HtmlInterceptor jteHtmlInterceptor");

        hasWrittenClass = true;
    }

    private String getContentClass() {
        if (config.contentType == ContentType.Html) {
            return "gg.jte.html.HtmlContent";
        } else {
            return "gg.jte.Content";
        }
    }

    private void writeTemplateOutputParam() {
        if (config.contentType == ContentType.Html) {
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
        // Line information must be updated before insert, otherwise the line info field is not up-to-date
        int lineCount = 2;
        if (!binaryTextParts.isEmpty()) {
            lineCount += binaryTextParts.size() + 1;
        }
        javaCode.fillLines(fieldsMarker, lineCount);

        StringBuilder fields = new StringBuilder();
        addNameField(fields, classInfo.name);
        addLineInfoField(fields);
        writeBinaryTextParts(fields);

        javaCode.insert(fieldsMarker, fields, false);

        javaCode.append("\t}\n");

        javaCode.append("\tpublic static void renderMap(");
        writeTemplateOutputParam();
        javaCode.append(", gg.jte.html.HtmlInterceptor jteHtmlInterceptor");

        javaCode.append(", java.util.Map<String, Object> params) {\n");
        for (ParamInfo parameter : parameters) {
            if (parameter.varargs) {
                continue;
            }

            javaCode.setCurrentTemplateLine(parameter.templateLine);
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

    private void addLineInfoField(StringBuilder fields) {
        fields.append("\tpublic static final int[] ").append(Constants.LINE_INFO_FIELD).append(" = {");
        for (int i = 0; i < javaCode.getCurrentCodeLine(); ++i) {
            if (i > 0) {
                fields.append(',');
            }
            fields.append(javaCode.getLineInfo(i));
        }
        fields.append("};\n");
    }

    private void addNameField(StringBuilder fields, String name) {
        fields.append("\tpublic static final String ").append(Constants.NAME_FIELD).append(" = \"");
        fields.append(name);
        fields.append("\";\n");
    }

    private void writeBinaryTextParts(StringBuilder fields) {
        if (binaryTextParts.isEmpty()) {
            return;
        }

        writeBinaryTextPartsContent(fields);
        writeBinaryTextPartsConstants(fields);
    }

    private void writeBinaryTextPartsContent(StringBuilder fields) {
        String contentFileName = new ClassDefinition(classInfo.className, "java").getBinaryTextPartsFileName();

        fields.append("\tprivate static final gg.jte.runtime.BinaryContent BINARY_CONTENT = gg.jte.runtime.BinaryContent.load(")
                .append(classInfo.className)
                .append(".class, \"")
                .append(contentFileName)
                .append("\", ");

        for (int i = 0; i < binaryTextParts.size(); ++i) {
            if (i > 0) {
                fields.append(',');
            }
            fields.append(binaryTextParts.get(i).length);
        }

        fields.append(");\n");
    }

    private void writeBinaryTextPartsConstants(StringBuilder fields) {
        for (int i = 0; i < binaryTextParts.size(); ++i) {
            fields.append("\tprivate static final byte[] ").append(TEXT_PART_BINARY).append(i).append(" = BINARY_CONTENT.get(").append(i).append(");\n");
        }
    }

    @Override
    public void onError( String message ) {
        DebugInfo debugInfo = getCurrentDebugInfo();
        throw new TemplateException("Failed to compile " + debugInfo.name + ", error at line " + debugInfo.line + ": " + message);
    }

    @Override
    public void onError(String message, int templateLine) {
        DebugInfo debugInfo = getDebugInfo(templateLine);
        throw new TemplateException("Failed to compile " + debugInfo.name + ", error at line " + debugInfo.line + ": " + message);
    }

    @Override
    public void onTextPart(int depth, String textPart) {
        if (textPart.isEmpty()) {
            return;
        }

        if (config.binaryStaticContent) {
            writeTextBinary(depth, textPart);
        } else {
            writeTextString(depth, textPart);
        }
    }

    private void writeTextBinary(int depth, String textPart) {
        writeIndentation(depth);

        javaCode.append("jteOutput.writeBinaryContent(");
        javaCode.append(TEXT_PART_BINARY).append(binaryTextParts.size());
        javaCode.append(");\n");

        byte[] bytes = textPart.getBytes(StandardCharsets.UTF_8);
        binaryTextParts.add(bytes);
    }

    private void writeTextString(int depth, String textPart) {
        final int length = textPart.length();
        if (length < 65535 / 6) {
            // Optimization for strings that definitely fit into a single string literal
            writeText(depth, textPart);
        } else {
            int modifiedUtf8Length = 0;
            int chunkOffset = 0;

            for (int i = 0; i < length; i++) {
                int c = textPart.charAt(i);
                if (c >= 0x80 || c == 0) {
                    modifiedUtf8Length += c >= 0x800 ? 2 : 1;
                }

                // low surrogate: c >= 0xdc00 && c <= 0xdfff
                // high surrogate: c >= 0xd800 && c <= 0xdbff
                if (c >= 0xd800 && c <= 0xdbff) {
                    continue; // don't split low and high surrogates
                }

                if (modifiedUtf8Length + (i - chunkOffset + 1) > 65529) {
                    writeText(depth, textPart.substring(chunkOffset, i + 1));
                    modifiedUtf8Length = 0;
                    chunkOffset = i + 1;
                }
            }

            if (chunkOffset < length) {
                writeText(depth, textPart.substring(chunkOffset));
            }
        }
    }

    private void writeText(int depth, String text) {
        writeIndentation(depth);
        javaCode.append("jteOutput.writeContent(\"");
        javaCode.appendEscaped(text);
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

        writeIndentation(depth);
        javaCode.append("jteOutput.setContext(\"").append(tagName).append("\", null);\n");
    }

    @Override
    public void onUnsafeCodePart(int depth, String codePart) {
        writeIndentation(depth);
        javaCode.append("jteOutput.writeUnsafeContent(");
        javaCode.append(codePart);
        javaCode.append(");\n");
    }

    private void writeCodePart(int depth, String codePart) {
        writeIndentation(depth);

        javaCode.append("jteOutput.writeUserContent(");
        writeJavaCodeWithContentSupport(depth, codePart);
        javaCode.append(");\n");
    }

    @Override
    public void onCodeStatement(int depth, String codePart) {
        writeIndentation(depth);
        writeJavaCodeWithContentSupport(depth, codePart);
        javaCode.append("\n");
    }

    @Override
    public void onConditionStart(int depth, String condition) {
        writeIndentation(depth);

        javaCode.append("if (");
        javaCode.append(condition);
        javaCode.append(") {\n");
    }

    @Override
    public void onConditionElse(int depth, String condition) {
        writeIndentation(depth);
        javaCode.append("} else if (");
        javaCode.append(condition);
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
        CodeMarker beforeLoop = javaCode.getMarkerOfCurrentPosition();

        writeIndentation(depth);
        javaCode.append("for (").append(codePart).append(") {\n");

        CodeMarker inLoop = javaCode.getMarkerOfCurrentPosition();

        forLoopStack.push(new ForLoopStart(beforeLoop, inLoop, depth));
    }

    @Override
    public void onForLoopElse(int depth) {
        ForLoopStart forLoopStart = forLoopStack.peek();

        if (forLoopStart != null) {
            String variableName = "__jte_for_loop_entered_" + forLoopStack.size();

            StringBuilder variableDeclaration = new StringBuilder();
            writeIndentation(variableDeclaration, forLoopStart.indentation);
            variableDeclaration.append("boolean ").append(variableName).append(" = false;\n");
            javaCode.insert(forLoopStart.beforeLoop, variableDeclaration);

            StringBuilder variableAssignment = new StringBuilder();
            writeIndentation(variableAssignment, forLoopStart.indentation + 1);
            variableAssignment.append(variableName).append(" = true;\n");
            javaCode.insert(forLoopStart.inLoop, variableAssignment);

            writeIndentation(depth);
            javaCode.append("}\n");

            writeIndentation(depth);
            javaCode.append("if (!").append(variableName).append(") {\n");
        }
    }

    @Override
    public void onForLoopEnd(int depth) {
        forLoopStack.pop();

        writeIndentation(depth);
        javaCode.append("}\n");
    }

    @Override
    public void onTemplateCall(int depth, String name, List<String> params) {
        ClassInfo tagInfo = compiler.generateTemplateCall(name, "jte", classDefinitions, templateDependencies, getCurrentDebugInfo());

        writeIndentation(depth);

        javaCode.append(tagInfo.fullName).append(".render(jteOutput, jteHtmlInterceptor");

        appendParams(depth, tagInfo.name, params);
        javaCode.append(");\n");
    }

    @Override
    public void onInterceptHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag) {
        writeIndentation(depth);
        javaCode.append("jteHtmlInterceptor.onHtmlTagOpened(\"").append(htmlTag.name).append("\", ");
        writeAttributeMap(htmlTag);
        javaCode.append(", jteOutput);\n");
    }

    @Override
    public void onInterceptHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag) {
        writeIndentation(depth);
        javaCode.append("jteHtmlInterceptor.onHtmlTagClosed(\"").append(htmlTag.name).append("\", jteOutput);\n");
    }

    @Override
    public void onHtmlAttributeOutput(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
        String javaExpression = CodeGenerator.extractSingleOutputTemplateExpression(htmlAttribute.value);
        if (htmlAttribute.bool) {
            onConditionStart(depth, javaExpression);
            onTextPart(depth, " " + htmlAttribute.name);
        } else {
            onConditionStart(depth, "gg.jte.runtime.TemplateUtils.isAttributeRendered(" + javaExpression + ")");
            onTextPart(depth + 1, " " + htmlAttribute.name + "=" + htmlAttribute.quotes);
            onHtmlTagAttributeCodePart(depth + 1, javaExpression, currentHtmlTag.name, htmlAttribute.name);
            onTextPart(depth + 1, "" + htmlAttribute.quotes);
        }
        onConditionEnd(depth);
    }

    private void writeAttributeMap(TemplateParser.HtmlTag htmlTag) {
        CodeGenerator.writeAttributeMap(javaCode, htmlTag);
    }

    private void writeJavaCodeWithContentSupport(int depth, String code) {
        if (code.contains("@`")) {
            new JavaContentProcessor(depth, code).process();
        } else {
            javaCode.append(code);
        }
    }

    private DebugInfo getCurrentDebugInfo() {
        return getDebugInfo(getCurrentTemplateLine());
    }

    private DebugInfo getDebugInfo(int templateLine) {
        return new DebugInfo(classInfo.name, templateLine + 1);
    }

    @Override
    public int getCurrentTemplateLine() {
        return javaCode.getCurrentTemplateLine();
    }

    @Override
    public List<ParamInfo> getParamInfo() {
        return parameters;
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    private void appendParams(int depth, String name, List<String> params) {
        List<ParamInfo> paramInfos = paramOrder.get(name);
        if (paramInfos == null) {
            throw new IllegalStateException("No parameter information for " + name);
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

        throw new TemplateException("Failed to compile template, error at " + classInfo.name + ":" + getCurrentTemplateLine() + ". No parameter with name " + paramCallInfo.name + " is defined in " + name);
    }

    private void writeIndentation(int depth) {
        for (int i = 0; i < depth + 2; ++i) {
            javaCode.append('\t');
        }
    }

    @SuppressWarnings("StringRepeatCanBeUsed")
    private void writeIndentation(StringBuilder code, int depth) {
        for (int i = 0; i < depth + 2; ++i) {
            code.append('\t');
        }
    }

    @Override
    public String getCode() {
        return javaCode.getCode();
    }

    @Override
    public List<byte[]> getBinaryTextParts() {
        return binaryTextParts;
    }

    class JavaContentProcessor extends ContentProcessor {

        public JavaContentProcessor( int depth, String code ) {
            super(depth, code);
        }

        @Override
        protected void onContentBlock( int depth, String code, int lastWrittenIndex, int startIndex, int endIndex ) {
            javaCode.append(code, lastWrittenIndex + 1, startIndex - 2);

            javaCode.append("new ").append(getContentClass()).append("() {\n");

            writeIndentation(depth + 1);
            javaCode.append("public void writeTo(");
            writeTemplateOutputParam();
            javaCode.append(") {\n");

            TemplateParser parser = new TemplateParser(code, TemplateType.Content, JavaCodeGenerator.this, config);
            parser.setStartIndex(startIndex);
            parser.setEndIndex(endIndex);
            parser.setParamsComplete(true);
            parser.parse(depth + 2);

            writeIndentation(depth + 1);
            javaCode.append("}\n");

            writeIndentation(depth);
            javaCode.append("}");
        }

        @Override
        protected void onRemainingCode( String code, int startIndex, int endIndex ) {
            javaCode.append(code, startIndex, endIndex);
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

    private record ForLoopStart(CodeMarker beforeLoop, CodeMarker inLoop, int indentation) {}
}
