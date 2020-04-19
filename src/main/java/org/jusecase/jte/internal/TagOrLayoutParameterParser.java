package org.jusecase.jte.internal;

import java.util.ArrayList;
import java.util.List;

final class TagOrLayoutParameterParser {
    List<String> importClasses = new ArrayList<>();
    List<String> parameters = new ArrayList<>();

    public int parse(String templateCode) {
        return new ParameterParser(templateCode, new ParameterParserVisitor() {
            @Override
            public void onImport(String importClass) {
                importClasses.add(importClass);
            }

            @Override
            public void onParameter(String parameter) {
                parameters.add(parameter);
            }
        }).parse();
    }
}
