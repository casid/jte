package gg.jte.compiler.kotlin;

import gg.jte.compiler.ParamInfo;
import gg.jte.compiler.TemplateParserVisitor;
import gg.jte.compiler.TemplateParserVisitorAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestKotlinParamInfo {

    @Test
    public void failWhenParaHasNoType() {
        final Value<String> value = new Value<>();
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter() {
            @Override
            public void onError(String message) {
                value.set(message);
            }
        };

        KotlinParamInfo.parse("name", visitor, 1);
        Assertions.assertEquals("Missing parameter type: '@param name'", value.get());
    }

    @Test
    public void parseParamWithType() {
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter();
        ParamInfo paramInfo = KotlinParamInfo.parse("name: String", visitor, 1);
        Assertions.assertEquals("name", paramInfo.name);
        Assertions.assertEquals("String", paramInfo.type);
        Assertions.assertEquals(1, paramInfo.templateLine);
    }

    @Test
    public void parseParamWithGenericsType() {
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter();
        ParamInfo paramInfo = KotlinParamInfo.parse("values: Map<String, List<Pair<String, Int>>>", visitor, 1);
        Assertions.assertEquals("values", paramInfo.name);
        Assertions.assertEquals("Map<String, List<Pair<String, Int>>>", paramInfo.type);
        Assertions.assertEquals(1, paramInfo.templateLine);
    }

    @Test
    public void parseVarargParam() {
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter();
        ParamInfo paramInfo = KotlinParamInfo.parse("vararg names: String", visitor, 1);

        Assertions.assertEquals("names", paramInfo.name);
        Assertions.assertEquals("String", paramInfo.type);
        Assertions.assertTrue(paramInfo.varargs, "expect to parse as vararg param");
        Assertions.assertEquals(1, paramInfo.templateLine);
    }

    @Test
    public void parseParamWithDefaultValue() {
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter();
        ParamInfo paramInfo = KotlinParamInfo.parse("name: String = \"jte\"", visitor, 1);
        Assertions.assertEquals("name", paramInfo.name);
        Assertions.assertEquals("String", paramInfo.type);
        Assertions.assertEquals("\"jte\"", paramInfo.defaultValue);
        Assertions.assertEquals(1, paramInfo.templateLine);
    }

    @Test
    public void parseParamWithTypeContent() {
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter();
        ParamInfo paramInfo = KotlinParamInfo.parse("content: gg.jte.Content = @`Something`", visitor, 1);
        Assertions.assertEquals("content", paramInfo.name);
        Assertions.assertEquals("gg.jte.Content", paramInfo.type);
        Assertions.assertEquals("@`Something`", paramInfo.defaultValue);
        Assertions.assertEquals(1, paramInfo.templateLine);
    }

    @Test
    public void parseParamWithComplexDefaultValues() {
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter();
        ParamInfo paramInfo = KotlinParamInfo.parse("numbers: List<Int> = listOf(1, 2, 3, 4)", visitor, 1);
        Assertions.assertEquals("numbers", paramInfo.name);
        Assertions.assertEquals("List<Int>", paramInfo.type);
        Assertions.assertEquals("listOf(1, 2, 3, 4)", paramInfo.defaultValue);
        Assertions.assertEquals(1, paramInfo.templateLine);
    }

    @Test
    public void stripEmptySpacesWhenParsingParamDefaultValue() {
        TemplateParserVisitor visitor = new TemplateParserVisitorAdapter();
        ParamInfo paramInfo = KotlinParamInfo.parse("name: String =      \"jte\"      ", visitor, 1);
        Assertions.assertEquals("name", paramInfo.name);
        Assertions.assertEquals("String", paramInfo.type);
        Assertions.assertEquals("\"jte\"", paramInfo.defaultValue);
        Assertions.assertEquals(1, paramInfo.templateLine);
    }

    private static class Value<T> {
        private T value;

        public void set(T value) {
            this.value = value;
        }

        public T get() { return this.value; }
    }
}
