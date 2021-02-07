package gg.jte.compiler;

import org.junit.jupiter.api.Test;

// This indeed is horrible.
class TemplateParametersCompleteVisitorTest {
    TemplateParametersCompleteVisitor visitor = new TemplateParametersCompleteVisitor();

    @Test
    void ignoreCoverageOfEmptyMethods() {
        visitor.onTextPart(0, null);
        visitor.onCodePart(0, null);
        visitor.onHtmlTagBodyCodePart(0, null, null);
        visitor.onHtmlTagAttributeCodePart(0, null, null, null);
        visitor.onUnsafeCodePart(0, null);
        visitor.onCodeStatement(0, null);
        visitor.onConditionStart(0, null);
        visitor.onConditionElse(0);
        visitor.onConditionElse(0, null);
        visitor.onConditionEnd(0);
        visitor.onForLoopStart(0, null);
        visitor.onForLoopEnd(0);
        visitor.onTag(0, null, null, null);
        visitor.onLineFinished();
        visitor.onComplete();
        visitor.onError(null);
        visitor.onInterceptHtmlTagOpened(0, null);
        visitor.onInterceptHtmlAttributeStarted(0, null, null);
        visitor.onInterceptHtmlTagClosed(0, null);
        visitor.onHtmlAttributeOutput(0, null, null);
    }
}