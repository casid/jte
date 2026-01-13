package gg.jte.compiler.kotlin;

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

@SuppressWarnings("unused") // Used by TemplateCompiler
public class KotlinCodeGenerator implements CodeGenerator {
    private final TemplateCompiler compiler;
    private final TemplateConfig config;
    private final ConcurrentHashMap<String, List<ParamInfo>> paramOrder;
    private final ClassInfo classInfo;
    private final CodeBuilder kotlinCode = new CodeBuilder(CodeType.Kotlin);
    private final LinkedHashSet<ClassDefinition> classDefinitions;
    private final LinkedHashSet<TemplateDependency> templateDependencies;
    private final List<ParamInfo> parameters = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();
    private final List<byte[]> binaryTextParts = new ArrayList<>();
    private final Deque<ForLoopStart> forLoopStack = new ArrayDeque<>();

    private boolean hasWrittenPackage;
    private boolean hasWrittenClass;
    private CodeMarker fieldsMarker;
    private int attributeCounter;
    private int nextForLoopId = 1;

    public KotlinCodeGenerator(TemplateCompiler compiler, TemplateConfig config, ConcurrentHashMap<String, List<ParamInfo>> paramOrder, ClassInfo classInfo, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<TemplateDependency> templateDependencies) {
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
        kotlinCode.append("import ").append(importClass).append("\n");
    }

    private void writePackageIfRequired() {
        if (!hasWrittenPackage) {
            kotlinCode.append("@file:Suppress(\"ktlint\")\n");
            kotlinCode.append("package " + classInfo.packageName + "\n");
            hasWrittenPackage = true;
        }
    }

    @Override
    public void onParam(String parameter) {
        ParamInfo paramInfo = KotlinParamInfo.parse(parameter, this, getCurrentTemplateLine());

        writePackageIfRequired();
        if (!hasWrittenClass) {
            writeClass();
        }

        kotlinCode.append(", ");
        if (paramInfo.varargs) {
            kotlinCode.append("vararg ");
        }
        kotlinCode.append(paramInfo.name).append(':').append(paramInfo.type);

        // If there is a default value that IS NOT a gg.jte.Content, then add it to the
        // method definition. gg.jte.Content are excluded because the syntax is not supported
        // by Kotlin.
        if (paramInfo.defaultValue != null && !paramInfo.defaultValue.startsWith("@`")) {
            kotlinCode.append(" = ").append(paramInfo.defaultValue);
        }

        parameters.add(paramInfo);
    }

