package gg.jte.runtime;

import gg.jte.TemplateException;
import gg.jte.TemplateOutput;
import gg.jte.html.HtmlInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Template {
    private final String name;
    private final TemplateType type;
    private final Class<?> clazz;
    private final int parameterCount;
    private Method render;
    private Method renderMap;
    private Map<String, Class<?>> parameterInfo;

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
        } catch (IllegalArgumentException e) {
            String expectedType = render.getParameterTypes()[2].getName();
            String actualType = param != null ? param.getClass().getName() : null;
            throw new TemplateException("Failed to render " + name + ", type mismatch for parameter: Expected " + expectedType + ", got " + actualType, e);
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

    public Map<String, Class<?>> getParamInfo() {
        if (parameterInfo == null) {
            parameterInfo = calculateParameterInfo();
        }
        return parameterInfo;
    }

    private Map<String, Class<?>> calculateParameterInfo() {
        Map<String, Class<?>> result = new HashMap<>();

        Parameter[] parameters = render.getParameters();
        for (int i = 2; i < parameters.length; ++i) {
            if (!parameters[i].isNamePresent()) {
                throw new TemplateException("No parameter information is available for " + name + ", compile templates with -parameters flag, to use this method.");
            }
            result.put(parameters[i].getName(), parameters[i].getType());
        }

        return Collections.unmodifiableMap(result);
    }
}
