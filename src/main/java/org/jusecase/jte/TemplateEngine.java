package org.jusecase.jte;

import org.jusecase.jte.internal.Template;
import org.jusecase.jte.internal.TemplateCompiler;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class TemplateEngine {
    private final TemplateCompiler compiler;
    private final ConcurrentMap<String, Template> templateCache;

    public TemplateEngine(CodeResolver codeResolver) {
        this(codeResolver, null);
    }

    public TemplateEngine(CodeResolver codeResolver, Path classDirectory) {
        compiler = new TemplateCompiler(codeResolver, classDirectory);
        templateCache = new ConcurrentHashMap<>();
    }

    public void render(String name, Object model, TemplateOutput output) {
        Template template = resolveTemplate(name);
        template.render(model, output);
    }

    /**
     * Notify the template engine, that the given code has changed and needs to be invalidated.
     * @param name Template, Tag or Layout name
     * @return List of templates that have been invalidated
     */
    public List<String> invalidate(String name) {
        if (name.startsWith(TemplateCompiler.TAG_DIRECTORY) || name.startsWith(TemplateCompiler.LAYOUT_DIRECTORY)) {
            templateCache.compute(name, (n, t) -> {
                compiler.clean(name);
                return null;
            });

            List<String> templateNames = compiler.getTemplatesUsing(name);
            for (String templateName : templateNames) {
                invalidate(templateName);
            }

            return templateNames;
        } else {
            templateCache.compute(name, (n, t) -> {
                compiler.clean(name);
                return null;
            });

            return Collections.singletonList(name);
        }
    }

    /**
     * Prepares the template with the given name for rendering
     */
    public void prepareForRendering(String name) {
        resolveTemplate(name);
    }

    public void cleanAll() {
        compiler.cleanAll();
    }

    public void precompileAll() {
        precompileAll(null);
    }

    public void precompileAll(List<String> compilePath) {
        compiler.precompileAll(compilePath);
    }

    private Template resolveTemplate(String name) {
        return templateCache.computeIfAbsent(name, compiler::compile);
    }

    public void setNullSafeTemplateCode(boolean value) {
        compiler.setNullSafeTemplateCode(value);
    }
}
