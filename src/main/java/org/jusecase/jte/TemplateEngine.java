package org.jusecase.jte;

import org.jusecase.jte.internal.Template;
import org.jusecase.jte.internal.TemplateCompiler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class TemplateEngine {
    private final TemplateCompiler compiler;
    private final ConcurrentMap<String, Template> templateCache;


    public TemplateEngine(CodeResolver codeResolver, Mode mode) {
        compiler = new TemplateCompiler(codeResolver);

        if (mode == Mode.Development) {
            templateCache = null;
        } else if (mode == Mode.Production) {
            templateCache = new ConcurrentHashMap<>();
        } else {
            throw new IllegalStateException("Unsupported mode " + mode);
        }
    }

    public void render(String name, Object model, TemplateOutput output) {
        Template template = resolveTemplate(name);
        template.render(model, output);
    }

    private Template resolveTemplate(String name) {
        if (templateCache != null) {
            return templateCache.computeIfAbsent(name, compiler::compile);
        } else {
            return compiler.compile(name);
        }
    }

    public enum Mode {
        Development,
        Production
    }
}
