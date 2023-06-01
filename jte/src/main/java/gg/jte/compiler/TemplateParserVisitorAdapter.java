package gg.jte.compiler;

import java.util.List;

public class TemplateParserVisitorAdapter implements TemplateParserVisitor {
    @Override
    public void onImport(String importClass) {

    }

    @Override
    public void onParam(String parameter) {

    }

    @Override
    public void onParamsComplete() {

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
    public void onForLoopElse(int depth) {

    }

    @Override
    public void onForLoopEnd(int depth) {

    }

    @Override
    public void onTemplateCall(int depth, String name, List<String> params) {

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
    public void onError(String message, int templateLine) {

    }

    @Override
    public void onInterceptHtmlTagOpened(int depth, TemplateParser.HtmlTag htmlTag) {

    }

    @Override
    public void onInterceptHtmlTagClosed(int depth, TemplateParser.HtmlTag htmlTag) {

    }

    @Override
    public void onHtmlAttributeOutput(int depth, TemplateParser.HtmlTag currentHtmlTag, TemplateParser.HtmlAttribute htmlAttribute) {

    }
}
