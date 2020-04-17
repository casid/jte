package org.jusecase.jte;

import java.util.HashMap;
import java.util.Map;

public class DummyCodeResolver implements CodeResolver {
    private Map<String, String> templateCode = new HashMap<>();
    private Map<String, String> tagCode = new HashMap<>();

    @Override
    public String resolveTemplate(String name) {
        return templateCode.get(name);
    }

    @Override
    public String resolveTag(String name) {
        return tagCode.get(name);
    }

    public void givenTagCode(String name, String code) {
        tagCode.put(name, code);
    }

    public void givenTemplateCode(String name, String code) {
        templateCode.put(name, code);
    }
}
