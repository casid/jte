package org.jusecase.jte;

import org.jusecase.jte.internal.Template;
import org.jusecase.jte.internal.TemplateCompiler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class TemplateEngine {
    private final TemplateCompiler compiler;
    private final ConcurrentMap<String, Template> templateCache;


    public TemplateEngine(CodeResolver codeResolver) {
        compiler = new TemplateCompiler(codeResolver);
        templateCache = new ConcurrentHashMap<>();
    }

    public void render(String name, Object model, TemplateOutput output) {
        Template template = resolveTemplate(name);
        template.render(model, output);
    }

    public void invalidate(String name) {
        templateCache.remove(name);
    }

    public void invalidateAll() {
        templateCache.clear();
    }

    private Template resolveTemplate(String name) {
        return templateCache.computeIfAbsent(name, compiler::compile);
    }

}
