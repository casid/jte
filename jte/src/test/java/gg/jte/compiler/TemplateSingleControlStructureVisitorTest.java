package gg.jte.compiler;

import org.junit.jupiter.api.Test;

// This indeed is horrible.
class TemplateSingleControlStructureVisitorTest {
    TemplateSingleControlStructureVisitor visitor = new TemplateSingleControlStructureVisitor();

    @Test
    void ignoreCoverageOfEmptyMethods() {
        visitor.onImport(null);
        visitor.onParam(null);
        visitor.onParamsComplete();
        visitor.onHtmlTagBodyCodePart(0, null, null);
        visitor.onHtmlTagAttributeCodePart(0, null, null, null);
        visitor.onLineFinished();
        visitor.onComplete();
        visitor.onError(null);
        visitor.onInterceptHtmlTagOpened(0, null);
        visitor.onInterceptHtmlTagClosed(0, null);
        visitor.onHtmlAttributeOutput(0, null, null);
    }
}