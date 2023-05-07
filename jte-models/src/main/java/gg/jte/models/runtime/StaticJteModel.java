package gg.jte.models.runtime;

import gg.jte.ContentType;
import gg.jte.TemplateException;
import gg.jte.TemplateOutput;
import gg.jte.html.HtmlInterceptor;
import gg.jte.html.OwaspHtmlTemplateOutput;
import gg.jte.output.WriterOutput;
import gg.jte.runtime.ClassInfo;
import gg.jte.runtime.DebugInfo;
import gg.jte.runtime.Template;
import gg.jte.runtime.TemplateLoader;

import java.io.Writer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StaticJteModel<OUTPUT extends TemplateOutput> implements JteModel {

    ContentType contentType;
    BiConsumer<OUTPUT, HtmlInterceptor> renderer;
    StaticTemplateLoader loader;

    public StaticJteModel(ContentType contentType, BiConsumer<OUTPUT, HtmlInterceptor> renderer,
                          String name, String packageName, int[] lineInfo) {
        this.contentType = contentType;
        this.renderer = renderer;
        this.loader = new StaticTemplateLoader(name, packageName, lineInfo);
    }

    @Override
    public void render(Writer writer) {
        try {
            renderer.accept(
                    getOutput(writer),
                    null
            );
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private OUTPUT getOutput(Writer writer) {
        if (contentType == ContentType.Html) {
            return (OUTPUT) new OwaspHtmlTemplateOutput(new WriterOutput(writer));
        } else {
            return (OUTPUT) new WriterOutput(writer);
        }
    }

    private TemplateException handleException(Exception e) {
        if (e instanceof TemplateException) {
            return (TemplateException) e;
        }
        StackTraceElement[] stackTrace = e.getStackTrace();

        DebugInfo debugInfo = loader.resolveDebugInfo(null, stackTrace);
        String message = "Failed to render " + loader.name;
        if (debugInfo != null) {
            message += ", error at " + debugInfo.name + ":" + debugInfo.line;

            loader.rewriteStackTrace(e, null, stackTrace);
        }

        return new TemplateException(message, e);
    }

    @Override
    public void writeTo(TemplateOutput output) {
        renderer.accept(getOutput(output), null);
    }

    @SuppressWarnings("unchecked")
    private OUTPUT getOutput(TemplateOutput output) {
        if (contentType == ContentType.Html) {
            return (OUTPUT) new OwaspHtmlTemplateOutput(output);
        }
        return (OUTPUT) output;
    }

    // TemplateLoader is extended just to re-use exception handling helpers
    private static class StaticTemplateLoader extends TemplateLoader {

        private final String name;
        private final ClassInfo classInfo;

        protected StaticTemplateLoader(String name, String packageName, int[] lineInfo) {
            super(null, packageName);

            this.name = name;
            this.classInfo = new ClassInfo(name, packageName);
            classInfo.lineInfo = lineInfo;

        }

        @Override
        public Template hotReload(String name) {
            throw new UnsupportedOperationException("hotReload");
        }

        @Override
        protected ClassInfo getClassInfo(ClassLoader ignored, String className) {
            if (classInfo.fullName.equals(className))
            {
                return classInfo;
            }
            return null;
        }

        @Override
        protected ClassLoader getClassLoader() {
            throw new UnsupportedOperationException("getClassLoader");
        }

        @Override
        public List<String> getTemplatesUsing(String name) {
            throw new UnsupportedOperationException("getTemplatesUsing");
        }

        @Override
        public void cleanAll() {

        }

        @Override
        public List<String> generateAll() {
            throw new UnsupportedOperationException("generateAll");
        }

        @Override
        public List<String> precompileAll() {
            throw new UnsupportedOperationException("precompileAll");
        }

        @Override
        public boolean hasChanged(String name) {
            return false;
        }
    }
}
