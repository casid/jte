package org.apache.jasper.compiler;

import gg.jte.convert.ConverterOutput;
import gg.jte.convert.CustomTagConverter;
import gg.jte.convert.StandardConverterOutput;
import gg.jte.convert.jsp.Converter;
import gg.jte.convert.jsp.converter.JspAttributeConverter;
import org.apache.jasper.JasperException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JtpConverter extends Node.Visitor {
    private static final Pattern NEW_LINE_CLEANUP = Pattern.compile("\n{3,}");
    private static final Pattern NEW_LINE_WHITESPACE_CLEANUP = Pattern.compile("\n([\\t ]+)\n");

    private final ConverterOutput output;
    private final Map<String, CustomTagConverter> converters = new HashMap<>();

    private final Deque<Boolean> trimWhitespace = new ArrayDeque<>();

    public JtpConverter(ConverterOutput output) {
        this.output = output;
    }

    private void register(String tagName, CustomTagConverter converter) {
        this.converters.put(tagName, converter);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(newBuilder(Files.readAllBytes(Paths.get("jte-jsp-converter/testdata/simpleTagWithForEach/before/jsp/simple.tag")), true));
    }

    public static Converter newBuilder(byte[] input, boolean tagFile) {
        return new Converter() {
            private final Map<String, CustomTagConverter> converters = new HashMap<>();
            private String prefix = "";

            @Override
            public void register(String tagName, CustomTagConverter converter) {
                converters.put(tagName, converter);
            }

            @Override
            public String convert() {
                var output = new StandardConverterOutput();

                try {
                    Node.Nodes nodes = JtpParser.parse(input, tagFile);

                    JtpConverter converter = new JtpConverter(output);
                    this.converters.forEach(converter::register);

                    nodes.visit(converter);

                    String result = prefix + output.toString();
                    return cleanResult(result);
                } catch (JasperException | IOException | SAXException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void setPrefix(String prefix) {
                this.prefix = prefix;
            }
        };
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
    protected void doVisit(Node n) {
        throw new RuntimeException("Unsupported feature detected: " + n.getClass().getSimpleName());
    }
}
