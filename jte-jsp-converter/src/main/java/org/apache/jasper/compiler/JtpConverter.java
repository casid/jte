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
import java.util.HashMap;
import java.util.Map;

import static gg.jte.convert.jsp.converter.JspExpressionConverter.convertAttributeValue;

public class JtpConverter extends Node.Visitor {
    private final ConverterOutput output;
    private final Map<String, CustomTagConverter> converters = new HashMap<>();

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

                    return prefix + output.toString();
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

    @Override
    public void visit(Node.TemplateText n) throws JasperException {
        if (output.isTrimWhitespace()) {
            output.append(n.getText().trim());
        } else {
            output.append(n.getText());
        }
    }

    @Override
    public void visit(Node.ELExpression n) throws JasperException {
        if (!output.isInsideScript()) {
            output.append("${");
        }

        output.append(convertAttributeValue("${" + n.getText() + "}"));

        if (!output.isInsideScript()) {
            output.append("}");
        }
    }

    @Override
    public void visit(Node.PageDirective n) throws JasperException {
        // noop
    }

    @Override
    public void visit(Node.Root n) throws JasperException {
        visitBody(n);
    }

    @Override
    public void visit(Node.TaglibDirective n) throws JasperException {
        if (n.getAttributeValue("uri").equals("http://java.sun.com/jsp/jstl/core")) {
            return;
        }

        if (n.getAttributeValue("uri").equals("http://java.sun.com/jsp/jstl/fmt")) {
            return;
        }

        throw new RuntimeException("Unsupported taglib: " + n.getAttributeValue("uri"));
    }

    @Override
    public void visit(Node.AttributeDirective n) throws JasperException {
        new JspAttributeConverter().convert(new JtpAttribute(n), output);
    }

    @Override
    public void visit(Node.CustomTag n) throws JasperException {
        CustomTagConverter converter = converters.get(n.getQName());
        if (converter == null) {
            throw new RuntimeException("Missing converter for custom tag: <" + n.getQName() + " />");
        }

        JtpCustomTag tag = new JtpCustomTag(n);

        converter.before(tag, output);
        visitBody(n);
        converter.after(tag, output);
    }

    @Override
    public void visit(Node.Comment n) throws JasperException {
        output.append("<%--").append(n.getText()).append("--%>");
    }

    @Override
    protected void doVisit(Node n) throws JasperException {
        throw new RuntimeException("Unsupported feature detected: " + n.getClass().getSimpleName());
    }
}
