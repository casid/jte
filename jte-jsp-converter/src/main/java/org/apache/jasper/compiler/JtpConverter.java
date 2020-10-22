package org.apache.jasper.compiler;

import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.ConverterOutput;
import gg.jte.convert.jsp.Converter;
import gg.jte.convert.jsp.JspElementType;
import gg.jte.convert.jsp.converter.JspAttributeConverter;
import gg.jte.convert.jsp.converter.JspExpressionConverter;
import org.apache.jasper.JasperException;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JtpConverter extends Node.Visitor implements Converter {
    private static final Pattern NEW_LINE_CLEANUP = Pattern.compile("\n{3,}");
    private static final Pattern NEW_LINE_WHITESPACE_CLEANUP = Pattern.compile("\n([\\t ]+)\n");

    private final String relativeFilePath;
    private final byte[] input;
    private final URL resourceBase;
    private final boolean tagFile;
    private final ConverterOutput output;
    private final Map<String, CustomTagConverter> converters = new HashMap<>();
    private final Set<String> inlinedIncludes = new HashSet<>();
    private final Map<String, EnumSet<JspElementType>> suppressions = new HashMap<>();
    private final Set<String> imports = new TreeSet<>();
    private final Set<String> params = new TreeSet<>();

    private String prefix;
    private String lineSeparator = "\n";

    public JtpConverter(String relativeFilePath, byte[] input, URL resourceBase, boolean tagFile, ConverterOutput output) {
        this.relativeFilePath = relativeFilePath;
        this.input = input;
        this.resourceBase = resourceBase;
        this.tagFile = tagFile;
        this.output = output;
    }

    @Override
    public void register(String tagName, CustomTagConverter converter) {
        this.converters.put(tagName, converter);
    }

    @Override
    public void addInlinedInclude(String path) {
        inlinedIncludes.add(path);
    }

    @Override
    public void addSuppressions(String path, EnumSet<JspElementType> suppressions) {
        this.suppressions.put(path, suppressions);
    }

    @Override
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    @Override
    public void setIndentationCount(int indentationCount) {
        output.setIndentationCount(indentationCount);
    }

    @Override
    public void setIndentationChar(char indentationChar) {
        output.setIndentationChar(indentationChar);
    }

    @Override
    public String convert() {
        try {
            Node.Nodes nodes = JtpParser.parse(relativeFilePath, input, resourceBase, tagFile);

            nodes.visit(this);

            return cleanResult(output.trim().prepend(createPrefix()).toString());
        } catch (JasperException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String createPrefix() {
        StringBuilder result = new StringBuilder(prefix == null ? "" : prefix);

        for (String type : imports) {
            result.append("@import ").append(type).append('\n');
        }

        if (result.length() > 0) {
            result.append('\n');
        }

        for (String param : params) {
            result.append("@param ").append(param).append('\n');
        }

        return result.toString();
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void addImport(String className) {
        imports.add(className);
    }

    public void addParam(String param) {
        params.add(param);
    }

    private String cleanResult(String result) {
        result = NEW_LINE_WHITESPACE_CLEANUP.matcher(result).replaceAll("\n");
        result = NEW_LINE_CLEANUP.matcher(result).replaceAll("\n\n");
        result = result.replace("\n", lineSeparator);
        return result;
    }

    @Override
    public void visit(Node.TemplateText n) {
        if (output.isTrimWhitespace()) {
            output.append(n.getText().trim());
        } else {
            output.append(n.getText());
        }
    }

    @Override
    public void visit(Node.UseBean n) throws JasperException {
        if (isSuppressed(n)) {
            return;
        }
        super.visit(n);
    }

    @Override
    public void visit(Node.ELExpression n) {
        if (!output.isInsideScript()) {
            output.append("${");
        }

        output.append(convertAttributeValue("${" + n.getText() + "}"));

        if (!output.isInsideScript()) {
            output.append("}");
        }
    }

    @Override
    public void visit(Node.PageDirective n) {
        // noop
    }

    @Override
    public void visit(Node.Root n) throws JasperException {
        visitBody(n);
    }

    @Override
    public void visit(Node.TaglibDirective n) {
        // noop
    }

    @Override
    public void visit(Node.TagDirective n) {
        // noop
    }

    @Override
    public void visit(Node.SetProperty n) {
        String name = n.getAttributeValue("name");
        String value = n.getAttributeValue("value");
        String property = n.getAttributeValue("property");

        output.append("!{");
        output.append(name).append(".set");
        output.append(Character.toUpperCase(property.charAt(0)) + property.substring(1));
        output.append("(");
        output.append(JspExpressionConverter.convertAttributeValue(value));
        output.append(");}");
    }

    @Override
    public void visit(Node.AttributeDirective n) {
        new JspAttributeConverter().convert(this, new JtpAttribute(n), output);
    }

    @Override
    public void visit(Node.Declaration n) throws JasperException {
        if (isSuppressed(n)) {
            return;
        }
        super.visit(n);
    }

    @Override
    public void visit(Node.CustomTag n) throws JasperException {
        if (isSuppressed(n)) {
            return;
        }

        CustomTagConverter converter = converters.get(n.getQName());
        if (converter == null) {
            throw new RuntimeException("Missing converter for custom tag: <" + n.getQName() + " />");
        }

        JtpCustomTag tag = new JtpCustomTag(n);

        output.pushTrimWhitespace(converter.isTrimWhitespace());
        converter.convert(this, tag, output, () -> tag.visitBody(this));
        output.popTrimWhitespace();
    }

    @Override
    public void visit(Node.Comment n) {
        if (isSuppressed(n)) {
            return;
        }

        output.append("<%--").append(n.getText()).append("--%>");
    }

    @Override
    public void visit(Node.Scriptlet n) throws JasperException {
        if (isSuppressed(n)) {
            return;
        }

        super.visit(n);
    }

    public static JspElementType getType(Node n) {
        return JspElementType.valueOf(n.getClass().getSimpleName());
    }

    @Override
    public void visit(Node.IncludeDirective n) throws JasperException {
        String file = n.getAttributeValue("file");
        if (!inlinedIncludes.contains(file)) {
            throw new UnsupportedOperationException("Includes are not supported. You should convert it to a tag first, or suppress with addInlinedInclude(\"" + file + "\")");
        }

        visitBody(n);
    }

    @Override
    public void visit(Node.DoBodyAction n) {
        addImport("gg.jte.Content");
        addParam("Content bodyContent");
        output.append("${bodyContent}").newLine();
    }

    @Override
    protected void doVisit(Node n) {
        throw new RuntimeException("Unsupported feature detected: " + n.getClass().getSimpleName());
    }

    private boolean isSuppressed(Node n) {
        EnumSet<JspElementType> suppressedTypes = suppressions.get(n.getStart().getFile());
        if (suppressedTypes == null) {
            return false;
        }

        JspElementType type = getType(n);
        return suppressedTypes.contains(type);
    }
}
