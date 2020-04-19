package org.jusecase.jte.internal;

interface ParameterParserVisitor {
    void onImport(String importClass);
    void onParameter(String parameter);
}
