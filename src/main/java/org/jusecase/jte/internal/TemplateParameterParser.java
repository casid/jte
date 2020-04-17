package org.jusecase.jte.internal;

final class TemplateParameterParser {
    int lastIndex;
    String className;
    String instanceName;

    public void parse(String templateCode) {
        ParameterParser parser = new ParameterParser();
        lastIndex = parser.parse(templateCode, parameter -> {
            String[] params = parameter.split(" ");
            className = params[0];
            instanceName = params[1];
        });
    }
}
