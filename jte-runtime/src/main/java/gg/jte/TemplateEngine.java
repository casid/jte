package gg.jte;

import gg.jte.html.HtmlPolicy;
import gg.jte.internal.*;
import gg.jte.html.HtmlTemplateOutput;
import gg.jte.html.OwaspHtmlTemplateOutput;
import gg.jte.html.HtmlInterceptor;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
public final class TemplateEngine {
    private final TemplateLoader templateLoader;
    private final TemplateMode templateMode;
    private final ConcurrentMap<String, Template> templateCache;
    private final ContentType contentType;

    private HtmlInterceptor htmlInterceptor;

    /**
     * Creates a new template engine.
     * All templates are compiled to Java class files on demand.
     * A JDK is required.
     * Every template has its own class loader.
     * This is recommended when running templates on your developer machine.
     *
     * @param codeResolver to lookup jte templates
     * @param contentType the content type of all templates this engine manages
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine create(CodeResolver codeResolver, ContentType contentType) {
        return create(codeResolver, Path.of("jte-classes"), contentType);
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
     * @param contentType the content type of all templates this engine manages
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine create(CodeResolver codeResolver, Path classDirectory, ContentType contentType) {
        return new TemplateEngine(codeResolver, classDirectory, contentType, TemplateMode.OnDemand);
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
     * @param contentType the content type of all templates this engine manages
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine createPrecompiled(Path classDirectory, ContentType contentType) {
        return new TemplateEngine(null, classDirectory, contentType, TemplateMode.Precompiled);
    }

    /**
     * Creates a new template engine.
     * All templates must have been precompiled to Java class files already.
     * The template engine will load them via the application class loader.
     * This means all template classes must be bundled in you application JAR file.
     * No JDK is required.
     * This is recommended when running templates in production.
     * How to precompile templates: https://github.com/casid/jte/blob/master/DOCUMENTATION.md#precompiling-templates
     *
     * @param contentType the content type of all templates this engine manages
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine createPrecompiled(ContentType contentType) {
        return createPrecompiled(null, contentType);
    }

    private TemplateEngine(CodeResolver codeResolver, Path classDirectory, ContentType contentType, TemplateMode templateMode) {
        if (contentType == null) {
            throw new NullPointerException("Content type must be specified.");
        }
        
        this.templateLoader = createTemplateLoader(codeResolver, classDirectory, contentType, templateMode);
        this.templateMode = templateMode;
        this.templateCache = new ConcurrentHashMap<>();
        this.contentType = contentType;

        if (templateMode == TemplateMode.OnDemand) {
            cleanAll();
        }
    }

    private static TemplateLoader createTemplateLoader(CodeResolver codeResolver, Path classDirectory, ContentType contentType, TemplateMode templateMode) {
        if (templateMode == TemplateMode.Precompiled) {
            return new RuntimeTemplateLoader(classDirectory);
        } else {
            try {
                Class<?> compilerClass = Class.forName("gg.jte.internal.TemplateCompiler");
                return (TemplateLoader)compilerClass.getConstructor(CodeResolver.class, Path.class, ContentType.class).newInstance(codeResolver, classDirectory, contentType);
            } catch (Exception e) {
                throw new TemplateException("TemplateCompiler could not be located. Maybe jte isn't on your classpath?", e);
            }
        }
    }

    /**
     * Renders the template with the given name.
     * It is preferred to use this method, if all your templates have exactly one parameter.
     * @param name the template name relative to the specified root directory, for instance "pages/welcome.jte".
     * @param param the param passed to the template.
     * @param output any implementation of {@link TemplateOutput}, where the template will be written to.
     * @throws TemplateException in case the template failed to render, containing information where the error happened.
     */
    public void render(String name, Object param, TemplateOutput output) throws TemplateException {
        Template template = resolveTemplate(name);
        try {
            template.render(checkOutput(output), htmlInterceptor, param);
        } catch (Throwable e) {
            handleRenderException(name, template, e);
        }
    }

    /**
     * Renders the template with the given name.
     * Parameters in the params map are mapped to the corresponding parameters in the template.
     * Template parameters with a default value don't have to be provided in the map.
     * @param name the template name relative to the specified root directory, for instance "pages/welcome.jte".
     * @param params the parameters passed to the template as key value pairs.
     * @param output any implementation of {@link TemplateOutput}, where the template will be written to.
     * @throws TemplateException in case the template failed to render, containing information where the error happened.
     */
    public void render(String name, Map<String, Object> params, TemplateOutput output) throws TemplateException {
        Template template = resolveTemplate(name);
        try {
            template.renderMap(checkOutput(output), htmlInterceptor, params);
        } catch (Throwable e) {
            handleRenderException(name, template, e);
        }
    }

