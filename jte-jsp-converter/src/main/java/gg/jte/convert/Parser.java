package gg.jte.convert;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

public class Parser {
    private final List<Converter> converters = new ArrayList<>();
    private final Deque<Converter> converterStack = new ArrayDeque<>();

    private String content;
    private int index;
    private int lastContentIndex;
    private StringBuilder result;
    private Converter currentConverter;

    public void register(Converter converter) {
        converters.add(converter);
    }

    public boolean startsWith(String token) {
        return content.startsWith(token, index);
    }

    public boolean endsWith(String token) {
        return content.startsWith(token, index - token.length());
    }

    public boolean hasNextToken(String token, int offset) {
        for (int i = index + offset; i < content.length(); ++i) {
            char c = content.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }

            return content.startsWith(token, i);
        }
        return false;
    }

    public void parseXmlAttribute(String name, Consumer<String> consumer) {
        if (!endsWith(name)) {
            return;
        }

        if (!Character.isWhitespace(content.charAt(index - name.length() - 1))) {
            return;
        }

        int equalsIndex = content.indexOf('=', index);
        if (equalsIndex == -1) {
            return;
        }

        int quotesIndex = content.indexOf('"', equalsIndex + 1);
        if (quotesIndex == -1) {
            return;
        }

        int nextQuotesIndex = content.indexOf('"', quotesIndex + 1);
        if (nextQuotesIndex == -1) {
            return;
        }

        consumer.accept(content.substring(quotesIndex + 1, nextQuotesIndex));

        index = nextQuotesIndex;
    }

    public void parseXmlAttributeAsBoolean(String name, Consumer<Boolean> consumer) {
        parseXmlAttribute(name, v -> consumer.accept(Boolean.parseBoolean(v)));
    }

    public String convert(String content) {
        this.content = content;
        this.result = new StringBuilder(content.length());

        for (index = 0; index < content.length(); ++index) {
            if (currentConverter == null) {
                for (Converter converter : converters) {
                    if (converter.canConvert(this)) {
                        pushConverter(converter);
                    }
                }
            }

            if (currentConverter != null) {
                if (currentConverter.advance(this)) {
                    popConverter();
                }
            }
        }

        appendContentToResultIfRequired();

        return result.toString();
    }

    private void appendContentToResultIfRequired() {
        if (currentConverter == null && lastContentIndex < index) {
            result.append(content, lastContentIndex, index);
        }
    }

    private void pushConverter(Converter converter) {
        appendContentToResultIfRequired();

        currentConverter = converter.newInstance();
        converterStack.add(currentConverter);
    }

    private void popConverter() {
        Converter converter = converterStack.pop();
        if (converter != null) {
            converter.convert(result);
        }
        currentConverter = converterStack.peek();

        lastContentIndex = index;
    }
}
