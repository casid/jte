package gg.jte.convert.jsp.converter;

import gg.jte.convert.Converter;
import gg.jte.convert.Parser;
import gg.jte.convert.xml.XmlAttributesParser;

public abstract class AbstractJspTagConverter implements Converter {

    private final String openTagPrefix;
    private final String closingTag;

    private XmlAttributesParser attributes;
    private boolean body;
    private boolean hasBody;

    public AbstractJspTagConverter(String tagName) {
        openTagPrefix = "<" + tagName;
        closingTag = "</" + tagName + ">";
    }

    @Override
    public final boolean advance(Parser parser) {
        if (body) {
            if (advanceBody(parser)) {
                appendContentBeforeClosingTag(parser);
                convertBody(parser, parser.getResult());

                if (dropClosingTagLine()) {
                    parser.removeLeadingSpaces();
                    parser.advanceIndex(closingTag.length());
                    parser.advanceIndexAfter('\n', 0);
                } else {
                    parser.advanceIndex(closingTag.length());
                }
                parser.markLastContentIndex(1);
                return true;
            }
        } else if (advanceTag(parser)) {
            convertTag(parser, parser.getResult());
            parser.markLastContentIndex(1);
            if (!hasBody) {
                return true;
            }

            body = true;
        }

        return false;
    }

    protected void appendContentBeforeClosingTag(Parser parser) {
        parser.appendContentToResultIfRequired();
    }

    @Override
    public boolean canConvert(Parser parser) {
        return parser.startsWith(openTagPrefix);
    }

    private boolean advanceTag(Parser parser) {
        if (attributes == null) {
            attributes = parser.parseXmlAttributes(openTagPrefix.length());
            parseAttributes(attributes);
        }

        if (parser.startsWith("/>")) {
            parser.advanceIndex(2);
            hasBody = false;
            return true;
        }

        if (parser.startsWith(">")) {
            parser.advanceIndex(1);
            hasBody = true;
            return true;
        }

        return false;
    }

    private boolean advanceBody(Parser parser) {
        return parser.startsWith(closingTag);
    }

    public abstract void convertTag(Parser parser, StringBuilder result);

    public abstract void convertBody(Parser parser, StringBuilder result);

    protected abstract void parseAttributes(XmlAttributesParser attributes);

    protected boolean dropClosingTagLine() {
        return false;
    }
}