    /**
     * Renders a tag with the given name.
     * This comes at the cost of an extra method invocation and losing the type safety for params that jte usually provides.
     * However, this is useful while migrating to jte.
     * For instance, you can port a JSP tag to a jte tag and invoke the new jte tag from all other JSPs,
     * so that there are no redundant implementations during the migration.
     * @param name the template name relative to the specified root directory, for instance "tag/myTag.jte".
     * @param params map of parameters that should be passed to the tag.
     * @param output any implementation of {@link TemplateOutput}, where the template will be written to.
     * @throws TemplateException in case the tag failed to render, containing information where the error happened.
     */
    public void renderTag(String name, Map<String, Object> params, TemplateOutput output) throws TemplateException {
        Template template = resolveTemplate(name);
        try {
            template.renderMap(checkOutput(output), htmlInterceptor, params);
        } catch (Throwable e) {
            handleRenderException(name, template, e);
        }
    }

    /**
     * Renders a layout with the given name.
     * This comes at the cost of an extra method invocation and losing the type safety for params that jte usually provides.
     * However, this is useful while migrating to jte.
     * For instance, you can port a JSP layout to a jte layout and invoke the new jte layout from all other JSPs,
     * so that there are no redundant implementations during the migration.
     * @param name the template name relative to the specified root directory, for instance "layout/myLayout.jte".
     * @param params map of parameters that should be passed to the layout.
     * @param output any implementation of {@link TemplateOutput}, where the template will be written to.
     * @throws TemplateException in case the layout failed to render, containing information where the error happened.
     */
    public void renderLayout(String name, Map<String, Object> params, TemplateOutput output) throws TemplateException {
        renderTag(name, params, output);
    }

    private TemplateOutput checkOutput(TemplateOutput templateOutput) {
        if (contentType == ContentType.Html && !(templateOutput instanceof HtmlTemplateOutput)) {
            return new OwaspHtmlTemplateOutput(templateOutput);
        }
        return templateOutput;
    }

    private void handleRenderException(String name, Template template, Throwable e) {
        DebugInfo debugInfo = templateLoader.resolveDebugInfo(template.getClassLoader(), e.getStackTrace());
        String message = "Failed to render " + name;
        if (debugInfo != null) {
            message += ", error at " + debugInfo.name + ":" + debugInfo.line;
        }
        throw new TemplateException(message, e);
    }

    public List<String> getTemplatesUsing(String name) {
        if (name.startsWith(Constants.TAG_DIRECTORY) || name.startsWith(Constants.LAYOUT_DIRECTORY)) {
            return templateLoader.getTemplatesUsing(name);
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
        templateLoader.cleanAll();
    }

    /**
     * Generates all template classes in the sources directory, to the compiled template classes directory.
     * This only generates .java files, not .class files.
     * @return amount of templates that were generated
     */
    public int generateAll() {
        return templateLoader.generateAll();
    }

    /**
     * Compiles all templates located in the sources directory, to the compiled template classes directory.
     * @return amount of templates that were compiled
     */
    public int precompileAll() {
        return precompileAll(null);
    }

    /**
     * Compiles all templates located in the sources directory, to the compiled template classes directory.
     * @param compilePath additional compile path arguments for the Java compiler.
     * @return amount of templates that were compiled
     */
    public int precompileAll(List<String> compilePath) {
        return templateLoader.precompileAll(compilePath);
    }

    private Template resolveTemplate(String name) {
        if (templateMode == TemplateMode.OnDemand && templateLoader.hasChanged(name)) {
            synchronized (templateCache) {
                if (templateLoader.hasChanged(name)) {
                    Template template = templateLoader.load(name);
                    templateCache.put(name, template);
                    return template;
                }
            }
        }
        return templateCache.computeIfAbsent(name, templateLoader::load);
    }

    /**
     * Experimental mode, that ignores any {@link NullPointerException} that occurs in template files.
     * @param value true, to enable
     */
    public void setNullSafeTemplateCode(boolean value) {
        templateLoader.setNullSafeTemplateCode(value);
    }

    /**
     * Policy that checks the parsed HTML at compile time.
     * @param htmlPolicy the policy
     * @throws NullPointerException if policy is null
     */
    public void setHtmlPolicy(HtmlPolicy htmlPolicy) {
        if (htmlPolicy == null) {
            throw new NullPointerException("htmlPolicy must not be null");
        }
        templateLoader.setHtmlPolicy(htmlPolicy);
    }

    /**
     * Intercepts the given html tags during template compilation
     * and calls the configured htmlInterceptor during template rendering.
     * @param htmlTags tags to be intercepted, for instance setHtmlTags("form", "input");
     */
    public void setHtmlTags(String ... htmlTags) {
        templateLoader.setHtmlTags(htmlTags);
    }

    /**
     * Intercepts the given html attributes for configured htmlTags
     * during template compilation and calls the configured htmlInterceptor during template
     * rendering.
     * @param htmlAttributes attributes to be intercepted, for instance setHtmlAttributes("class");
     */
    public void setHtmlAttributes(String ... htmlAttributes) {
        templateLoader.setHtmlAttributes(htmlAttributes);
    }

    /**
     * Interceptor that is called during template rendering when one of the
     * configured htmlTags is rendered.
     * This allows to integrate existing frameworks into jte.
     * @param htmlInterceptor the interceptor
     */
    public void setHtmlInterceptor(HtmlInterceptor htmlInterceptor) {
        this.htmlInterceptor = htmlInterceptor;
    }
}
