package gg.jte;

import gg.jte.output.StringOutput;
import gg.jte.runtime.TemplateUtils;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngineTest {
    String templateName = "test/template.jte";

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver, ContentType.Plain);
    Model model = new Model();

    @BeforeEach
    void setUp() {
        model.hello = "Hello";
        model.x = 42;
    }

    @Test
    void helloWorld() {
        givenTemplate("${model.hello} World");
        thenOutputIs("Hello World");
    }

    @Test
    void templateWithoutParameters() {
        givenRawTemplate("Hello World!");
        thenOutputIs("Hello World!");
    }

    @Test
    void templateWithoutParameters_mrPoo() {
        givenRawTemplate("\uD83D\uDCA9");
        thenOutputIs("\uD83D\uDCA9");
    }

    @Test
    void templateWithoutParametersLong() {
        givenRawTemplate(TestUtils.repeat(".", 65536));
        thenOutputIs(TestUtils.repeat(".", 65536));
    }

    @Test
    void templateWithoutParametersLongNull() {
        givenRawTemplate(TestUtils.repeat("\u0000", 65536) + "foo");
        thenOutputIs(TestUtils.repeat("\u0000", 65536) + "foo");
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset0() {
        givenRawTemplate(TestUtils.repeat("\uD83D\uDCA9", 65536));
        thenOutputIs(TestUtils.repeat("\uD83D\uDCA9", 65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset1() {
        givenRawTemplate("." + TestUtils.repeat("\uD83D\uDCA9", 65536));
        thenOutputIs("." + TestUtils.repeat("\uD83D\uDCA9", 65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset2() {
        givenRawTemplate(".." + TestUtils.repeat("\uD83D\uDCA9", 65536));
        thenOutputIs(".." + TestUtils.repeat("\uD83D\uDCA9", 65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset3() {
        givenRawTemplate("..." + TestUtils.repeat("\uD83D\uDCA9", 65536));
        thenOutputIs("..." + TestUtils.repeat("\uD83D\uDCA9", 65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset4() {
        givenRawTemplate("...." + TestUtils.repeat("\uD83D\uDCA9", 65536));
        thenOutputIs("...." + TestUtils.repeat("\uD83D\uDCA9", 65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset5() {
        givenRawTemplate("....." + TestUtils.repeat("\uD83D\uDCA9", 65536));
        thenOutputIs("....." + TestUtils.repeat("\uD83D\uDCA9", 65536));
    }

    @Test
    void helloWorld_lineBreak() {
        givenTemplate("${model.hello}\nWorld");
        thenOutputIs("Hello\nWorld");
    }

    @Test
    void helloWorldMethod() {
        givenTemplate("${model.hello} ${model.getAnotherWorld()}");
        thenOutputIs("Hello Another World");
    }

    @Test
    void helloWorldMethod42() {
        givenTemplate("${model.hello} ${model.getAnotherWorld()} ${model.x}");
        thenOutputIs("Hello Another World 42");
    }

    @Test
    void helloLength() {
        givenTemplate("'${model.hello}' has length: ${model.hello.length()}");
        thenOutputIs("'Hello' has length: 5");
    }

    @Test
    void simpleTemplateName() {
        templateName = "Simple";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void simpleTemplateNameWithExtension() {
        templateName = "Simple.txt";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void simpleTemplateNameWithMultipleExtensions() {
        templateName = "Simple.txt.jte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void addition() {
        givenTemplate("${model.x + 1}");
        thenOutputIs("43");
    }

    @Test
    void addition2() {
        givenTemplate("${model.x + model.x}");
        thenOutputIs("84");
    }

    @Test
    void condition() {
        givenTemplate("@if (model.x == 42)Bingo@endif${model.x}");
        thenOutputIs("Bingo42");
        model.x = 12;
        thenOutputIs("12");
    }

    @Test
    void condition_else() {
        givenTemplate("@if (model.x == 42)" +
                "Bingo" +
                "@else" +
                "Bongo" +
                "@endif");

        model.x = 42;
        thenOutputIs("Bingo");
        model.x = 12;
        thenOutputIs("Bongo");
    }

    @Test
    void condition_elseif() {
        givenTemplate("@if (model.x == 42)" +
                "Bingo" +
                "@elseif (model.x == 43)" +
                "Bongo" +
                "@endif!");

        model.x = 42;
        thenOutputIs("Bingo!");
        model.x = 43;
        thenOutputIs("Bongo!");
        model.x = 44;
        thenOutputIs("!");
    }

    @Test
    void conditionBraces() {
        givenTemplate("@if ((model.x == 42) && (model.x > 0))Bingo@endif${model.x}");
        thenOutputIs("Bingo42");
        model.x = 12;
        thenOutputIs("12");
    }

    @Test
    void conditionNested() {
        givenTemplate("@if (model.x < 100)Bingo@if (model.x == 42)Bongo@endif@endif${model.x}");
        thenOutputIs("BingoBongo42");
        model.x = 12;
        thenOutputIs("Bingo12");
    }

    @Test
    void conditionInContentBlock() {
        model.x = 0;
        givenTemplate("x is ${@`@if (model.x > 0)" +
                "positive!" +
                "@elseif(model.x < 0)" +
                "negative!" +
                "@else" +
                "zero!" +
                "@endif`}");
        thenOutputIs("x is zero!");
    }

    @Test
    void conditionInstanceOfPatternMatching() {
        if (TestUtils.isInstanceOfPatternMatchingJavaVersion()) {
            givenTemplate("@if (model.object instanceof String s)${s.length()}@endif");
            model.object = "foobar";
            thenOutputIs("6");
        }
    }

    @Test
    void loop() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("@for (int i : model.array)" +
                "${i}" +
                "@endfor");
        thenOutputIs("123");
    }

    @Test
    void loopWithCondition() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("@for (int i : model.array)" +
                "@if (i > 1)" +
                "${i}" +
                "@endif" +
                "@endfor");
        thenOutputIs("23");
    }

    @Test
    void classicLoop() {
        model.array = new int[]{10, 20, 30};
        givenTemplate("@for (int i = 0; i < model.array.length; ++i)" +
                "Index ${i} is ${model.array[i]}" +
                "@if (i < model.array.length - 1)" +
                "<br>" +
                "@endif" +
                "@endfor");
        thenOutputIs("Index 0 is 10<br>Index 1 is 20<br>Index 2 is 30");
    }

    @Test
    void loopWithVariable() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("@for (int i : model.array)" +
                "!{int y = i + 1}" +
                "${y}" +
                "@endfor");
        thenOutputIs("234");
    }

    @Test
    void loopInContentBlock() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("${@`@for (int i : model.array)" +
                "${i}" +
                "@endfor`}");
        thenOutputIs("123");
    }

    @Test
    void unsafeInContentBlock() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("${@`$unsafe{\"\" + model.array.length}`}");
        thenOutputIs("3");
    }

    @Test
    void commentInContentBlock() {
        givenTemplate("${@`<%--$unsafe{model.array.length}--%>`}");
        thenOutputIs("");
    }

    @Test
    void commentInContentBlock_textBeforeAndAfterIsWritten() {
        givenTemplate("${@`before<%--$unsafe{model.array.length}--%>after`}");
        thenOutputIs("beforeafter");
    }

    @Test
    void variableInContentBlock() {
        givenTemplate("${@`!{int x = 5;}${x}`}");
        thenOutputIs("5");
    }

    @Test
    void jsStringInterpolationInContentBlock() {
        givenTemplate("!{gg.jte.Content content = @`\n"
              + "    <p>Hello World!</p>\n"
              + "    @raw\n"
              + "    <script>\n"
              + "        const hello = \"Hello!\"\n"
              + "        console.log(`${hello}`)\n"
              + "    </script>\n"
              + "    @endraw\n"
              + "`;}\n"
              + "${content}");
        thenOutputIs("\n\n"
              + "    <p>Hello World!</p>\n"
              + "    \n"
              + "    <script>\n"
              + "        const hello = \"Hello!\"\n"
              + "        console.log(`${hello}`)\n"
              + "    </script>\n"
              + "    \n");
    }

    @Test
    void statement() {
        givenTemplate("!{model.setX(12)}${model.x}");
        thenOutputIs("12");
    }

    @Test
    void ternaryOperator() {
        givenTemplate("${model.x > 0 ? \"Yay\" : \"Nay\"}");
        thenOutputIs("Yay");
    }

    @Test
    void variable() {
        givenTemplate("!{int y = 50}${y}");
        thenOutputIs("50");
    }

    @Test
    void variable_modern() {
        if (TestUtils.isLegacyJavaVersion()) {
            return;
        }

        givenTemplate("!{var y = 50}${y}");
        thenOutputIs("50");
    }

    @Test
    void braceInJavaString() {
        model.hello = ":-)";
        givenTemplate("@if(\":-)\".equals(model.hello))this is a smiley@endif");
        thenOutputIs("this is a smiley");
    }

    @Test
    void braceInJavaStringWithEscapedQuote() {
        model.hello = "\":-)";
        givenTemplate("@if(\"\\\":-)\".equals(model.hello))this is a smiley@endif");
        thenOutputIs("this is a smiley");
    }

    @Test
    void template() {
        givenTemplate("card.jte", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@template.card(model.hello, model.x), That was a tag!");
        thenOutputIs("One: Hello, two: 42, That was a tag!");
    }

    @Test
    void template_content() {
        givenTemplate("card.jte", "@param gg.jte.Content content\n" +
                "<span>${content}</span>");
        givenTemplate("@template.card(@`<b>${model.hello}</b>`), That was a tag!");
        thenOutputIs("<span><b>Hello</b></span>, That was a tag!");
    }

    @Test
    void template_content_comma() {
        givenTemplate("card.jte", "@param gg.jte.Content content\n" +
                "<span>${content}</span>");
        givenTemplate("@template.card(@`<b>Hello, ${model.hello}</b>`), That was a tag!");
        thenOutputIs("<span><b>Hello, Hello</b></span>, That was a tag!");
    }

    @Test
    void templateWithGenericParam() {
        givenTemplate("entry.jte", "@param java.util.Map.Entry<String, java.util.List<String>> entry\n" +
                "${entry.getKey()}: ${entry.getValue().toString()}");
        givenRawTemplate("@param java.util.Map<String, java.util.List<String>> map\n" +
                "@for(java.util.Map.Entry entry : map.entrySet())@template.entry(entry)\n@endfor");

        Map<String, List<String>> model = new TreeMap<>();
        model.put("one", Arrays.asList("1", "2"));
        model.put("two", Arrays.asList("6", "7"));

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, model, output);

        assertThat(output.toString()).isEqualTo("one: [1, 2]\ntwo: [6, 7]\n");
    }

    @Test
    void templateWithMethodCallForParam() {
        givenTemplate("card.jte", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@template.card(model.getAnotherWorld(), model.x), That was a tag!");
        thenOutputIs("One: Another World, two: 42, That was a tag!");
    }

    @Test
    void templateInTemplate() {
        givenTemplate("divTwo.jte", "@param int amount\n" +
                "Divided by two is ${amount / 2}!");
        givenTemplate("card.jte", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "${firstParam}, @template.divTwo(secondParam)");
        givenTemplate("@template.card (model.hello, model.x) That was a tag in a tag!");
        thenOutputIs("Hello, Divided by two is 21! That was a tag in a tag!");
    }

    @Test
    void sameTemplateReused() {
        givenTemplate("divTwo.jte", "@param int amount\n" +
                "${amount / 2}!");
        givenTemplate("@template.divTwo(model.x),@template.divTwo(2 * model.x)");
        thenOutputIs("21!,42!");
    }

    @Test
    void templateRecursion() {
        givenTemplate("recursion.jte", "@param int amount\n" +
                "${amount}" +
                "@if (amount > 0)" +
                "@template.recursion(amount - 1)" +
                "@endif"
        );
        givenTemplate("@template.recursion(5)");
        thenOutputIs("543210");
    }

    @Test
    void templateWithoutParams() {
        givenTemplate("basic.jte", "I do nothing!");
        givenTemplate("@template.basic()");
        thenOutputIs("I do nothing!");
    }

    @Test
    void templateWithoutParams_paramPassed() {
        givenTemplate("basic.jte", "I do nothing!");
        givenTemplate("@template.basic(42)");
        thenRenderingFailsWithException().hasMessageStartingWith("Failed to compile template, error at test/template.jte:2");
    }

    @Test
    void templateWithPackage() {
        givenTemplate("my/basic.jte", "I have a custom package");
        givenTemplate("@template.my.basic()");
        thenOutputIs("I have a custom package");
    }

    @Test
    void templateWithNamedParam() {
        givenTemplate("named.jte", "@param int one\n" +
                "@param int two\n" +
                "${one}, ${two}");
        givenTemplate("@template.named(two = 2, one = 1)");
        thenOutputIs("1, 2");
    }

    @Test
    void templateWithNamedParamString() {
        givenTemplate("named.jte", "@param int one\n" +
                "@param int two\n" +
                "@param String three\n" +
                "${one}, ${two}, ${three}");
        givenTemplate("@template.named(\n" +
                "two = 2,\n" +
                "three = \"Hello, there ;-)\",\n" +
                "one = 1)");
        thenOutputIs("1, 2, Hello, there ;-)");
    }

    @Test
    void templateWithNamedParam_ternary() {
        givenTemplate("named.jte", "@param int one\n" +
                "@param int two\n" +
                "${one}, ${two}");
        givenTemplate("@template.named(two = 1 == 2 ? 1 : 0, one = 1)");
        thenOutputIs("1, 0");
    }

    @Test
    void templateWithDefaultParam() {
        givenTemplate("named.jte", "@param int one = 1\n" +
                "@param int two = 2\n" +
                "${one}, ${two}");
        givenTemplate("@template.named()");

        thenOutputIs("1, 2");
    }

    @Test
    void templateWithDefaultParam_firstSet() {
        givenTemplate("named.jte", "@param int one = 1\n" +
                "@param int two = 2\n" +
                "${one}, ${two}");
        givenTemplate("@template.named(one = 6)");

        thenOutputIs("6, 2");
    }

    @Test
    void templateWithDefaultParam_secondSet() {
        givenTemplate("named.jte", "@param int one = 1\n" +
                "@param int two = 2\n" +
                "${one}, ${two}");
        givenTemplate("@template.named(two= 5)");

        thenOutputIs("1, 5");
    }

    @Test
    void templateWithVarArgs1() {
        givenTemplate("varargs.jte",
                "@param String ... values\n" +
                        "@for(String value : values)${value} @endfor");
        givenTemplate("@template.varargs(\"Hello\")");
        thenOutputIs("Hello ");
    }

    @Test
    void templateWithVarArgs2() {
        givenTemplate("varargs.jte",
                "@param String ... values\n" +
                        "@for(String value : values)${value} @endfor");
        givenTemplate("@template.varargs(\"Hello\", \"World\")");
        thenOutputIs("Hello World ");
    }

    @Test
    void templateWithVarArgs3() {
        givenTemplate("localize.jte",
                "@param String key\n" +
                        "@param String ... values\n" +
                        "${key} with @for(String value : values)${value} @endfor");
        givenTemplate("@template.localize(key = \"test.key\", \"Hello\", \"World\")");
        thenOutputIs("test.key with Hello World ");
    }

    @Test
    void templateWithVarArgs4() {
        givenTemplate("localize.jte",
                "@param String key\n" +
                        "@param String ... values\n" +
                        "${key} with @for(String value : values)${value} @endfor");
        givenTemplate("@template.localize(\"test.key\", \"Hello\", \"World\")");
        thenOutputIs("test.key with Hello World ");
    }

    @Test
    void templateCall() {
        givenTemplate("module/components/card.jte", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@template.module.components.card(model.hello, model.x), That was an arbitrary template call!");
        thenOutputIs("One: Hello, two: 42, That was an arbitrary template call!");
    }

    @Test
    void templateCall_noConflictWithKeywords_for() {
        givenTemplate("format.jte", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@template.format(model.hello, model.x), That was an arbitrary template call!");
        thenOutputIs("One: Hello, two: 42, That was an arbitrary template call!");
    }

    @Test
    void templateCall_noConflictWithKeywords_param() {
        givenTemplate("parameter.jte", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@template.parameter(model.hello, model.x), That was an arbitrary template call!");
        thenOutputIs("One: Hello, two: 42, That was an arbitrary template call!");
    }

    @Test
    void templateCall_noConflictWithKeywords_import() {
        givenTemplate("importer.jte", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@template.importer(model.hello, model.x), That was an arbitrary template call!");
        thenOutputIs("One: Hello, two: 42, That was an arbitrary template call!");
    }

    @Test
    void comment() {
        givenTemplate("<%--This is a comment" +
                " ${model.hello} everything in here is omitted--%>" +
                "This is visible...");
        thenOutputIs("This is visible...");
    }

    @Test
    void commentBeforeImports() {
        givenRawTemplate("<%--This is a comment--%>@import gg.jte.TemplateEngineTest.Model\n@param Model model\n" + "!{model.setX(12)}${model.x}");
        thenOutputIs("12");
    }

    @Test
    void commentBeforeParams() {
        givenRawTemplate("<%--This is a comment--%>@param gg.jte.TemplateEngineTest.Model model\n" + "!{model.setX(12)}${model.x}");
        thenOutputIs("12");
    }

    @Test
    void commentInImports() {
        givenRawTemplate("<%--@import gg.jte.TemplateEngineTest.Model--%>\n" + "<p>foo</p>");
        thenOutputIs("\n<p>foo</p>");
    }

    @Test
    void commentInParams() {
        givenRawTemplate("<%--@param gg.jte.TemplateEngineTest.Model model--%>\n" + "<p>foo</p>");
        thenOutputIs("\n<p>foo</p>");
    }

    @Test
    void commentInPlainTemplate() {
        givenTemplate("Hello<%--This is a comment--%> World");
        thenOutputIs("Hello World");
    }

    @Test
    void commentInPlainTemplate_raw() {
        givenTemplate("@rawHello<%--This is a comment--%> World@endraw");
        thenOutputIs("Hello<%--This is a comment--%> World");
    }

    @Test
    void htmlCommentInPlainTemplate() {
        givenTemplate("Hello<!--This is an HTML comment--> World");
        thenOutputIs("Hello<!--This is an HTML comment--> World");
    }

    @Test
    void importInCss() {
        givenTemplate("<style type=\"text/css\" rel=\"stylesheet\" media=\"all\">\n" +
                "    @import url(\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,700&display=swap\"); /* <--- Right here */");
        thenOutputIs("<style type=\"text/css\" rel=\"stylesheet\" media=\"all\">\n" +
                "    @import url(\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,700&display=swap\"); /* <--- Right here */");
    }

    @Test
    void importInCss2() {
        givenTemplate("<style type=\"text/css\" rel=\"stylesheet\" media=\"all\">\n" +
                "xxx@if(model.hello != null)    @import url(\"${model.hello}\");\n@endif</style>");
        thenOutputIs("<style type=\"text/css\" rel=\"stylesheet\" media=\"all\">\n" +
                "xxx    @import url(\"Hello\");\n" +
                "</style>");
    }

    @Test
    void paramAfterText() {
        givenTemplate("Hello @param");
        thenOutputIs("Hello @param");
    }

    @Test
    void paramWithoutName() {
        givenRawTemplate("@param int\n");
        thenRenderingFailsWithException().hasMessage("Failed to compile test/template.jte, error at line 1: Missing parameter name: '@param int'");
    }

    @Test
    void layout() {
        givenTemplate("layout/main.jte", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "@param gg.jte.Content content\n" +
                "@param gg.jte.Content footer\n" +
                "<body>\n" +
                "<b>Welcome to my site - you are on page ${model.x}</b>\n" +
                "\n" +
                "<div class=\"content\">\n" +
                "    ${content}\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"footer\">\n" +
                "    ${footer}\n" +
                "</div>\n" +
                "</body>");

        givenTemplate("@template.layout.main(model, content = @`\n" +
                "        ${model.hello}, enjoy this great content\n" +
                "    `,\n" +
                "    footer = @`\n" +
                "        Come again!\n" +
                "    `)");

        thenOutputIs(
                "<body>\n" +
                        "<b>Welcome to my site - you are on page 42</b>\n" +
                        "\n" +
                        "<div class=\"content\">\n" +
                        "    \n" +
                        "        Hello, enjoy this great content\n" +
                        "    \n" +
                        "</div>\n" +
                        "\n" +
                        "<div class=\"footer\">\n" +
                        "    \n" +
                        "        Come again!\n" +
                        "    \n" +
                        "</div>\n" +
                        "</body>");
    }

    @Test
    void nestedLayouts() {
        givenTemplate("layout/main.jte",
                "@param gg.jte.Content header = null\n" +
                        "@param gg.jte.Content content\n" +
                        "@param gg.jte.Content footer = null\n" +
                        "@if(header != null)<header>${header}</header>@endif" +
                        "<content>${content}</content>" +
                        "@if(footer != null)<footer>${footer}</footer>@endif");
        givenTemplate("layout/mainExtended.jte",
                "@param gg.jte.Content header = null\n" +
                        "@param gg.jte.Content contentPrefix = null\n" +
                        "@param gg.jte.Content content\n" +
                        "@param gg.jte.Content contentSuffix = null\n" +
                        "@param gg.jte.Content footer = null\n" +
                        "@template.layout.main(header = header, content = @`" +
                        "@if(contentPrefix != null)${contentPrefix}@endif" +
                        "<b>${content}</b>" +
                        "@if(contentSuffix != null)${contentSuffix}@endif" +
                        "`, footer = footer)");
        givenTemplate("@template.layout.mainExtended(" +
                "header = @`" +
                "this is the header" +
                "`," +
                "contentPrefix = @`" +
                "<content-prefix>" +
                "`," +
                "content = @`" +
                "this is the content" +
                "`, " +
                "contentSuffix=@`" +
                "<content-suffix>" +
                "`)");

        thenOutputIs("<header>this is the header</header>" +
                "<content><content-prefix><b>this is the content</b><content-suffix></content>");
    }

    @Test
    void layoutWithNamedParams() {
        givenTemplate("layout/main.jte",
                "@param int status = 5\n" +
                        "@param int duration = -1\n" +
                        "@param gg.jte.Content content\n" +
                        "Hello, ${content} your status is ${status}, the duration is ${duration}");

        givenTemplate("@template.layout.main(content = @`" +
                "Sir`)");

        thenOutputIs("Hello, Sir your status is 5, the duration is -1");
    }

    @Test
    void layoutWithNamedParams_noNames() {
        givenTemplate("layout/main.jte",
                "@param int status = 5\n" +
                        "@param int duration = -1\n" +
                        "@param gg.jte.Content content\n" +
                        "Hello, ${content} your status is ${status}, the duration is ${duration}");

        givenTemplate("@template.layout.main(42, 10, @`Sir`)");

        thenOutputIs("Hello, Sir your status is 42, the duration is 10");
    }

    @Test
    void layoutWithVarArgs() {
        givenTemplate("layout/varargs.jte",
                "@param String ... values\n" +
                        "@for(String value : values)${value} @endfor");
        givenTemplate("@template.layout.varargs(\"Hello\", \"World\")");
        thenOutputIs("Hello World ");
    }

    @Test
    void enumCheck() {
        givenRawTemplate(
                "@import gg.jte.TemplateEngineTest.Model\n" +
                        "@import gg.jte.TemplateEngineTest.ModelType\n" +
                        "@param Model model\n" +
                        "@if (model.type == ModelType.One)" +
                        "one" +
                        "@else" +
                        "not one" +
                        "@endif");

        model.type = ModelType.One;
        thenOutputIs("one");

        model.type = ModelType.Two;
        thenOutputIs("not one");
    }

    @Test
    void enumOutput() {
        givenTemplate("${model.type}");
        model.type = ModelType.One;
        thenOutputIs("One");
    }

    @Test
    void nestedJavascript() {
        givenTemplate("@if (model.isCaseA() && model.isCaseB())\n" +
                "        <meta name=\"robots\" content=\"a, b\">\n" +
                "        @elseif (model.isCaseB())\n" +
                "        <meta name=\"robots\" content=\"b\">\n" +
                "        @elseif (model.isCaseA())\n" +
                "        <meta name=\"robots\" content=\"a\">\n" +
                "        @endif" +
                "@if (model.x > 0)\n" +
                "        <meta name=\"description\" content=\"${model.x}\">\n" +
                "        @endif\n" +
                "\n" +
                "        <script>\n" +
                "            function readCookie(name) {");
        thenOutputIs("\n" +
                "        <meta name=\"robots\" content=\"a\">\n" +
                "        \n" +
                "        <meta name=\"description\" content=\"42\">\n" +
                "        \n" +
                "\n" +
                "        <script>\n" +
                "            function readCookie(name) {");
    }

    @Test
    void kebabCaseCanBeCompiled() {
        templateName = "kebab-case.jte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void kebabCaseCanBeCompiled_package() {
        templateName = "kebab-case/kebab-case.jte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void kebabCaseCanBeCompiled_templateCall() {
        givenTemplate("kebab-case/kebab-case.jte", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "@param int i = 0\n" +
                "i is: ${i}");

        givenTemplate("@template.kebab-case.kebab-case(model = model, i = 42)");

        thenOutputIs("i is: 42");
    }

    @Test
    void classPrefix() {
        templateName = "test/404.jte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void escaping() {
        givenTemplate("\\");
        thenOutputIs("\\");
    }

    @Test
    void npe_internal() {
        givenTemplate("This is ${model.getThatThrows()} world");

        thenRenderingFailsWithExceptionCausedBy(NullPointerException.class);
    }

    @Test
    void exceptionLineNumber1() {
        givenRawTemplate(
                "@import gg.jte.TemplateEngineTest.Model\n" +
                        "\n" +
                        "@param gg.jte.TemplateEngineTest.Model model\n" +
                        "\n" +
                        "${model.getThatThrows()}\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.jte, error at test/template.jte:5");
    }

    @Test
    void exceptionLineNumber2() {
        givenRawTemplate(
                "@import gg.jte.TemplateEngineTest.Model\n" +
                        "\n\n\n" +
                        "@param gg.jte.TemplateEngineTest.Model model\n" +
                        "\n" +
                        "${model.getThatThrows()}\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.jte, error at test/template.jte:7")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.test.JtetemplateGenerated.render(test/template.jte:7)");
    }

    @Test
    void exceptionLineNumber3() {
        givenRawTemplate(
                "@import gg.jte.TemplateEngineTest.Model\n" +
                        "\n" +
                        "@param gg.jte.TemplateEngineTest.Model model\n" +
                        "\n" +
                        "${model.hello} ${model.getThatThrows()}\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.jte, error at test/template.jte:5");
    }

    @Test
    void exceptionLineNumber4() {
        givenRawTemplate(
                "@import gg.jte.TemplateEngineTest.Model\n" +
                        "\n" +
                        "@param gg.jte.TemplateEngineTest.Model model\n" +
                        "\n" +
                        "${model.hello}\n" +
                        "@for(int i = 0; i < 3; i++)\n" +
                        "\t${i}\n" +
                        "\t${model.getThatThrows()}\n" +
                        "@endfor\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.jte, error at test/template.jte:8");
    }

    @Test
    void exceptionLineNumber5() {
        givenTemplate("my/model.jte", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "@param int i = 0\n" +
                "i is: ${i}\n" +
                "${model.getThatThrows()}");
        givenTemplate("@template.my.model(model)");

        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.jte, error at my/model.jte:4")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.my.JtemodelGenerated.render(my/model.jte:4)")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.test.JtetemplateGenerated.render(test/template.jte:2)");
    }

    @Test
    void exceptionLineNumber6() {
        givenTemplate("my/model.jte", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "@param int i = 0\n" +
                "i is: ${i}\n" +
                "${model.getThatThrows()}");

        StringOutput output = new StringOutput();
        Throwable throwable = catchThrowable(() -> templateEngine.render("my/model.jte", TemplateUtils.toMap("model", model, "i", 1L), output));

        assertThat(throwable)
                .hasCauseInstanceOf(ClassCastException.class)
                .hasMessage("Failed to render my/model.jte, error at my/model.jte:2")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.my.JtemodelGenerated.renderMap(my/model.jte:2)");
    }

    @Test
    void curlyBracesAreTracked() {
        givenRawTemplate("!{java.util.Optional<String> name = java.util.Optional.ofNullable(null)}" +
                "${name.map((n) -> { return \"name: \" + n; }).orElse(\"empty\")}");

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, null, output);

        assertThat(output.toString()).isEqualTo("empty");
    }

    @Test
    void curlyBracesAreTracked_unsafe() {
        givenRawTemplate("!{java.util.Optional<String> name = java.util.Optional.ofNullable(null)}" +
                "$unsafe{name.map((n) -> { return \"name: \" + n; }).orElse(\"empty\")}");

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, null, output);

        assertThat(output.toString()).isEqualTo("empty");
    }

    @Test
    void curlyBracesAreTracked_statement() {
        givenRawTemplate("!{String name = java.util.Optional.ofNullable(null).map((n) -> { return \"name: \" + n; }).orElse(\"empty\")}" +
                "${name}");

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, null, output);

        assertThat(output.toString()).isEqualTo("empty");
    }

    @Test
    void brace_unclosed() {
        givenTemplate("${model.isCaseA(}");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Unexpected closing brace }, expected )");
    }

    @Test
    void brace_closedOnceMore() {
        givenTemplate("${model.isCaseA())}");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Unexpected closing brace )");
    }

    @Test
    void brace_allUnclosed() {
        givenTemplate("${model.isCaseA(");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Missing closing brace )");
    }

    @Test
    void brace_unclosed_string() {
        givenTemplate("!{model.setHello(\n" +
                "\")}{(test)}{(\"\n" +
                ";}");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 4: Unexpected closing brace }, expected )");
    }

    @Test
    void brace_closedOnceMore_string() {
        givenTemplate("${model.setHello(\n" +
                "\")}{(test)}{(\"\n" +
                "))}");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 4: Unexpected closing brace )");
    }

    @Test
    void brace_allUnclosed_string() {
        givenTemplate("!{model.setHello(\"foo{{{{{)\"");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Missing closing brace )");
    }

    @Test
    void brace_if_unclosed() {
        givenTemplate("@if(model.isCaseA()\n" +
                "foo\n" +
                "@endif");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Missing closing brace )");
    }

    @Test
    void brace_if_unclosed_elseif() {
        givenTemplate("@if(model.isCaseA()\n" +
                "foo\n" +
                "@elseif(model.isCaseB())\n");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Missing closing brace )");
    }

    @Test
    void brace_if_unclosed_else() {
        givenTemplate("@if(model.isCaseA()\n" +
                "foo\n" +
                "@else\n");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Missing closing brace )");
    }

    @Test
    void brace_for_unclosed() {
        givenTemplate("@for(int i = 0; i < model.getAnotherWorld().length; ++i\n" +
                "foo\n" +
                "@endfor");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 2: Missing closing brace )");
    }

    @Test
    void if_missing_endif() {
        givenTemplate("@if(model.isCaseA())\n" +
                "foo\n");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 4: Missing @endif");
    }

    @Test
    void elseif_missing_endif() {
        givenTemplate("@if(model.isCaseA())\n" +
                "foo\n" +
                "@elseif(model.isCaseB())\n");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 5: Missing @endif");
    }

    @Test
    void for_missing_endfor() {
        givenTemplate("@for(int i = 0; i < 10; ++i)\n" +
                "foo\n");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile test/template.jte, error at line 4: Missing @endfor");
    }

    @Test
    void emptyTemplate() {
        givenRawTemplate("");
        thenOutputIs("");
    }

    @Test
    void emptyTemplateCall() {
        givenTemplate("test.jte", "");
        givenTemplate("@template.test()");
        thenOutputIs("");
    }

    @Test
    void compileError0() {
        thenRenderingFailsWithException()
                .hasMessage("test/template.jte not found");
    }

    @Test
    void compileError1() {
        givenTemplate("@template.my.model(model)");
        thenRenderingFailsWithException()
                .hasMessage("my/model.jte not found, referenced at test/template.jte:2");
    }

    @Test
    void compileError2() {
        givenTemplate("Hello\n@template.layout.page(model)");
        thenRenderingFailsWithException()
                .hasMessage("layout/page.jte not found, referenced at test/template.jte:3");
    }

    @Test
    void compileError3() {
        givenTemplate("${model.hello}\n" +
                "${model.hello}\n" +
                "${model.helloUnknown}");

        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at test/template.jte:4\n")
                .hasMessageContaining("cannot find symbol")
                .hasMessageContaining("model.helloUnknown");
    }

    @Test
    void compileError4() {
        givenTemplate("test.jte", "@param gg.jte.TemplateEngineTest.Model model\nThis will not compile!\n${model.helloUnknown}\n!!");
        givenTemplate("@template.test(model)");
        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at test.jte:3\n")
                .hasMessageContaining("cannot find symbol")
                .hasMessageContaining("model.helloUnknown");
    }

    @Test
    void compileError5() {
        givenTemplate("test.jte", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "${\n" +
                "@`\n" +
                "This will not compile!\n${model.helloUnknown}\n!!\n" +
                "`}");
        givenTemplate("@template.test(model)");
        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at test.jte:5\n")
                .hasMessageContaining("cannot find symbol")
                .hasMessageContaining("model.helloUnknown");
    }

    @Test
    void compileError6() {
        givenTemplate("test.jte", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "test");
        givenTemplate("@template.test(\n" +
                "model = model,\n" +
                "param2 = \"foo\")");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile template, error at test/template.jte:3. No parameter with name param2 is defined in test.jte");
    }

    @Test
    void calledWithWrongParam1() {
        givenRawTemplate("@param String hello\n${hello}");
        thenRenderingFailsWithException().hasMessage("Failed to render test/template.jte, type mismatch for parameter: Expected java.lang.String, got gg.jte.TemplateEngineTest$Model");
    }

    @Test
    void calledWithWrongParam2() {
        givenRawTemplate("@param int x\n${x}");
        thenRenderingFailsWithException().hasMessage("Failed to render test/template.jte, type mismatch for parameter: Expected int, got gg.jte.TemplateEngineTest$Model");
    }

    @Test
    void calledWithWrongParam3() {
        model = null;
        givenRawTemplate("@param int x\n${x}");
        thenRenderingFailsWithException().hasMessage("Failed to render test/template.jte, type mismatch for parameter: Expected int, got null");
    }

    @Test
    void getParamInfo_none() {
        givenRawTemplate("Hello World!");
        Map<String, Class<?>> params = templateEngine.getParamInfo(templateName);
        assertThat(params).isEmpty();
    }

    @Test
    void getParamInfo_one() {
        givenRawTemplate("@param int foo\nHello World!");
        Map<String, Class<?>> params = templateEngine.getParamInfo(templateName);
        assertThat(params).hasSize(1);
        assertThat(params).containsEntry("foo", int.class);
    }

    @Test
    void getParamInfo_some() {
        givenRawTemplate("@import gg.jte.Content\n@param int foo\n@param Content content\nHello World!");
        Map<String, Class<?>> params = templateEngine.getParamInfo(templateName);
        assertThat(params).hasSize(2);
        assertThat(params).containsEntry("foo", int.class);
        assertThat(params).containsEntry("content", Content.class);
    }

    @Test
    void getParamInfo_lazy() {
        givenRawTemplate("@param int foo\nHello World!");
        Map<String, Class<?>> params1 = templateEngine.getParamInfo(templateName);
        Map<String, Class<?>> params2 = templateEngine.getParamInfo(templateName);

        assertThat(params1).isSameAs(params2);
        assertThat(catchThrowable(() -> params1.put("bar", String.class))).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void missingContentType() {
        Throwable throwable = catchThrowable(() -> TemplateEngine.create(dummyCodeResolver, null));
        assertThat(throwable).isInstanceOf(NullPointerException.class).hasMessage("Content type must be specified.");
    }

    @Test
    void compileArgs_enablePreview() {
        if (TestUtils.isLegacyJavaVersion()) {
            return;
        }

        templateEngine.setCompileArgs("--enable-preview");
        givenRawTemplate("Hello World!");
        thenRenderingFailsWithException().hasMessageContaining("--enable-preview must be used with either -source or --release");
    }

    @Test
    void kteTemplate() {
        templateName = "test/template.kte";
        givenRawTemplate("Hello Kotlin!");
        thenRenderingFailsWithException().hasMessage("Failed to create kotlin generator. To handle .kte files, you need to add gg.jte:jte-kotlin to your project.");
    }

    private void givenTemplate(String name, String code) {
        dummyCodeResolver.givenCode(name, code);
    }

    private void givenTemplate(String template) {
        template = "@param gg.jte.TemplateEngineTest.Model model\n" + template;
        givenRawTemplate(template);
    }

    private void givenRawTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void thenOutputIs(String expected) {
        StringOutput output = new StringOutput();
        templateEngine.render(templateName, model, output);

        assertThat(output.toString()).isEqualTo(expected);
    }

    private AbstractThrowableAssert<?, ? extends Throwable> thenRenderingFailsWithException() {
        Throwable throwable = catchThrowable(() -> thenOutputIs("ignored"));
        return assertThat(throwable).isInstanceOf(TemplateException.class);
    }

    @SuppressWarnings("SameParameterValue")
    private void thenRenderingFailsWithExceptionCausedBy(Class<? extends Throwable> clazz) {
        thenRenderingFailsWithException().hasCauseInstanceOf(clazz);
    }

    @SuppressWarnings("unused")
    public enum ModelType {
        One, Two, Three
    }

    @SuppressWarnings("unused")
    public static class Model {
        public String hello;
        public int x;
        public int[] array;
        public ModelType type;
        public Object object;

        // For null iteration tests
        public boolean[] booleanArray;
        public byte[] byteArray;
        public short[] shortArray;
        public int[] intArray;
        public long[] longArray;
        public float[] floatArray;
        public double[] doubleArray;
        public List<String> list;
        public ArrayList<String> arrayList;
        public Set<String> set;
        public Collection<String> collection;
        public Iterable<String> iterable;

        public String getAnotherWorld() {
            return "Another World";
        }

        public void setX(int amount) {
            x = amount;
        }

        public boolean isCaseA() {
            return true;
        }

        public boolean isCaseB() {
            return false;
        }

        public void setHello(String hello) {
            this.hello = hello;
        }

        public String getThatThrows() {
            throw new NullPointerException("Oops");
        }
    }
}