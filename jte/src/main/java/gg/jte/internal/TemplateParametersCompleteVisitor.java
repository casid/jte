package gg.jte.internal;

import java.util.List;

class TemplateParametersCompleteVisitor implements TemplateParserVisitor {

    @Override
    public void onImport(String importClass) {
        throw new Result(false);
    }

    @Override
    public void onParam(ParamInfo parameter) {
        throw new Result(false);
    }

    @Override
    public void onParamsComplete() {
        throw new Result(true);
    }

    @Override
    public void onTextPart(int depth, String textPart) {

    }

    @Override
    public void onCodePart(int depth, String codePart) {

    }

    @Override
    public void onHtmlTagBodyCodePart(int depth, String codePart, String tagName) {

    }

    @Override
    public void onHtmlTagAttributeCodePart(int depth, String codePart, String tagName, String attributeName) {

    }

    @Override
    public void onUnsafeCodePart(int depth, String codePart) {

    }

    @Override
    public void onCodeStatement(int depth, String codePart) {

    }

    @Override
    public void onConditionStart(int depth, String condition) {

    }

    @Override
    public void onConditionElse(int depth, String condition) {

    }

    @Override
    public void onConditionElse(int depth) {

    }

    @Override
    public void onConditionEnd(int depth) {

    }

    @Override
    public void onForLoopStart(int depth, String codePart) {

    }

    @Override
    public void onForLoopEnd(int depth) {

    }

    @Override
    public void onTag(int depth, TemplateType type, String name, List<String> params) {

    }

    @Override
    public void onLineFinished() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag) {

    }

    @Override
    public void onHtmlAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {

    }

    @Override
    public void onHtmlBooleanAttributeStarted(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {

    }

    @Override
    public void onHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag) {

    }

    public static final class Result extends RuntimeException {
        public final boolean complete;

        public Result(boolean complete) {
            this.complete = complete;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
