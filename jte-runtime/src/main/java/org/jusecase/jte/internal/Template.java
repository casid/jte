package org.jusecase.jte.internal;

import org.jusecase.jte.TemplateOutput;
import org.jusecase.jte.html.HtmlInterceptor;

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

    public void render(TemplateOutput output, HtmlInterceptor htmlInterceptor, Object param) throws Throwable {
        try {
            if (parameterCount == 0) {
                render.invoke(null, output, htmlInterceptor);
            } else {
                render.invoke(null, output, htmlInterceptor, param);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public void renderMap(TemplateOutput output, HtmlInterceptor htmlInterceptor, Map<String, Object> params) throws Throwable {
        try {
            renderMap.invoke(null, output, htmlInterceptor, params);
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
