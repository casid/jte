package gg.jte.compiler;

import gg.jte.runtime.StringUtils;

import java.util.List;

class TemplateSingleControlStructureVisitor extends TemplateParserVisitorAdapter {

    private int amount;

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
    public void onRawStart(int depth) {
        incrementAmount();
    }

    @Override
    public void onRawEnd(int depth) {
        incrementAmount();
    }

    @Override
    public void onTemplateCall(int depth, String name, List<String> params) {
        throw new NotSingleControlStructure();
    }

    private void incrementAmount() {
        ++amount;
        if (amount > 1) {
            throw new NotSingleControlStructure();
        }
    }

    public static final class NotSingleControlStructure extends RuntimeException {
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
