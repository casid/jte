package org.jusecase.jte.internal;

import java.util.ArrayList;
import java.util.List;

final class TemplateParameterParser {
    final List<String> importClasses = new ArrayList<>();

    int lastIndex;
    String className;
    String instanceName;

    public void parse(String templateCode) {
        lastIndex = new ParameterParser(templateCode, new ParameterParserVisitor() {
            @Override
            public void onImport(String importClass) {
                importClasses.add(importClass);
            }

            @Override
            public void onParameter(String parameter) {
                String[] params = parameter.split(" ");
                className = params[0];
                instanceName = params[1];
            }
        }).parse();
    }
}
