package gg.jte;

import gg.jte.html.HtmlPolicy;
import gg.jte.html.HtmlTemplateOutput;
import gg.jte.html.OwaspHtmlTemplateOutput;
import gg.jte.html.HtmlInterceptor;
import gg.jte.runtime.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private final TemplateConfig config;
    private final ContentType contentType;
    private final Path classDirectory;
    private final ClassLoader parentClassLoader;

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
        return create(codeResolver, Paths.get("jte-classes"), contentType);
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
        return create(codeResolver, classDirectory, contentType, null);
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
     * @param parentClassLoader the parent classloader to use, or null to use the application class loader as parent
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine create(CodeResolver codeResolver, Path classDirectory, ContentType contentType, ClassLoader parentClassLoader) {
        return create(codeResolver, classDirectory, contentType, parentClassLoader, Constants.PACKAGE_NAME_ON_DEMAND);
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
     * @param parentClassLoader the parent classloader to use, or null to use the application class loader as parent
     * @param packageName the package name, where template classes are generated to
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine create(CodeResolver codeResolver, Path classDirectory, ContentType contentType, ClassLoader parentClassLoader, String packageName) {
        return new TemplateEngine(codeResolver, classDirectory, contentType, TemplateMode.OnDemand, parentClassLoader, packageName);
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
        return createPrecompiled(classDirectory, contentType, null);
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
     * @param parentClassLoader the parent classloader to use, or null to use the application class loader as parent (only has an effect if classDirectory is not null)
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine createPrecompiled(Path classDirectory, ContentType contentType, ClassLoader parentClassLoader) {
        return createPrecompiled(classDirectory, contentType, parentClassLoader, Constants.PACKAGE_NAME_PRECOMPILED);
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
     * @param parentClassLoader the parent classloader to use, or null to use the application class loader as parent (only has an effect if classDirectory is not null)
     * @param packageName the package name, where template classes are generated to
     * @return a fresh TemplateEngine instance
     */
    public static TemplateEngine createPrecompiled(Path classDirectory, ContentType contentType, ClassLoader parentClassLoader, String packageName) {
        return new TemplateEngine(null, classDirectory, contentType, TemplateMode.Precompiled, parentClassLoader, packageName);
    }

    private TemplateEngine(CodeResolver codeResolver, Path classDirectory, ContentType contentType, TemplateMode templateMode, ClassLoader parentClassLoader, String packageName) {
        if (contentType == null) {
            throw new NullPointerException("Content type must be specified.");
        }

        this.config = new TemplateConfig(contentType, packageName);

        this.templateLoader = createTemplateLoader(config, codeResolver, classDirectory, templateMode, parentClassLoader);
        this.templateMode = templateMode;
        this.templateCache = new ConcurrentHashMap<>();
        this.contentType = contentType;
        this.classDirectory = classDirectory;
        this.parentClassLoader = parentClassLoader;

        if (templateMode == TemplateMode.OnDemand) {
            cleanAll();
        }
    }

    private static TemplateLoader createTemplateLoader(TemplateConfig config, CodeResolver codeResolver, Path classDirectory, TemplateMode templateMode, ClassLoader parentClassLoader) {
        if (templateMode == TemplateMode.Precompiled) {
            return new RuntimeTemplateLoader(classDirectory, parentClassLoader, config.packageName);
        } else {
            try {
                Class<?> compilerClass = Class.forName("gg.jte.compiler.TemplateCompiler");
                return (TemplateLoader)compilerClass.getConstructor(TemplateConfig.class, CodeResolver.class, Path.class, ClassLoader.class).newInstance(config, codeResolver, classDirectory, parentClassLoader);
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
            throw handleRenderException(name, template, e);
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
            throw handleRenderException(name, template, e);
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
            throw handleRenderException(name, template, e);
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

    private TemplateException handleRenderException(String name, Template template, Throwable e) {
        if (e instanceof TemplateException) {
            return (TemplateException)e;
        }

        ClassLoader classLoader = template.getClassLoader();
        StackTraceElement[] stackTrace = e.getStackTrace();

        DebugInfo debugInfo = templateLoader.resolveDebugInfo(classLoader, stackTrace);
        String message = "Failed to render " + name;
        if (debugInfo != null) {
            message += ", error at " + debugInfo.name + ":" + debugInfo.line;

            templateLoader.rewriteStackTrace(e, classLoader, stackTrace);
        }

        return new TemplateException(message, e);
    }

    public boolean hasTemplate(String name) {
        try {
            resolveTemplate(name);
            return true;
        } catch (TemplateNotFoundException e) {
            return false;
        }
    }

    public List<String> getTemplatesUsing(String name) {
        if (name.startsWith(Constants.TAG_DIRECTORY) || name.startsWith(Constants.LAYOUT_DIRECTORY)) {
            return templateLoader.getTemplatesUsing(name);
        } else {
            return Collections.singletonList(name);
        }
    }

    /**
     * Obtain parameter information about a specific template.
     * @param name the template name relative to the specified root directory, for instance "tag/example.jte".
     * @return a map containing all template parameters names and their classes
     * @throws TemplateException in case parameter information is not available (jte classes must be compiled with -parameters compiler flag.)
     */
    public Map<String, Class<?>> getParamInfo(String name) throws TemplateException {
        return resolveTemplate(name).getParamInfo();
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
     * @return list of .java template files that were generated
     */
    public List<String> generateAll() {
        return templateLoader.generateAll();
    }

    /**
     * Compiles all templates located in the sources directory, to the compiled template classes directory.
     * @return list of .java template files that were compiled
     */
    public List<String> precompileAll() {
        return templateLoader.precompileAll();
    }

    /**
     * Compiles all templates located in the sources directory, to the compiled template classes directory.
     * @param classPath additional class path arguments for the Java compiler. See also {@link TemplateEngine#setClassPath(List)}
     * @return list of .java template files that were compiled
     */
    public List<String> precompileAll(List<String> classPath) {
        setClassPath(classPath);
        return templateLoader.precompileAll();
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
     * Useful, if this engine is in precompiled mode (probably production) but you still want to be able to apply a hotfix without deployment.
     * This template engine will be entirely unaffected by this call. Instead, a fresh template engine will be created.
     * When this call succeeds, you can safely switch the template engine reference to the new instance. The old instance including all
     * old templates should then be subject to garbage collection.
     *
     * This only works if you're running on the JDK and if templates have their own classloader.
     *
     * @param precompiler a template engine that is configured exactly as you usually would precompile your templates.
     * @return a fresh template engine with a warmed up cache.
     * @throws TemplateException in case there was a compilation error, in this case you should keep the current engine running!
     */
    public TemplateEngine reloadPrecompiled(TemplateEngine precompiler) throws TemplateException {
        precompiler.precompileAll();
        return reloadPrecompiled(precompiler.classDirectory);
    }

    /**
     * Useful, if this engine is in precompiled mode (probably production) but you still want to be able to apply a hotfix without deployment.
     * This template engine will be entirely unaffected by this call. Instead, a fresh template engine will be created.
     * When this call succeeds, you can safely switch the template engine reference to the new instance. The old instance including all
     * old templates should then be subject to garbage collection.
     *
     * Basically you could recompile all templates on your build server, upload them to your production server
     * and call this method afterwards.
     *
     * This only works if templates have their own classloader.
     *
     * @param classDirectory the class directory to load the new templates from.
     * @return a fresh template engine with a warmed up cache.
     * @throws TemplateException in case there was an error during class loading, in this case you should keep the current engine running!
     */
    public TemplateEngine reloadPrecompiled(Path classDirectory) throws TemplateException {
        TemplateEngine engine = createPrecompiled(classDirectory, contentType, parentClassLoader, config.packageName);
        engine.setHtmlInterceptor(htmlInterceptor);

        Set<String> templates = new HashSet<>(templateCache.keySet());
        for (String templateName : templates) {
            engine.prepareForRendering(templateName);
        }

        return engine;
    }

    /**
     * Sets additional compiler arguments for jte templates.
     * @param compileArgs for instance templateEngine.setCompileArgs("--enable-preview", "--release", "" + Runtime.version().feature());
     */
    public void setCompileArgs(String ... compileArgs) {
        config.compileArgs = compileArgs;
    }

    /**
     * Experimental mode, that trims control structures, resulting in prettier output.
     * @param value true, to enable
     */
    public void setTrimControlStructures(boolean value) {
        config.trimControlStructures = value;
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
        config.htmlPolicy = htmlPolicy;
    }

    /**
     * Intercepts the given html tags during template compilation
     * and calls the configured htmlInterceptor during template rendering.
     * @param htmlTags tags to be intercepted, for instance setHtmlTags("form", "input");
     */
    public void setHtmlTags(String ... htmlTags) {
        config.htmlTags = htmlTags;
    }

    /**
     * Intercepts the given html attributes for configured htmlTags
     * during template compilation and calls the configured htmlInterceptor during template
     * rendering.
     * @param htmlAttributes attributes to be intercepted, for instance setHtmlAttributes("class");
     */
    public void setHtmlAttributes(String ... htmlAttributes) {
        config.htmlAttributes = htmlAttributes;
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

    /**
     * By default, jte omits all HMTL/CSS/JS comments, when compiling with {@link ContentType#Html}.
     * If you don't want this behavior, you can disable it here.
     * @param htmlCommentsPreserved true, to preserve HTML comments in templates
     */
    public void setHtmlCommentsPreserved(boolean htmlCommentsPreserved) {
        config.htmlCommentsPreserved = htmlCommentsPreserved;
    }

    /**
     * Experimental setting, that UTF-8 encodes all static template parts.
     *
     * @param binaryStaticContent true, to pre-generate UTF-8 encoded byte arrays for all static template parts
     */
    public void setBinaryStaticContent(boolean binaryStaticContent) {
        config.binaryStaticContent = binaryStaticContent;
    }

    /**
     * The class path used for compiling templates.
     * @param classPath list of elements on the class path
     */
    public void setClassPath(List<String> classPath) {
        config.classPath = classPath;
    }

    /**
     * Directory in which to generate non-java files (resources). Typically set by plugin rather than end user.
     * Optional - if null, resources will not be generated
     * @param targetResourceDirectory directory to generate resources in
     */
    public void setTargetResourceDirectory(Path targetResourceDirectory) {
        config.resourceDirectory = targetResourceDirectory;
    }

    /**
     * "group/artifact" of the project using jte. Typically set by plugin rather than end user.
     * If null, the compiler will make one up.
     * @param projectNamespace "groupId/artifactId"
     */
    public void setProjectNamespace(String projectNamespace) {
        config.projectNamespace = projectNamespace;
    }

    /**
     * Whether to try and generate configuration files to support Graal native-image.
     * If true, setTargetResourceDirectory should also be used to indicate where to
     * put the resources.
     * @param value true to generate native image configuration resources
     */
    public void setGenerateNativeImageResources(boolean value) {
        config.generateNativeImageResources = value;
    }
}
