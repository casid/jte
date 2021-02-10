package gg.jte;

import gg.jte.output.StringOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateEngine_OnDemandReloadableTest {

    DummyCodeResolver codeResolver = new DummyCodeResolver();
    Path classDirectory = Paths.get("jte-classes");
    ContentType contentType = ContentType.Html;

    TemplateEngine templateEngine;


    @BeforeEach
    void setUp() {
        codeResolver.givenCode("hello.jte" , "Hello Word!");

        // We started the app with a dev engine
        templateEngine = TemplateEngine.create(codeResolver, classDirectory, contentType);
    }

    @Test
    void testReloadOnDev() {
        thenOutputIs("Hello Word!");

        // We test the reload feature with a dev engine
        codeResolver.givenCode("hello.jte" , "Hello World!");

        // Recompile all templates and create a fresh engine. The old dev engine will be garbage collected..
        templateEngine = templateEngine.reloadPrecompiled(TemplateEngine.create(codeResolver, classDirectory, contentType));

        // Now, the correct output is rendered
        thenOutputIs("Hello World!");
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render("hello.jte", null, output);
        assertThat(output.toString()).isEqualTo(expected);
    }
}