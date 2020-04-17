package org.jusecase.jte.internal;

interface TemplateParserVisitor {
    void onTextPart(int depth, String textPart);

    void onCodePart(int depth, String codePart);

    void onCodeStatement(int depth, String codePart);

    void onConditionStart(int depth, String condition);

    void onConditionElse(int depth, String condition);

    void onConditionElse(int depth);

    void onConditionEnd(int depth);

    void onForLoopStart(int depth, String codePart);

    void onForLoopEnd(int depth);

    void onTag(int depth, String name, String params);

}
