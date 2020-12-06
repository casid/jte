package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngine_PrecompiledReloadableTest {

    DummyCodeResolver codeResolver = new DummyCodeResolver();
    Path classDirectory = Path.of("jte-classes");
    ContentType contentType = ContentType.Html;

    TemplateEngine precompiler;
    TemplateEngine templateEngine;


    @BeforeEach
    void setUp() {
        codeResolver.givenCode("hello.jte" , "Hello Word!");

        // Our CI did this (precompiled all templates)
        precompiler = TemplateEngine.create(codeResolver, classDirectory, contentType);
        precompiler.precompileAll();

        // We start the app with a precompiled engine
        templateEngine = TemplateEngine.createPrecompiled(classDirectory, contentType);
    }

    @Test
    void hotfix() {
        thenOutputIs("Hello Word!");

        // We fix an annoying bug
        codeResolver.givenCode("hello.jte" , "Hello World!");

        // Recompile all templates and create a fresh engine. The old engine will be garbage collected..
        templateEngine = templateEngine.reloadPrecompiled(precompiler);

        // Now, the correct output is rendered
        thenOutputIs("Hello World!");
    }

    @Test
    void compileError() {
        thenOutputIs("Hello Word!");

        // We fix an annoying bug, but make a mistake
        codeResolver.givenCode("hello.jte" , "Hello ${unknown}.");

        Exception exception = null;
        try {
            templateEngine = templateEngine.reloadPrecompiled(precompiler);
        } catch (Exception e) {
            exception = e;
        }
        assertThat(exception).isNotNull().isInstanceOf(TemplateException.class); // Exception is thrown and can be handled ...

        // ... but the old engine is still functional and renders all templates as it did before.
        thenOutputIs("Hello Word!");
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render("hello.jte", null, output);
        assertThat(output.toString()).isEqualTo(expected);
    }
}