    private void writeClass() {
        kotlinCode.append("@Suppress(\"UNCHECKED_CAST\", \"UNUSED_PARAMETER\")").append('\n');
        kotlinCode.append("internal class ").append(classInfo.className).append(" {\n");
        kotlinCode.append("companion object {\n");
        fieldsMarker = kotlinCode.getMarkerOfCurrentPosition();
        kotlinCode.append("\t@JvmStatic fun render(");
        writeTemplateOutputParam();
        kotlinCode.append(", jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?");

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
            kotlinCode.append("jteOutput:gg.jte.html.HtmlTemplateOutput");
        } else {
            kotlinCode.append("jteOutput:gg.jte.TemplateOutput");
        }
    }

    @Override
    public void onParamsComplete() {
        writePackageIfRequired();
        if (!hasWrittenClass) {
            writeClass();
        }

        kotlinCode.append(") {\n");

        paramOrder.put(classInfo.name, parameters);
    }

    @Override
    public void onLineFinished() {
        kotlinCode.finishTemplateLine();
    }

    @Override
    public void onComplete() {
        kotlinCode.append("\t}\n");

        kotlinCode.append("\t@JvmStatic fun renderMap(");
        writeTemplateOutputParam();
        kotlinCode.append(", jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?");

        kotlinCode.append(", params:Map<String, Any?>) {\n");
        for (ParamInfo parameter : parameters) {
            if (parameter.varargs) {
                continue;
            }

            writeParameterDeclaration(parameter);
        }
        kotlinCode.append("\t\trender(jteOutput, jteHtmlInterceptor");

        for (ParamInfo parameter : parameters) {
            if (parameter.varargs) {
                continue;
            }

            kotlinCode.append(", ").append(parameter.name);
        }
        kotlinCode.append(");\n");
        kotlinCode.append("\t}\n");

        kotlinCode.append("}\n");
        kotlinCode.append("}\n");

        // Line information must be updated before insert, otherwise the line info field is not up-to-date
        int lineCount = 2;
        if (!binaryTextParts.isEmpty()) {
            lineCount += binaryTextParts.size() + 1;
        }
        kotlinCode.fillLines(fieldsMarker, lineCount);

        StringBuilder fields = new StringBuilder();
        addNameField(fields, classInfo.name);
        addLineInfoField(fields);
        writeBinaryTextParts(fields);

        kotlinCode.insert(fieldsMarker, fields, false);

        this.classInfo.lineInfo = kotlinCode.getLineInfo();
    }

    private void writeParameterDeclaration(ParamInfo parameter) {
        var nonNullDefaultValue = parameter.defaultValue != null && !parameter.defaultValue.equals("null");

        kotlinCode.setCurrentTemplateLine(parameter.templateLine);
        kotlinCode.append("\t\tval ").append(parameter.name).append(" = params[\"").append(parameter.name).append("\"] as ");
        if (nonNullDefaultValue) {
            kotlinCode.append(asNullableType(parameter.type));
            kotlinCode.append(" ?: ");
            writeCodeWithContentSupport(0, parameter.defaultValue);
        } else {
            kotlinCode.append(parameter.type);
        }
        kotlinCode.append('\n');
    }

    private String asNullableType(String type) {
        if (type.endsWith("?")) {
            return type;
        } else {
            return type + "?";
        }
    }

    private void addNameField(StringBuilder fields, String name) {
        fields.append("\t@JvmField val ").append(Constants.NAME_FIELD).append(" = \"");
        fields.append(name);
        fields.append("\"\n");
    }

    private void addLineInfoField(StringBuilder fields) {
        fields.append("\t@JvmField val ").append(Constants.LINE_INFO_FIELD).append(" = intArrayOf(");
        for (int i = 0; i < kotlinCode.getCurrentCodeLine(); ++i) {
            if (i > 0) {
                fields.append(',');
            }
            fields.append(kotlinCode.getLineInfo(i));
        }
        fields.append(")\n");
    }

    private void writeBinaryTextParts(StringBuilder fields) {
        if (binaryTextParts.isEmpty()) {
            return;
        }

        writeBinaryTextPartsContent(fields);
        writeBinaryTextPartsConstants(fields);
    }

    private void writeBinaryTextPartsContent(StringBuilder fields) {
        String contentFileName = new ClassDefinition(classInfo.className, "kt").getBinaryTextPartsFileName();

        fields.append("\t@JvmStatic val BINARY_CONTENT = gg.jte.runtime.BinaryContent.load(")
                .append(classInfo.className)
                .append("::class.java, \"")
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
            fields.append("\t@JvmStatic val ").append(TEXT_PART_BINARY).append(i).append(" = BINARY_CONTENT.get(").append(i).append(")\n");
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

        kotlinCode.append("jteOutput.writeBinaryContent(");
        kotlinCode.append(TEXT_PART_BINARY).append(binaryTextParts.size());
        kotlinCode.append(")\n");

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
        kotlinCode.append("jteOutput.writeContent(\"");
        kotlinCode.appendEscaped(text);
        kotlinCode.append("\")\n");
    }

    @Override
    public void onCodePart(int depth, String codePart) {
        writeCodePart(depth, codePart);
    }

    @Override
    public void onHtmlTagBodyCodePart(int depth, String codePart, String tagName) {
        writeIndentation(depth);
        kotlinCode.append("jteOutput.setContext(\"").append(tagName).append("\", null)\n");

        writeCodePart(depth, codePart);
    }

    @Override
    public void onHtmlTagAttributeCodePart(int depth, String codePart, String tagName, String attributeName) {
        writeIndentation(depth);
        kotlinCode.append("jteOutput.setContext(\"").append(tagName).append("\", \"").appendEscaped(attributeName).append("\")\n");

        writeCodePart(depth, codePart);

        writeIndentation(depth);
        kotlinCode.append("jteOutput.setContext(\"").append(tagName).append("\", null)\n");
    }

    @Override
    public void onUnsafeCodePart(int depth, String codePart) {
        writeIndentation(depth);
        kotlinCode.append("jteOutput.writeUnsafeContent(");
        kotlinCode.append(codePart);
        kotlinCode.append(")\n");
    }

    private void writeCodePart(int depth, String codePart) {
        writeIndentation(depth);

        kotlinCode.append("jteOutput.writeUserContent(");
        writeCodeWithContentSupport(depth, codePart);
        kotlinCode.append(")\n");
    }

    @Override
    public void onCodeStatement(int depth, String codePart) {
        writeIndentation(depth);
        writeCodeWithContentSupport(depth, codePart);
        kotlinCode.append("\n");
    }

    @Override
    public void onConditionStart(int depth, String condition) {
        writeIndentation(depth);

        kotlinCode.append("if (");
        kotlinCode.append(condition);
        kotlinCode.append(") {\n");
    }

    @Override
    public void onConditionElse(int depth, String condition) {
        writeIndentation(depth);
        kotlinCode.append("} else if (");
        kotlinCode.append(condition);
        kotlinCode.append(") {\n");
    }

    @Override
    public void onConditionElse(int depth) {
        writeIndentation(depth);
        kotlinCode.append("} else {\n");
    }

    @Override
    public void onConditionEnd(int depth) {
        writeIndentation(depth);
        kotlinCode.append("}\n");
    }

    @Override
    public void onForLoopStart(int depth, String codePart) {
        CodeMarker beforeLoop = kotlinCode.getMarkerOfCurrentPosition();

        writeIndentation(depth);
        kotlinCode.append("for (").append(codePart).append(") {\n");

        CodeMarker inLoop = kotlinCode.getMarkerOfCurrentPosition();

        forLoopStack.push(new ForLoopStart(beforeLoop, inLoop, depth, nextForLoopId++));
    }

    @Override
    public void onForLoopElse(int depth) {
        ForLoopStart forLoopStart = forLoopStack.peek();

        if (forLoopStart != null) {
            String variableName = "__jte_for_loop_entered_" + forLoopStart.id;

            StringBuilder variableDeclaration = new StringBuilder();
            writeIndentation(variableDeclaration, forLoopStart.indentation);
            variableDeclaration.append("var ").append(variableName).append(" = false\n");
            kotlinCode.insert(forLoopStart.beforeLoop, variableDeclaration);

            StringBuilder variableAssignment = new StringBuilder();
            writeIndentation(variableAssignment, forLoopStart.indentation + 1);
            variableAssignment.append(variableName).append(" = true\n");
            kotlinCode.insert(forLoopStart.inLoop, variableAssignment);

            writeIndentation(depth);
            kotlinCode.append("}\n");

            writeIndentation(depth);
            kotlinCode.append("if (!").append(variableName).append(") {\n");
        }
    }

    @Override
    public void onForLoopEnd(int depth) {
        forLoopStack.pop();

        writeIndentation(depth);
        kotlinCode.append("}\n");
    }

    @Override
    public void onTemplateCall(int depth, String name, List<String> params) {
        ClassInfo tagInfo = compiler.generateTemplateCall(name, "kte", classDefinitions, templateDependencies, getCurrentDebugInfo());

        writeIndentation(depth);

        kotlinCode.append(tagInfo.fullName).append(".render(jteOutput, jteHtmlInterceptor");

        appendParams(depth, tagInfo.name, params);
        kotlinCode.append(");\n");
    }

    @Override
    public void onInterceptHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag) {
        writeIndentation(depth);
        kotlinCode.append("jteHtmlInterceptor?.onHtmlTagOpened(\"").append(htmlTag.name).append("\", ");
        writeAttributeMap(htmlTag);
        kotlinCode.append(", jteOutput)\n");
    }

    @Override
    public void onInterceptHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag) {
        writeIndentation(depth);
        kotlinCode.append("jteHtmlInterceptor?.onHtmlTagClosed(\"").append(htmlTag.name).append("\", jteOutput)\n");
    }

    @Override
    public void onHtmlAttributeOutput(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
        String variableName = assignAttributeToVariable(depth, htmlAttribute);
        if (htmlAttribute.bool) {
            onConditionStart(depth, variableName);
            onTextPart(depth, " " + htmlAttribute.name);
        } else {
            onConditionStart(depth, "gg.jte.runtime.TemplateUtils.isAttributeRendered(" + variableName + ")");
            onTextPart(depth + 1, " " + htmlAttribute.name + "=" + htmlAttribute.quotes);
            onHtmlTagAttributeCodePart(depth + 1, variableName, currentHtmlTag.name, htmlAttribute.name);
            onTextPart(depth + 1, "" + htmlAttribute.quotes);
        }
        onConditionEnd(depth);
    }

    private String assignAttributeToVariable(int depth, TemplateParser.HtmlAttribute htmlAttribute) {
        String variableName = "__jte_html_attribute_" + attributeCounter;
        String variableValue = CodeGenerator.extractSingleOutputTemplateExpression(htmlAttribute.value);

        ++attributeCounter;

        htmlAttribute.variableName = variableName;

        writeIndentation(depth);

        kotlinCode.append("val ").append(variableName).append(" = ").append(variableValue).append("\n");

        return variableName;
    }

    private void writeAttributeMap(TemplateParser.HtmlTag htmlTag) {
        CodeGenerator.writeAttributeMap(kotlinCode, htmlTag);
    }

    private void writeCodeWithContentSupport(int depth, String code) {
        if (code.contains("@`")) {
            new KotlinContentProcessor(depth, code).process();
        } else {
            kotlinCode.append(code);
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
        return kotlinCode.getCurrentTemplateLine();
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
        kotlinCode.append(", ");
        writeCodeWithContentSupport(depth, param);
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
            kotlinCode.append('\t');
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
        return kotlinCode.getCode();
    }

    @Override
    public List<byte[]> getBinaryTextParts() {
        return binaryTextParts;
    }

    class KotlinContentProcessor extends ContentProcessor {

        KotlinContentProcessor(int depth, String param) {
            super(depth, param);
        }

        @Override
        protected void onContentBlock( int depth, String code, int lastWrittenIndex, int startIndex, int endIndex ) {
            kotlinCode.append(code, lastWrittenIndex + 1, startIndex - 2);

            kotlinCode.append("object : ").append(getContentClass()).append(" {\n");

            writeIndentation(depth + 1);
            kotlinCode.append("override fun writeTo(");
            writeTemplateOutputParam();
            kotlinCode.append(") {\n");

            TemplateParser parser = new TemplateParser(code, TemplateType.Content, KotlinCodeGenerator.this, config);
            parser.setStartIndex(startIndex);
            parser.setEndIndex(endIndex);
            parser.setParamsComplete(true);
            parser.parse(depth + 2);

            writeIndentation(depth + 1);
            kotlinCode.append("}\n");

            writeIndentation(depth);
            kotlinCode.append("}");
        }

        @Override
        protected void onRemainingCode( String code, int startIndex, int endIndex ) {
            kotlinCode.append(code, startIndex, endIndex);
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

    private record ForLoopStart(CodeMarker beforeLoop, CodeMarker inLoop, int indentation, int id) {}
}
