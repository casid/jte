package org.apache.jasper.compiler;

public class JtpAttribute {
    private final Node.AttributeDirective attributeDirective;

    public JtpAttribute(Node.AttributeDirective attributeDirective) {
        this.attributeDirective = attributeDirective;
    }

    public String getAttribute(String name) {
        return this.attributeDirective.getAttributeValue(name);
    }

    public boolean isRequired() {
        return Boolean.parseBoolean(getAttribute("required"));
    }

    public String getName() {
        return getAttribute("name");
    }

    public String getType() {
        return getAttribute("type");
    }
}
