package org.jusecase.jte;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.jte.internal.TemplateCompiler;
import org.jusecase.jte.output.StringOutput;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngineTest {
    String templateName = "test/template.jte";

    DummyCodeResolver dummyCodeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(dummyCodeResolver);
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
                "!{var y = i + 1}" +
                "${y}" +
                "@endfor");
        thenOutputIs("234");
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
        givenTag("card", "@param java.lang.String firstParam\n" +
                "@param int secondParam\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@tag.card(model.hello, model.x), That was a tag!");
        thenOutputIs("One: Hello, two: 42, That was a tag!");
    }

    @Test
    void tagWithGenericParam() {
        givenTag("entry", "@param java.util.Map.Entry<String, java.util.List<String>> entry\n" +
                "${entry.getKey()}: ${entry.getValue()}");
        givenRawTemplate("@param java.util.Map<String, java.util.List<String>> map\n" +
                "@for(var entry : map.entrySet())@tag.entry(entry)\n@endfor");

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
    void layout() {
        givenLayout("main", "@param org.jusecase.jte.TemplateEngineTest.Model model\n" +
                "\n" +
                "<body>\n" +
                "<b>Welcome to my site - you are on page ${model.x}</b>\n" +
                "\n" +
                "<div class=\"content\">\n" +
                "    @render(content)\n" +
                "</div>\n" +
                "\n" +
                "<div class=\"footer\">\n" +
                "    @render(footer)\n" +
                "</div>\n" +
                "</body>");

        givenTemplate("@layout.main(model)\n" +
                "    @define(content)\n" +
                "        ${model.hello}, enjoy this great content\n" +
                "    @enddefine\n" +
                "    @define(footer)\n" +
                "        Come again!\n" +
                "    @enddefine\n" +
                "@endlayout");

        thenOutputIs("\n" +
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
                "<header>@render(header)</header>" +
                        "<content>@render(content)</content>" +
                        "<footer>@render(footer)</footer>");
        givenLayout("mainExtended", "@layout.main()" +
                "@define(content)" +
                "@render(contentPrefix)" +
                "<b>@render(content)</b>" +
                "@render(contentSuffix)" +
                "@enddefine" +
                "@endlayout");
        givenTemplate("@layout.mainExtended()" +
                "@define(header)" +
                "this is the header" +
                "@enddefine" +
                "@define(contentPrefix)" +
                "<content-prefix>" +
                "@enddefine" +
                "@define(content)" +
                "this is the content" +
                "@enddefine" +
                "@define(contentSuffix)" +
                "<content-suffix>" +
                "@enddefine" +
                "@endlayout");

        thenOutputIs("<header>this is the header</header>" +
                "<content><content-prefix><b>this is the content</b><content-suffix></content>" +
                "<footer></footer>");
    }

    @Test
    void layoutWithNamedParams() {
        givenLayout("main",
                "@param int status = 5\n" +
                        "@param int duration = -1\n" +
                        "Hello, @render(content) your status is ${status}, the duration is ${duration}");

        givenTemplate("@layout.main()" +
                "@define(content)Sir@enddefine" +
                "@endlayout");

        thenOutputIs("Hello, Sir your status is 5, the duration is -1");
    }

    @Test
    void layoutWithVarArgs() {
        givenLayout("varargs",
                "@param String ... values\n" +
                        "@for(String value : values)${value} @endfor");
        givenTemplate("@layout.varargs(\"Hello\", \"World\")@endlayout");
        thenOutputIs("Hello World ");
    }

    @Test
    void enumCheck() {
        givenRawTemplate(
                "@import org.jusecase.jte.TemplateEngineTest.Model\n" +
                        "@import org.jusecase.jte.TemplateEngineTest.ModelType\n" +
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
    void npe_if() {
        model = null;
        givenTemplate("@if(model.hello.equals(\"dummy\"))" +
                "yes" +
                "@else" +
                "no" +
                "@endif");

        thenRenderingFailsWithExceptionCausedBy(NullPointerException.class);
    }

    @Test
    void npe_nullSafe_if() {
        templateEngine.setNullSafeTemplateCode(true);
        model = null;
        givenTemplate("@if(model.hello.equals(\"dummy\"))" +
                "yes" +
                "@else" +
                "no" +
                "@endif");

        thenOutputIs("no");
    }

    @Test
    void npe_nullSafe_elseif() {
        templateEngine.setNullSafeTemplateCode(true);
        model = null;
        givenTemplate("@if(model.hello.equals(\"yes\"))" +
                "yes" +
                "@elseif(model.hello.equals(\"no\"))" +
                "no" +
                "@endif");

        thenOutputIs("");
    }

    @Test
    void npe_output() {
        model = null;
        givenTemplate("${model.hello} world");

        thenRenderingFailsWithExceptionCausedBy(NullPointerException.class);
    }

    @Test
    void npe_nullSafe_output() {
        templateEngine.setNullSafeTemplateCode(true);
        model = null;
        givenTemplate("This is ${model.hello} world");

        thenOutputIs("This is  world");
    }

    @Test
    void npe_nullSafe_output_int() {
        templateEngine.setNullSafeTemplateCode(true);
        model = null;
        givenTemplate("This is ${model.x}.");

        thenOutputIs("This is .");
    }

    @Test
    void npe_nullSafe_output_object() {
        templateEngine.setNullSafeTemplateCode(true);
        model = null;
        givenTemplate("This is ${model.type} world");

        thenOutputIs("This is  world");
    }

    @Test
    void npe_nullSafe_unsafeOutput() {
        templateEngine.setNullSafeTemplateCode(true);
        model = null;
        givenTemplate("This is $unsafe{model.hello} world");

        thenOutputIs("This is  world");
    }

    @Test
    void npe_internal_stillThrown() {
        templateEngine.setNullSafeTemplateCode(true);
        givenTemplate("This is ${model.getThatThrows()} world");

        thenRenderingFailsWithExceptionCausedBy(NullPointerException.class);
    }

    // Making for null-safe would be very messy and require an extra dependency,
    // to extract generic lambda parameters, so it's been reverted.
    // The null-safe mode is not the default setting and not encouraged to use anyways.
    @Test
    void npe_for() {
        model = null;
        givenTemplate("@for (int i : model.array)" +
                "${i}" +
                "@endfor");
        thenRenderingFailsWithExceptionCausedBy(NullPointerException.class);
    }

    @Test
    void npe_statement() {
        model = null;
        templateEngine.setNullSafeTemplateCode(true);
        givenTemplate("!{var x = model.hello.length()}${x}");
        thenOutputIs("");
    }

    @Test
    void npe_statement_notNull() {
        templateEngine.setNullSafeTemplateCode(true);
        givenTemplate("!{var x = model.hello.length()}${x}");
        thenOutputIs("5");
    }

    @Test
    void npe_call() {
        model = null;
        templateEngine.setNullSafeTemplateCode(true);
        givenTemplate("!{model.hello.length()}");
        thenOutputIs("");
    }

    @Test
    void exceptionLineNumber1() {
        givenRawTemplate(
                "@import org.jusecase.jte.TemplateEngineTest.Model\n" +
                "\n" +
                "@param org.jusecase.jte.TemplateEngineTest.Model model\n" +
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
                "@import org.jusecase.jte.TemplateEngineTest.Model\n" +
                        "\n\n\n" +
                        "@param org.jusecase.jte.TemplateEngineTest.Model model\n" +
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
                "@import org.jusecase.jte.TemplateEngineTest.Model\n" +
                        "\n" +
                        "@param org.jusecase.jte.TemplateEngineTest.Model model\n" +
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
                "@import org.jusecase.jte.TemplateEngineTest.Model\n" +
                        "\n" +
                        "@param org.jusecase.jte.TemplateEngineTest.Model model\n" +
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
        givenTag("model", "@param org.jusecase.jte.TemplateEngineTest.Model model\n" +
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
                "@layout.test()\n" +
                "@endlayout");
        thenOutputIs("");
    }

    @Test
    void compileError0() {
        thenRenderingFailsWithException()
            .hasMessage("Template not found: test/template.jte");
    }

    @Test
    void compileError1() {
        givenTemplate("@tag.model(model)");
        thenRenderingFailsWithException()
            .hasMessage("Tag not found: tag/model.jte, referenced at test/template.jte:2");
    }

    @Test
    void compileError2() {
        givenTemplate("Hello\n@layout.page(model)");
        thenRenderingFailsWithException()
                .hasMessage("Layout not found: layout/page.jte, referenced at test/template.jte:3");
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
        givenTag("test", "@param org.jusecase.jte.TemplateEngineTest.Model model\nThis will not compile!\n${model.helloUnknown}\n!!");
        givenTemplate("@tag.test(model)");
        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at tag/test.jte:3\n")
                .hasMessageContaining("cannot find symbol")
                .hasMessageContaining("model.helloUnknown");
    }

    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name + TemplateCompiler.TAG_EXTENSION, code);
    }

    private void givenTemplate(String template) {
        template = "@param org.jusecase.jte.TemplateEngineTest.Model model\n" + template;
        givenRawTemplate(template);
    }

    private void givenRawTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void givenLayout(String name, String code) {
        dummyCodeResolver.givenCode("layout/" + name + TemplateCompiler.LAYOUT_EXTENSION, code);
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