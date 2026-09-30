package gg.jte.compiler;

import gg.jte.ContentType;
import gg.jte.DummyCodeResolver;
import gg.jte.TemplateConfig;
import gg.jte.runtime.ClassInfo;
import gg.jte.runtime.Constants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TemplateCompiler_LineNumberTest {

    public DummyCodeResolver getDummyCodeResolver() {
        DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
        dummyCodeResolver.givenCode("another.jte", """
        @param Content content
        
        ${content}
        """
        );
        // This test case is designed to exercise a number of pathological cases involving content blocks integrated into
        // various kinds of code blocks. The output should be manually verified using dumpAnnotatedGeneratedCode and then copy-pasted
        // into static data in the tests to prevent regressions
        dummyCodeResolver.givenCode("test.jte", """
        @import java.util.List      <%-- Line0 --%>
        @import java.util.Map       <%-- Line 1 --%>
                                    <%-- Line 2 --%>
        @param String example       <%-- Line 3 --%>
        @param Content content = @` <%-- Line 4 --%>
          content0                  <%-- Line 5 --%>
        `                           <%-- Line 6 --%>
        Some text 1                 <%-- Line 7 --%>
        !{function(arg1,            //   Line 8
                   arg2,            //   Line 9
        @`                          <%-- Line 10 --%>
            content1                <%-- Line 11 --%>
        `,                          //   Line 12
        arg3);}                     <%-- Line 13 --%>
        Some text 2                 <%-- Line 14 --%>
        @template.another(arg1,     //   Line 15
        arg2,                       //   Line 16
        @`                          <%-- Line 17 --%>
            content2                <%-- Line 18 --%>
        `, arg3,                    //   Line 19
        arg4) inline text           <%-- Line 20 --%>
        Some text 3                 <%-- Line 21 --%>
        @if(1=2)                    <%-- Line 22 --%>
        Some text 4                 <%-- Line 23 --%>
        @elseif(content == @`       <%-- Line 24 --%>
            content3                <%-- Line 25 --%>
        `)                          //   Line 26
        @endif                      <%-- Line 27 --%>
        @raw                        <%-- Line 28 --%>
        Some text 5                 <%-- Line 29 --%>
        @endraw                     <%-- Line 30 --%>
        Some text 6                 <%-- Line 31 --%>
        """);
        return dummyCodeResolver;
    }

    @Test
    void testLineNumbers() {
        TemplateCompiler templateCompiler = new TemplateCompiler(new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED), getDummyCodeResolver(), Paths.get(""), null);
        templateCompiler.generateAll();
        ClassInfo info = templateCompiler.getClassInfo(null, "test.jte");
        assertThat(info.lineInfo).isEqualTo(new int[] {0,0,1,0,0,0,0,0,0,7,8,8,9,10,10,10,11,12,12,12,13,13,14,15,15,16,17,17,17,18,19,19,19,20,20,21,22,22,22,23,24,24,25,26,27,27,27,28,30,30,31,32,0,0,3,4,4,6,6,6,0,0,0});
    }

    @Test
    void testLineNumbersBinary()  {
        TemplateConfig config = new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED);
        config.binaryStaticContent = true;
        TemplateCompiler templateCompiler = new TemplateCompiler(config, getDummyCodeResolver(), Paths.get(""), null);
        templateCompiler.generateAll();
        ClassInfo info = templateCompiler.getClassInfo(null, "test.jte");
        assertThat(info.lineInfo).isEqualTo(new int[] {0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,8,8,9,10,10,10,11,12,12,12,13,13,14,15,15,16,17,17,17,18,19,19,19,20,20,21,22,22,22,23,24,24,25,26,27,27,27,28,30,30,31,32,0,0,3,4,4,6,6,6,0,0,0});
    }

    @Disabled("Test exists to dump the annotated generated code for debugging/updating the above tests")
    @Test
    void dumpAnnotatedGeneratedCode() throws IOException {
        TemplateConfig config = new TemplateConfig(ContentType.Plain, Constants.PACKAGE_NAME_PRECOMPILED);
        config.binaryStaticContent = false;
        TemplateCompiler templateCompiler = new TemplateCompiler(config, getDummyCodeResolver(), Paths.get(""), null);
        String generatedPath = templateCompiler.generateAll().stream().filter(p -> p.endsWith("testGenerated.java")).findFirst().orElseThrow();
        ClassInfo info = templateCompiler.getClassInfo(null, "test.jte");
        List<String> content = Files.readAllLines(Paths.get(generatedPath));
        for(int i = 0; i < info.lineInfo.length; i++) {
            System.out.printf("%02d: %02d -> %s%n", i, info.lineInfo[i], content.get(i));
        }
    }
}
