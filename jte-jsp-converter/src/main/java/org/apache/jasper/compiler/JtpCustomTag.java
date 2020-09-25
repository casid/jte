package org.apache.jasper.compiler;

import org.apache.jasper.JasperException;
import org.xml.sax.Attributes;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class JtpCustomTag {
    private final Node.CustomTag customTag;

    public JtpCustomTag(Node.CustomTag customTag) {
        this.customTag = customTag;
    }

    public String getAttribute(String name) {
        return this.customTag.getAttributeValue(name);
    }

    public boolean hasBody() {
        return !this.customTag.hasEmptyBody();
    }

    public JtpCustomTag parent(Predicate<JtpCustomTag> tagFilter) {
        Node parent = this.customTag.getParent();

        while (!(parent instanceof Node.CustomTag)) {
            if (parent == null) {
                return null;
            }
            parent = parent.parent;
        }

        Node.CustomTag customTagParent = (Node.CustomTag) parent;
        JtpCustomTag jtpCustomTag = new JtpCustomTag(customTagParent);

        if (tagFilter.test(jtpCustomTag)) {
            return jtpCustomTag;
        }

        return jtpCustomTag.parent(tagFilter);
    }

    public String getTagName() {
        return this.customTag.getQName();
    }

    public static Predicate<JtpCustomTag> byTagName(String tagName) {
        return tag -> tag.getTagName().equals(tagName);
    }

    public int indexOf(JtpCustomTag tag) {
        Node.Nodes body = this.customTag.getBody();

        int index = 0;

        for (int i = 0; i < body.size(); i++) {
            if (!(body.getNode(i) instanceof Node.CustomTag)) {
                continue;
            }

            if (body.getNode(i) == tag.customTag) {
                return index;
            }

            index++;
        }

        throw new RuntimeException("Failed to determine index of child tag.");
    }

    public Attributes getAttributes() {
        return this.customTag.getAttributes();
    }

    public boolean hasParent(Predicate<JtpCustomTag> tagFilter) {
        return parent(tagFilter) != null;
    }

    public void forEach(Consumer<JtpCustomTag> tagConsumer) {
        try {
            this.customTag.body.visit(new Node.Visitor() {
                @Override
                public void visit(Node.CustomTag n) throws JasperException {
                    tagConsumer.accept(new JtpCustomTag(n));
                    super.visit(n);
                }
            });
        } catch (JasperException e) {
            throw new RuntimeException(e);
        }
    }
}
