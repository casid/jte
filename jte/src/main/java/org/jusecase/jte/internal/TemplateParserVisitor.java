package org.jusecase.jte.internal;

import java.util.List;

interface TemplateParserVisitor {
    void onImport(String importClass);

    void onParam(ParamInfo parameter);

    void onParamsComplete();

    void onTextPart(int depth, String textPart);

    void onCodePart(int depth, String codePart);

    void onUnsafeCodePart(int depth, String codePart);

    void onCodeStatement(int depth, String codePart);

    void onConditionStart(int depth, String condition);

    void onConditionElse(int depth, String condition);

    void onConditionElse(int depth);

    void onConditionEnd(int depth);

    void onForLoopStart(int depth, String codePart);

    void onForLoopEnd(int depth);

    void onTag(int depth, String name, List<String> params);

    void onLayout(int depth, String name, List<String> params);

    void onLayoutRender(int depth, String name);

    void onLayoutDefine(int depth, String name);

    void onLayoutDefineEnd(int depth);

    void onLayoutEnd(int depth);

    void onLineFinished();

    void onComplete();

    void onHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag);

    void onHtmlAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute);

    void onHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag);
}
