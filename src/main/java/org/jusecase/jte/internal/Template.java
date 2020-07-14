package org.jusecase.jte.internal;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.support.HtmlTagSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public final class Template {
    private final String name;
    private final TemplateType type;
    private final Class<?> clazz;
    private final int parameterCount;
    private Method render;
    private Method renderMap;

    public Template(String name, TemplateType type, Class<?> clazz) {
        this.name = name;
        this.type = type;
        this.clazz = clazz;
        findRenderMethods(clazz);
        parameterCount = resolveParameterCount();
    }

    public void render(TemplateOutput output, HtmlTagSupport htmlTagSupport, Object param) throws Throwable {
        try {
            if (parameterCount == 0) {
                render.invoke(null, output, htmlTagSupport);
            } else {
                render.invoke(null, output, htmlTagSupport, param);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("unchecked")
    public void renderMap(TemplateOutput output, HtmlTagSupport htmlTagSupport, Map<String, Object> params) throws Throwable {
        try {
            if (type == TemplateType.Layout) {
                Map<String, String> layoutDefinitions = (Map<String, String>) params.get(TemplateCompiler.LAYOUT_DEFINITIONS_PARAM);

                renderMap.invoke(null, output, htmlTagSupport, (Function<String, Runnable>) definitionName -> () -> {
                    String layoutDefinition = layoutDefinitions.get(definitionName);
                    if (layoutDefinition != null) {
                        output.writeContent(layoutDefinition);
                    }
                }, params);
            } else {
                renderMap.invoke(null, output, htmlTagSupport, params);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }

    private void findRenderMethods(Class<?> clazz) {
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if ("render".equals(declaredMethod.getName())) {
                render = declaredMethod;
            } else if ("renderMap".equals(declaredMethod.getName())) {
                renderMap = declaredMethod;
            }
        }

        if (render == null) {
            throw new IllegalStateException("Failed to init " + type + " " + name + ", no method named 'render' found in " + clazz);
        }

        if (renderMap == null) {
            throw new IllegalStateException("Failed to init " + type + " " + name + ", no method named 'renderMap' found in " + clazz);
        }
    }

    private int resolveParameterCount() {
        if (type == TemplateType.Layout) {
            return render.getParameterCount() - 3;
        } else {
            return render.getParameterCount() - 2;
        }
    }
}
