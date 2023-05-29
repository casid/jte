package gg.jte.extension.api.mocks;

import gg.jte.extension.api.ParamDescription;

/**
 * Mock implementation to help with testing extensions.
 */
public class MockParamDescription implements ParamDescription {
    String type;
    String name;
    String defaultValue;

    public static MockParamDescription mockParamDescription() {
        return new MockParamDescription();
    }

    public MockParamDescription type(String value) {
        type = value;
        return this;
    }

    public MockParamDescription name(String value) {
        name = value;
        return this;
    }

    public MockParamDescription defaultValue(String value) {
        defaultValue = value;
        return this;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }
}
