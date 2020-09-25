package org.apache.jasper.compiler;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.jsp.Converter;
import gg.jte.convert.jsp.converter.JspAttributeConverter;
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

    private String prefix;

    private final Deque<Boolean> trimWhitespace = new ArrayDeque<>();

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
    public String convert() {
        try {
            Node.Nodes nodes = JtpParser.parse(relativeFilePath, input, resourceBase, tagFile);

            nodes.visit(this);

            return cleanResult(output.prepend(prefix).toString());
        } catch (JasperException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private static String cleanResult(String result) {
        result = NEW_LINE_WHITESPACE_CLEANUP.matcher(result).replaceAll("\n");
        result = NEW_LINE_CLEANUP.matcher(result).replaceAll("\n\n");
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

    }

    @Override
    public void visit(Node.AttributeDirective n) {
        new JspAttributeConverter().convert(new JtpAttribute(n), output);
    }

    @Override
    public void visit(Node.CustomTag n) throws JasperException {
        CustomTagConverter converter = converters.get(n.getQName());
        if (converter == null) {
            throw new RuntimeException("Missing converter for custom tag: <" + n.getQName() + " />");
        }

        JtpCustomTag tag = new JtpCustomTag(n);

        pushTrimWhitespace(converter.isTrimWhitespace());
        converter.convert(tag, output, () -> visitBody(n));
        popTrimWhitespace();
    }

    private void pushTrimWhitespace(boolean trimWhitespace) {
        this.trimWhitespace.push(trimWhitespace);
        output.setTrimWhitespace(trimWhitespace);
    }

    private void popTrimWhitespace() {
        this.trimWhitespace.pop();
        if (this.trimWhitespace.isEmpty()) {
            output.setTrimWhitespace(false);
        } else {
            output.setTrimWhitespace(this.trimWhitespace.peek());
        }
    }

    @Override
    public void visit(Node.Comment n) {
        output.append("<%--").append(n.getText()).append("--%>");
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
    protected void doVisit(Node n) {
        throw new RuntimeException("Unsupported feature detected: " + n.getClass().getSimpleName());
    }
}
