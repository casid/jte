package gg.jte.compiler;

class TemplateParametersCompleteVisitor extends TemplateParserVisitorAdapter {

    @Override
    public void onImport(String importClass) {
        throw new Result(false);
    }

    @Override
    public void onParam(String parameter) {
        throw new Result(false);
    }

    @Override
    public void onParamsComplete() {
        throw new Result(true);
    }

    public static final class Result extends RuntimeException {
        public final boolean complete;

        public Result(boolean complete) {
            this.complete = complete;
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }
}
