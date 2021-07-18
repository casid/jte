package gg.jte.compiler.kotlin;

import gg.jte.ContentType;
import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.compiler.*;
import gg.jte.runtime.ClassInfo;
import gg.jte.runtime.Constants;
import gg.jte.runtime.DebugInfo;
import gg.jte.runtime.TemplateType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static gg.jte.runtime.Constants.TEXT_PART_BINARY;

@SuppressWarnings("unused") // Used by TemplateCompiler
public class KotlinCodeGenerator implements CodeGenerator {
    private final TemplateCompiler compiler;
    private final TemplateConfig config;
    private final ConcurrentHashMap<String, List<ParamInfo>> paramOrder;
    private final ClassInfo classInfo;
    private final CodeBuilder kotlinCode = new CodeBuilder();
    private final LinkedHashSet<ClassDefinition> classDefinitions;
    private final LinkedHashSet<String> templateDependencies;
    private final List<ParamInfo> parameters = new ArrayList<>();
    private final List<byte[]> binaryTextParts = new ArrayList<>();

    private boolean hasWrittenPackage;
    private boolean hasWrittenClass;

    public KotlinCodeGenerator(TemplateCompiler compiler, TemplateConfig config, ConcurrentHashMap<String, List<ParamInfo>> paramOrder, ClassInfo classInfo, LinkedHashSet<ClassDefinition> classDefinitions, LinkedHashSet<String> templateDependencies) {
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
        kotlinCode.append("import ").append(importClass).append("\n");
    }

    private void writePackageIfRequired() {
        if (!hasWrittenPackage) {
            kotlinCode.append("package " + classInfo.packageName + "\n");
            hasWrittenPackage = true;
        }
    }

    @Override
    public void onParam(String parameter) {
        ParamInfo paramInfo = KotlinParamInfo.parse(parameter, this, kotlinCode.getCurrentTemplateLine());

        writePackageIfRequired();
        if (!hasWrittenClass) {
            writeClass();
        }

        kotlinCode.append(", ");
        if (paramInfo.varargs) {
            kotlinCode.append("vararg ");
        }
        kotlinCode.append(paramInfo.name).append(':').append(paramInfo.type);

        parameters.add(paramInfo);
    }

    private void writeClass() {
        kotlinCode.append("class ").append(classInfo.className).append(" {\n");
        kotlinCode.append("companion object {\n");
        kotlinCode.markFieldsIndex();
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
        int lineCount = 2;
        if (!binaryTextParts.isEmpty()) {
            lineCount += binaryTextParts.size() + 1;
        }
        kotlinCode.insertFieldLines(lineCount);

        StringBuilder fields = new StringBuilder(64 + 32 * lineCount);
        addNameField(fields, classInfo.name);
        addLineInfoField(fields);
        writeBinaryTextParts(fields);

        kotlinCode.insertFields(fields);

        kotlinCode.append("\t}\n");

        kotlinCode.append("\t@JvmStatic fun renderMap(");
        writeTemplateOutputParam();
        kotlinCode.append(", jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?");

        kotlinCode.append(", params:Map<String, Any?>) {\n");
        for (ParamInfo parameter : parameters) {
            if (parameter.varargs) {
                continue;
            }

            kotlinCode.setCurrentTemplateLine(parameter.templateLine);
            kotlinCode.append("\t\tval ").append(parameter.name).append(" = params[\"").append(parameter.name).append("\"] as ").append(parameter.type);
            if (parameter.defaultValue != null) {
                kotlinCode.append("? ?: ");
                writeCodeWithContentSupport(0, parameter.defaultValue);
            }
            kotlinCode.append('\n');
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

        this.classInfo.lineInfo = kotlinCode.getLineInfo();
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
                .append(".javaClass, \"")
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
        kotlinCode.append("jteOutput.setContext(\"").append(tagName).append("\", \"").append(attributeName).append("\")\n");

        writeCodePart(depth, codePart);
    }

    @Override
    public void onUnsafeCodePart(int depth, String codePart) {
        if (config.contentType == ContentType.Html) {
            writeIndentation(depth);
            kotlinCode.append("jteOutput.setContext(null, null)\n");
        }

        writeCodePart(depth, codePart);
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
        writeIndentation(depth);
        kotlinCode.append("for (").append(codePart).append(") {\n");
    }

    @Override
    public void onForLoopEnd(int depth) {
        writeIndentation(depth);
        kotlinCode.append("}\n");
    }

    @Override
    public void onTag(int depth, TemplateType type, String name, List<String> params) {
        ClassInfo tagInfo = compiler.generateTagOrLayout(type, name, "kte", classDefinitions, templateDependencies, getCurrentDebugInfo());

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
    public void onInterceptHtmlAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
        writeIndentation(depth);
        kotlinCode.append("jteHtmlInterceptor?.onHtmlAttributeStarted(\"").append(htmlAttribute.name).append("\", ");
        writeAttributeMap(currentHtmlTag);
        kotlinCode.append(", jteOutput)\n");
    }

    @Override
    public void onInterceptHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag) {
        writeIndentation(depth);
        kotlinCode.append("jteHtmlInterceptor?.onHtmlTagClosed(\"").append(htmlTag.name).append("\", jteOutput)\n");
    }

    @Override
    public void onHtmlAttributeOutput(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
        String javaExpression = CodeGenerator.extractTemplateExpression(htmlAttribute.value);
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
        CodeGenerator.writeAttributeMap(kotlinCode, htmlTag);
    }

    private void writeCodeWithContentSupport(int depth, String code) {
        if (code.contains("@`")) {
            new ContentProcessor(depth, code).process();
        } else {
            kotlinCode.append(code);
        }
    }

    private DebugInfo getCurrentDebugInfo() {
        return new DebugInfo(classInfo.name, kotlinCode.getCurrentTemplateLine() + 1);
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

        throw new TemplateException("Failed to compile template, error at " + classInfo.name + ":" + kotlinCode.getCurrentTemplateLine() + ". No parameter with name " + paramCallInfo.name + " is defined in " + name);
    }

    private void writeIndentation(int depth) {
        for (int i = 0; i < depth + 2; ++i) {
            kotlinCode.append('\t');
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
                        writeCode();
                    } else {
                        --nestedCount;
                    }
                }
            }

            if (lastWrittenIndex + 1 < param.length()) {
                kotlinCode.append(param, lastWrittenIndex + 1, param.length());
            }
        }

        private void writeCode() {
            kotlinCode.append(param, lastWrittenIndex + 1, startIndex - 2);

            kotlinCode.append("object : ").append(getContentClass()).append(" {\n");

            writeIndentation(depth + 1);
            kotlinCode.append("override fun writeTo(");
            writeTemplateOutputParam();
            kotlinCode.append(") {\n");

            TemplateParser parser = new TemplateParser(param, TemplateType.Content, KotlinCodeGenerator.this, config);
            parser.setStartIndex(startIndex);
            parser.setEndIndex(endIndex);
            parser.setParamsComplete(true);
            parser.parse(depth + 2);

            writeIndentation(depth + 1);
            kotlinCode.append("}\n");

            writeIndentation(depth);
            kotlinCode.append("}");

            lastWrittenIndex = endIndex;

            startIndex = -1;
            endIndex = -1;
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
