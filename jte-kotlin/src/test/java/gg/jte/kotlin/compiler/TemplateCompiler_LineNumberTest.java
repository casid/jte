package gg.jte.kotlin.compiler;

import gg.jte.ContentType;
import gg.jte.TemplateConfig;
import gg.jte.TemplateException;
import gg.jte.compiler.TemplateCompiler;
import gg.jte.kotlin.DummyCodeResolver;
import gg.jte.runtime.ClassInfo;
import gg.jte.runtime.Constants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class TemplateCompiler_LineNumberTest {

    public DummyCodeResolver getDummyCodeResolver() {
        DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
        dummyCodeResolver.givenCode("another.kte", """
        @param content:Content
        
        ${content}
        """
        );

        // Comment numbers are zero-indexed to match JTE_LINE_INFO; user-facing error messages will be one-indexed

        // This test case is designed to exercise a number of pathological cases involving content blocks integrated into
        // various kinds of code blocks. The output should be manually verified using dumpAnnotatedGeneratedCode and then copy-pasted
        // into static data in the tests to prevent regressions
        dummyCodeResolver.givenCode("test.kte", stripLineLabels("""
        @import java.util.List      <%-- Line 0 --%>
        @import java.util.Map       <%-- Line 1 --%>
                                    <%-- Line 2 --%>
        @param example: String      <%-- Line 3 --%>
        @param content: Content = @`<%-- Line 4 --%>
          content0                  <%-- Line 5 --%>
        `                           <%-- Line 6 --%>
        Some text 1                 <%-- Line 7 --%>
        !{function(arg1,            <%-- Line 8 --%>
                   arg2,            <%-- Line 9 --%>
        @`                          <%-- Line 10 --%>
            content1                <%-- Line 11 --%>
        `,                          <%-- Line 12 --%>
        arg3);}                     <%-- Line 13 --%>
        Some text 2                 <%-- Line 14 --%>
        @template.another(arg1,     <%-- Line 15 --%>
        arg2,                       <%-- Line 16 --%>
        @`                          <%-- Line 17 --%>
            content2                <%-- Line 18 --%>
        `, arg3,                    <%-- Line 19 --%>
        arg4) inline text           <%-- Line 20 --%>
        Some text 3                 <%-- Line 21 --%>
        @if(1=2)                    <%-- Line 22 --%>
        Some text 4                 <%-- Line 23 --%>
        @elseif(content == @`       <%-- Line 24 --%>
            content3                <%-- Line 25 --%>
        `)                          <%-- Line 26 --%>
        @endif                      <%-- Line 27 --%>
        @raw                        <%-- Line 28 --%>
        Some text 5                 <%-- Line 29 --%>
        @endraw                     <%-- Line 30 --%>
        Some text 6                 <%-- Line 31 --%>
        """));
        return dummyCodeResolver;
    }

    @Test
    void testLineNumbers() {
        TemplateCompiler templateCompiler = new TemplateCompiler(new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED), getDummyCodeResolver(), Paths.get(""), null);
        templateCompiler.generateAll();
        ClassInfo info = templateCompiler.getClassInfo(null, "test.kte");
        assertThat(info.lineInfo).isEqualTo(new int[] {0,0,0,1,0,0,0,0,0,0,8,8,9,10,10,12,12,12,13,15,17,17,19,19,20,22,22,24,24,25,26,27,27,28,30,32,0,0,3,4,4,6,6,6,0,0,0,0});
    }

    @Test
    void testLineNumbersBinary()  {
        TemplateConfig config = new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED);
        config.binaryStaticContent = true;
        TemplateCompiler templateCompiler = new TemplateCompiler(config, getDummyCodeResolver(), Paths.get(""), null);
        templateCompiler.generateAll();
        ClassInfo info = templateCompiler.getClassInfo(null, "test.kte");
        assertThat(info.lineInfo).isEqualTo(new int[] {0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,9,10,10,12,12,12,13,15,17,17,19,19,20,22,22,24,24,25,26,27,27,28,30,32,0,0,3,4,4,6,6,6,0,0,0,0});
    }

    @Test
    void testJteCompilerError() {
        DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
        dummyCodeResolver.givenCode("invalidJte.kte", stripLineLabels("""
        @import kotlin.collections.List  <%-- Line 0 --%>
                                         <%-- Line 1 --%>
        @param args: List<String>        <%-- Line 2 --%>
                                         <%-- Line 3 --%>
        ${args.toString()                <%-- Line 4 --%>
        """));
        TemplateCompiler templateCompiler = new TemplateCompiler(new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED), dummyCodeResolver, Paths.get(""), null);
        assertThatThrownBy(templateCompiler::generateAll).isInstanceOf(TemplateException.class).hasMessageContaining("error at line 6");
    }

    @Test
    void testJteKotlinError() {
        DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
        // The Map reference should produce a "Unresolved reference 'fakemap'" error on user-facing line 3
        dummyCodeResolver.givenCode("invalidKotlin.kte", stripLineLabels("""
        @import kotlin.collections.List      <%-- Line 0 --%>
                                             <%-- Line 1 --%>
        @param args: FakeMap<String, String> <%-- Line 2 --%>
                                             <%-- Line 3 --%>
        ${args.toString()}                   <%-- Line 4 --%>
        """));
        TemplateCompiler templateCompiler = new TemplateCompiler(new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED), dummyCodeResolver, Paths.get(""), null);
        assertThatThrownBy(templateCompiler::precompileAll).isInstanceOf(TemplateException.class).hasMessageContaining("invalidKotlin.kte:3");
    }


    @Disabled("Test exists to dump the annotated generated code for debugging/updating the above tests")
    @Test
    void dumpAnnotatedGeneratedCode() throws IOException {
        TemplateConfig config = new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED);
        config.binaryStaticContent = false;
        TemplateCompiler templateCompiler = new TemplateCompiler(config, getDummyCodeResolver(), Paths.get(""), null);
        String generatedPath = templateCompiler.generateAll().stream().filter(p -> p.endsWith("testGenerated.kt")).findFirst().orElseThrow();
        ClassInfo info = templateCompiler.getClassInfo(null, "test.kte");
        List<String> content = Files.readAllLines(Paths.get(generatedPath));
        for(int i = 0; i < info.lineInfo.length; i++) {
            System.out.printf("%02d: %02d -> %s%n", i, info.lineInfo[i], content.get(i));
        }
    }

    private String stripLineLabels(String content) {
        return content.replaceAll("<%-- Line [0-9]+ --%>", "");
    }
}
