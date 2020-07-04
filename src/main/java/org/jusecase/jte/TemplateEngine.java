package org.jusecase.jte;

import org.jusecase.jte.internal.DebugInfo;
import org.jusecase.jte.internal.Template;
import org.jusecase.jte.internal.TemplateCompiler;
import org.jusecase.jte.internal.TemplateMode;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * jte is a simple, yet powerful template engine for Java.
 * All jte templates are compiled to Java class files, meaning jte adds essentially zero overhead to your application.
 * jte is designed to introduce as few new keywords as possible and builds upon existing Java features,
 * so that it is very easy to reason about what a template does.
 *
 * Read more at the official documentation at https://github.com/casid/jte/blob/master/DOCUMENTATION.md
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class TemplateEngine {
    private final TemplateCompiler compiler;
    private final TemplateMode templateMode;
    private final ConcurrentMap<String, Template> templateCache;

    /**
     * Creates a new template engine.
     * All templates are compiled to Java class files on demand.
     * A JDK is required.
     * Every template has its own class loader.
     * This is recommended when running templates on your developer machine.
     *
     * @param codeResolver to lookup jte templates
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine create(CodeResolver codeResolver) {
        return create(codeResolver, Path.of("jte"));
    }

    /**
     * Creates a new template engine.
     * All templates are compiled to Java class files on demand.
     * A JDK is required.
     * Every template has its own class loader.
     * This is recommended when running templates on your developer machine.
     *
     * @param codeResolver to lookup jte templates
     * @param classDirectory where template class files are compiled to
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine create(CodeResolver codeResolver, Path classDirectory) {
        return new TemplateEngine(codeResolver, classDirectory, TemplateMode.OnDemand);
    }

    /**
     * Creates a new template engine.
     * All templates must have been precompiled to Java class files already.
     * The template engine will load them from the specified classDirectory.
     * No JDK is required.
     * All templates share one class loader with each other.
     * This is recommended when running templates in production.
     * How to precompile templates: https://github.com/casid/jte/blob/master/DOCUMENTATION.md#precompiling-templates
     *
     * @param classDirectory where template class files are located
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine createPrecompiled(Path classDirectory) {
        return new TemplateEngine(null, classDirectory, TemplateMode.Precompiled);
    }

    private TemplateEngine(CodeResolver codeResolver, Path classDirectory, TemplateMode templateMode) {
        this.compiler = new TemplateCompiler(codeResolver, classDirectory, templateMode);
        this.templateMode = templateMode;
        this.templateCache = new ConcurrentHashMap<>();

        if (templateMode == TemplateMode.OnDemand) {
            cleanAll();
        }
    }

    /**
     * Renders the template with the given name.
     * @param name the template name relative to the specified root directory, for instance "pages/welcome.jte".
     * @param model the model instance passed to the template.
     * @param output any implementation of {@link TemplateOutput}, where the template will be written to.
     * @throws TemplateException in case the template failed to render, containing information where the error happened.
     */
    public void render(String name, Object model, TemplateOutput output) throws TemplateException {
        Template template = resolveTemplate(name);
        try {
            template.render(output, model);
        } catch (Exception e) {
            DebugInfo debugInfo = compiler.resolveDebugInfo(template.getClass().getClassLoader(), e.getStackTrace());
            String message = "Failed to render " + name;
            if (debugInfo != null) {
                message += ", error at " + debugInfo.name + ":" + debugInfo.line;
            }
            throw new TemplateException(message, e);
        }
    }

    public List<String> getTemplatesUsing(String name) {
        if (name.startsWith(TemplateCompiler.TAG_DIRECTORY) || name.startsWith(TemplateCompiler.LAYOUT_DIRECTORY)) {
            return compiler.getTemplatesUsing(name);
        } else {
            return Collections.singletonList(name);
        }
    }

    /**
     * Prepares the template with the given name for rendering
     * @param name the template name relative to the specified root directory, for instance "pages/welcome.jte".
     */
    public void prepareForRendering(String name) {
        resolveTemplate(name);
    }

    /**
     * Cleans the directory containing the compiled template classes.
     */
    public void cleanAll() {
        compiler.cleanAll();
    }

    /**
     * Compiles all templates located in the sources directory, to the compiled template classes directory.
     */
    public void precompileAll() {
        precompileAll(null);
    }

    /**
     * Compiles all templates located in the sources directory, to the compiled template classes directory.
     * @param compilePath additional compile path arguments for the Java compiler.
     */
    public void precompileAll(List<String> compilePath) {
        compiler.precompileAll(compilePath);
    }

    private Template resolveTemplate(String name) {
        if (templateMode == TemplateMode.OnDemand && compiler.hasChanged(name)) {
            synchronized (templateCache) {
                if (compiler.hasChanged(name)) {
                    Template<?> template = compiler.compile(name);
                    templateCache.put(name, template);
                    return template;
                }
            }
        }
        return templateCache.computeIfAbsent(name, compiler::compile);
    }

    /**
     * Experimental mode, that ignores any {@link NullPointerException} that occurs in template files.
     * @param value true, to enable
     */
    public void setNullSafeTemplateCode(boolean value) {
        compiler.setNullSafeTemplateCode(value);
    }
}
