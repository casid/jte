package gg.jte.extension.api.mocks;

import gg.jte.extension.api.ParamDescription;
import gg.jte.extension.api.TemplateDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Mock implementation to help with testing extensions.
 */
public class MockTemplateDescription implements TemplateDescription {
    String name;
    String packageName;
    String className;
    List<ParamDescription> params = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();

    public static MockTemplateDescription mockTemplateDescription() {
        return new MockTemplateDescription();
    }

    public MockTemplateDescription name(String value) {
        name = value;
        return this;
    }

    public MockTemplateDescription packageName(String value) {
        packageName = value;
        return this;
    }

    public MockTemplateDescription className(String value) {
        className = value;
        return this;
    }

    public MockTemplateDescription params(List<ParamDescription> value) {
        params.clear();
        params.addAll(value);
        return this;
    }

    public MockTemplateDescription addParams(ParamDescription... value) {
        params.addAll(Arrays.asList(value));
        return this;
    }

    public MockTemplateDescription imports(List<String> value) {
        imports.clear();
        imports.addAll(value);
        return this;
    }

    public MockTemplateDescription addImports(String... value) {
        imports.addAll(Arrays.asList(value));
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String packageName() {
        return packageName;
    }

    @Override
    public String className() {
        return className;
    }

    @Override
    public List<ParamDescription> params() {
        return params;
    }

    @Override
    public List<String> imports() {
        return imports;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockTemplateDescription that = (MockTemplateDescription) o;

        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(packageName, that.packageName)) return false;
        return Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, packageName, className);
    }
}
