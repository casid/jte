package gg.jte;

import gg.jte.output.StringOutput;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngine_KotlinTest {
    String templateName = "test/template.kte";

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
        givenTemplate("'${model.hello}' has length: ${model.hello.length}");
        thenOutputIs("'Hello' has length: 5");
    }

    @Test
    void templateNameWithMultipleExtensions() {
        templateName = "Simple.txt.kte";
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
        givenTemplate("${@`$unsafe{model.array.length}`}");
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
    void statement() {
        givenTemplate("!{model.setX(12)}${model.x}");
        thenOutputIs("12");
    }

    @Test
    void ternaryOperator() {
        givenTemplate("${if (model.x > 0) \"Yay\" else \"Nay\"}");
        thenOutputIs("Yay");
    }

    @Test
    void variable() {
        givenTemplate("!{val y = 50}${y}");
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
    void tag() {
        givenTag("card", "@param firstParam:String\n" +
                "@param secondParam:Int\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@tag.card(model.hello, model.x), That was a tag!");
        thenOutputIs("One: Hello, two: 42, That was a tag!");
    }

    @Test
    void tag_content() {
        givenTag("card", "@param gg.jte.Content content\n" +
                "<span>${content}</span>");
        givenTemplate("@tag.card(@`<b>${model.hello}</b>`), That was a tag!");
        thenOutputIs("<span><b>Hello</b></span>, That was a tag!");
    }

    @Test
    void tag_content_comma() {
        givenTag("card", "@param gg.jte.Content content\n" +
                "<span>${content}</span>");
        givenTemplate("@tag.card(@`<b>Hello, ${model.hello}</b>`), That was a tag!");
        thenOutputIs("<span><b>Hello, Hello</b></span>, That was a tag!");
    }

    @Test
    void tagWithGenericParam() {
        givenTag("entry", "@param java.util.Map.Entry<String, java.util.List<String>> entry\n" +
                "${entry.getKey()}: ${entry.getValue().toString()}");
        givenRawTemplate("@param java.util.Map<String, java.util.List<String>> map\n" +
                "@for(java.util.Map.Entry entry : map.entrySet())@tag.entry(entry)\n@endfor");

        Map<String, List<String>> model = new TreeMap<>();
        model.put("one", Arrays.asList("1", "2"));
        model.put("two", Arrays.asList("6", "7"));

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, model, output);

        assertThat(output.toString()).isEqualTo("one: [1, 2]\ntwo: [6, 7]\n");
    }

    @Test
    void tagWithMethodCallForParam() {
        givenTag("card", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@tag.card(model.getAnotherWorld(), model.x), That was a tag!");
        thenOutputIs("One: Another World, two: 42, That was a tag!");
    }

    @Test
    void tagInTag() {
        givenTag("divTwo", "@param int amount\n" +
                "Divided by two is ${amount / 2}!");
        givenTag("card", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "${firstParam}, @tag.divTwo(secondParam)");
        givenTemplate("@tag.card (model.hello, model.x) That was a tag in a tag!");
        thenOutputIs("Hello, Divided by two is 21! That was a tag in a tag!");
    }

    @Test
    void sameTagReused() {
        givenTag("divTwo", "@param int amount\n" +
                "${amount / 2}!");
        givenTemplate("@tag.divTwo(model.x),@tag.divTwo(2 * model.x)");
        thenOutputIs("21!,42!");
    }

    @Test
    void tagRecursion() {
        givenTag("recursion", "@param int amount\n" +
                "${amount}" +
                "@if (amount > 0)" +
                "@tag.recursion(amount - 1)" +
                "@endif"
        );
        givenTemplate("@tag.recursion(5)");
        thenOutputIs("543210");
    }

    @Test
    void tagWithoutParams() {
        givenTag("basic", "I do nothing!");
        givenTemplate("@tag.basic()");
        thenOutputIs("I do nothing!");
    }

    @Test
    void tagWithoutParams_paramPassed() {
        givenTag("basic", "I do nothing!");
        givenTemplate("@tag.basic(42)");
        thenRenderingFailsWithException().hasMessageStartingWith("Failed to compile template, error at test/template.jte:2");
    }

    @Test
    void tagWithPackage() {
        givenTag("my/basic", "I have a custom package");
        givenTemplate("@tag.my.basic()");
        thenOutputIs("I have a custom package");
    }

    @Test
    void tagWithNamedParam() {
        givenTag("named", "@param int one\n" +
                "@param int two\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named(two = 2, one = 1)");
        thenOutputIs("1, 2");
    }

    @Test
    void tagWithNamedParamString() {
        givenTag("named", "@param int one\n" +
                "@param int two\n" +
                "@param String three\n" +
                "${one}, ${two}, ${three}");
        givenTemplate("@tag.named(\n" +
                "two = 2,\n" +
                "three = \"Hello, there ;-)\",\n" +
                "one = 1)");
        thenOutputIs("1, 2, Hello, there ;-)");
    }

    @Test
    void tagWithNamedParam_ternary() {
        givenTag("named", "@param int one\n" +
                "@param int two\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named(two = 1 == 2 ? 1 : 0, one = 1)");
        thenOutputIs("1, 0");
    }

    @Test
    void tagWithDefaultParam() {
        givenTag("named", "@param int one = 1\n" +
                "@param int two = 2\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named()");

        thenOutputIs("1, 2");
    }

    @Test
    void tagWithDefaultParam_firstSet() {
        givenTag("named", "@param int one = 1\n" +
                "@param int two = 2\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named(one = 6)");

        thenOutputIs("6, 2");
    }

    @Test
    void tagWithDefaultParam_secondSet() {
        givenTag("named", "@param int one = 1\n" +
                "@param int two = 2\n" +
                "${one}, ${two}");
        givenTemplate("@tag.named(two= 5)");

        thenOutputIs("1, 5");
    }

    @Test
    void tagWithVarArgs1() {
        givenTag("varargs",
                "@param String ... values\n" +
                "@for(String value : values)${value} @endfor");
        givenTemplate("@tag.varargs(\"Hello\")");
        thenOutputIs("Hello ");
    }

    @Test
    void tagWithVarArgs2() {
        givenTag("varargs",
                "@param String ... values\n" +
                "@for(String value : values)${value} @endfor");
        givenTemplate("@tag.varargs(\"Hello\", \"World\")");
        thenOutputIs("Hello World ");
    }

    @Test
    void tagWithVarArgs3() {
        givenTag("localize",
                "@param String key\n" +
                "@param String ... values\n" +
                "${key} with @for(String value : values)${value} @endfor");
        givenTemplate("@tag.localize(key = \"test.key\", \"Hello\", \"World\")");
        thenOutputIs("test.key with Hello World ");
    }

    @Test
    void tagWithVarArgs4() {
        givenTag("localize",
                "@param String key\n" +
                        "@param String ... values\n" +
                        "${key} with @for(String value : values)${value} @endfor");
        givenTemplate("@tag.localize(\"test.key\", \"Hello\", \"World\")");
        thenOutputIs("test.key with Hello World ");
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
        givenLayout("main", "@param gg.jte.TemplateEngineTest.Model model\n" +
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

        givenTemplate("@layout.main(model, content = @`\n" +
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
        givenLayout("main",
                "@param gg.jte.Content header = null\n" +
                "@param gg.jte.Content content\n" +
                "@param gg.jte.Content footer = null\n" +
                "@if(header != null)<header>${header}</header>@endif" +
                "<content>${content}</content>" +
                "@if(footer != null)<footer>${footer}</footer>@endif");
        givenLayout("mainExtended",
                "@param gg.jte.Content header = null\n" +
                "@param gg.jte.Content contentPrefix = null\n" +
                "@param gg.jte.Content content\n" +
                "@param gg.jte.Content contentSuffix = null\n" +
                "@param gg.jte.Content footer = null\n" +
                "@layout.main(header = header, content = @`" +
                "@if(contentPrefix != null)${contentPrefix}@endif" +
                "<b>${content}</b>" +
                "@if(contentSuffix != null)${contentSuffix}@endif" +
                "`, footer = footer)");
        givenTemplate("@layout.mainExtended(" +
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
        givenLayout("main",
                "@param int status = 5\n" +
                "@param int duration = -1\n" +
                "@param gg.jte.Content content\n" +
                "Hello, ${content} your status is ${status}, the duration is ${duration}");

        givenTemplate("@layout.main(content = @`" +
                "Sir`)");

        thenOutputIs("Hello, Sir your status is 5, the duration is -1");
    }

    @Test
    void layoutWithNamedParams_noNames() {
        givenLayout("main",
                "@param int status = 5\n" +
                        "@param int duration = -1\n" +
                        "@param gg.jte.Content content\n" +
                        "Hello, ${content} your status is ${status}, the duration is ${duration}");

        givenTemplate("@layout.main(42, 10, @`Sir`)");

        thenOutputIs("Hello, Sir your status is 42, the duration is 10");
    }

    @Test
    void layoutWithVarArgs() {
        givenLayout("varargs",
                "@param String ... values\n" +
                        "@for(String value : values)${value} @endfor");
        givenTemplate("@layout.varargs(\"Hello\", \"World\")");
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
    void snakeCaseCanBeCompiled() {
        templateName = "snake-case.jte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
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
                .hasMessage("Failed to render test/template.jte, error at test/template.jte:7");
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
        givenTag("model", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "@param int i = 0\n" +
                "i is: ${i}\n" +
                "${model.getThatThrows()}");
        givenTemplate("@tag.model(model)");

        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.jte, error at tag/model.jte:4");
    }

    @Test
    void emptyTemplate() {
        givenRawTemplate("");
        thenOutputIs("");
    }

    @Test
    void emptyTag() {
        givenTag("test", "");
        givenTemplate("@tag.test()");
        thenOutputIs("");
    }

    @Test
    void emptyLayout() {
        givenLayout("test", "");
        givenTemplate(
                "@layout.test()");
        thenOutputIs("");
    }

    @Test
    void compileError0() {
        thenRenderingFailsWithException()
            .hasMessage("test/template.jte not found");
    }

    @Test
    void compileError1() {
        givenTemplate("@tag.model(model)");
        thenRenderingFailsWithException()
            .hasMessage("tag/model.jte not found, referenced at test/template.jte:2");
    }

    @Test
    void compileError2() {
        givenTemplate("Hello\n@layout.page(model)");
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
        givenTag("test", "@param gg.jte.TemplateEngineTest.Model model\nThis will not compile!\n${model.helloUnknown}\n!!");
        givenTemplate("@tag.test(model)");
        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at tag/test.jte:3\n")
                .hasMessageContaining("cannot find symbol")
                .hasMessageContaining("model.helloUnknown");
    }

    @Test
    void compileError5() {
        givenTag("test", "@param gg.jte.TemplateEngineTest.Model model\n" +
                "${\n" +
                "@`\n" +
                "This will not compile!\n${model.helloUnknown}\n!!\n" +
                "`}");
        givenTemplate("@tag.test(model)");
        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at tag/test.jte:5\n")
                .hasMessageContaining("cannot find symbol")
                .hasMessageContaining("model.helloUnknown");
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

    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name + ".kte", code);
    }

    private void givenTemplate(String template) {
        template = "@param model:gg.jte.TemplateEngine_KotlinTest.Model\n" + template;
        givenRawTemplate(template);
    }

    private void givenRawTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void givenLayout(String name, String code) {
        dummyCodeResolver.givenCode("layout/" + name + ".jte", code);
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
    public static class Model {
        public String hello;
        public int x;
        public int[] array;
        public ModelType type;

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

        public String getThatThrows() {
            throw new NullPointerException("Oops");
        }
    }

    @SuppressWarnings("unused")
    public enum ModelType {
        One, Two, Three
    }
}