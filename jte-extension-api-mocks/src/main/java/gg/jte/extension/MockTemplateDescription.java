package gg.jte.extension;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockTemplateDescription implements TemplateDescription {
    String name;
    String packageName;
    String className;
    List<ParamDescription> params = new ArrayList<>();
    private List<String> imports = new ArrayList<>();

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
}
