package gg.jte.compiler;

import java.util.List;

public interface TemplateParserVisitor {
    void onImport(String importClass);

    void onParam(String parameter);

    void onParamsComplete();

    void onTextPart(int depth, String textPart);

    void onCodePart(int depth, String codePart);

    void onHtmlTagBodyCodePart(int depth, String codePart, String tagName);

    void onHtmlTagAttributeCodePart(int depth, String codePart, String tagName, String attributeName);

    void onUnsafeCodePart(int depth, String codePart);

    void onCodeStatement(int depth, String codePart);

    void onConditionStart(int depth, String condition);

    void onConditionElse(int depth, String condition);

    void onConditionElse(int depth);

    void onConditionEnd(int depth);

    void onForLoopStart(int depth, String codePart);

    void onForLoopElse(int depth);

    void onForLoopEnd(int depth);

    default void onRawStart(int depth) {}

    default void onRawEnd(int depth) {}

    void onTemplateCall(int depth, String name, List<String> params);

    void onLineFinished();

    void onComplete();

    void onError(String message);

    void onError(String message, int templateLine);

    void onInterceptHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag);

    void onInterceptHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag);

    void onHtmlAttributeOutput(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute);
}
