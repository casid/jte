package gg.jte.compiler;

import gg.jte.runtime.StringUtils;
import gg.jte.runtime.TemplateType;

import java.util.List;

class TemplateSingleControlStructureVisitor implements TemplateParserVisitor {

    private int amount;

    @Override
    public void onImport(String importClass) {
        // not relevant
    }

    @Override
    public void onParam(String parameter) {
        // not relevant
    }

    @Override
    public void onParamsComplete() {
    }

    @Override
    public void onTextPart(int depth, String textPart) {
        if (!StringUtils.isBlank(textPart)) {
            throw new NotSingleControlStructure();
        }
    }

    @Override
    public void onCodePart(int depth, String codePart) {
        throw new NotSingleControlStructure();
    }

    @Override
    public void onHtmlTagBodyCodePart(int depth, String codePart, String tagName) {
        // not relevant
    }

    @Override
    public void onHtmlTagAttributeCodePart(int depth, String codePart, String tagName, String attributeName) {
        // not relevant
    }

    @Override
    public void onUnsafeCodePart(int depth, String codePart) {
        throw new NotSingleControlStructure();
    }

    @Override
    public void onCodeStatement(int depth, String codePart) {
        incrementAmount();
    }

    @Override
    public void onConditionStart(int depth, String condition) {
        incrementAmount();
    }

    @Override
    public void onConditionElse(int depth, String condition) {
        incrementAmount();
    }

    @Override
    public void onConditionElse(int depth) {
        incrementAmount();
    }

    @Override
    public void onConditionEnd(int depth) {
        incrementAmount();
    }

    @Override
    public void onForLoopStart(int depth, String codePart) {
        incrementAmount();
    }

    @Override
    public void onForLoopEnd(int depth) {
        incrementAmount();
    }

    @Override
    public void onTag(int depth, TemplateType type, String name, List<String> params) {
        throw new NotSingleControlStructure();
    }

    @Override
    public void onLineFinished() {
        // not relevant
    }

    @Override
    public void onComplete() {
        // not relevant
    }

    @Override
    public void onError(String message) {
        // not relevant
    }

    @Override
    public void onError(String message, int templateLine) {
        // not relevant
    }

    @Override
    public void onInterceptHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag) {
        // not relevant
    }

    @Override
    public void onInterceptHtmlAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
        // not relevant
    }

    @Override
    public void onInterceptHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag) {
        // not relevant
    }

    @Override
    public void onHtmlAttributeOutput(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {
        // not relevant
    }

    private void incrementAmount() {
        ++amount;
        if (amount > 1) {
            throw new NotSingleControlStructure();
        }
    }

    public static final class NotSingleControlStructure extends RuntimeException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
