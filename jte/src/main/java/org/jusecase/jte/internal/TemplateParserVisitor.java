package org.jusecase.jte.internal;

import java.util.List;

interface TemplateParserVisitor {
    void onImport(String importClass);

    void onParam(ParamInfo parameter);

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

    void onForLoopEnd(int depth);

    void onTag(int depth, TemplateType type, String name, List<String> params);

    void onLineFinished();

    void onComplete();

    void onError(String message);

    void onHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag);

    void onHtmlAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute);

    void onHtmlBooleanAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute);

    void onHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag);
}
