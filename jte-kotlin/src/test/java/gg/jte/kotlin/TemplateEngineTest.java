package gg.jte.kotlin;

import gg.jte.*;
import gg.jte.output.StringOutput;
import gg.jte.runtime.TemplateUtils;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class TemplateEngineTest {
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
        givenRawTemplate(".".repeat(65536));
        thenOutputIs(".".repeat(65536));
    }

    @Test
    void templateWithoutParametersLongNull() {
        givenRawTemplate("\u0000".repeat(65536) + "foo");
        thenOutputIs("\u0000".repeat(65536) + "foo");
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset0() {
        givenRawTemplate("\uD83D\uDCA9".repeat(65536));
        thenOutputIs("\uD83D\uDCA9".repeat(65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset1() {
        givenRawTemplate("." + "\uD83D\uDCA9".repeat(65536));
        thenOutputIs("." + "\uD83D\uDCA9".repeat(65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset2() {
        givenRawTemplate(".." + "\uD83D\uDCA9".repeat(65536));
        thenOutputIs(".." + "\uD83D\uDCA9".repeat(65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset3() {
        givenRawTemplate("..." + "\uD83D\uDCA9".repeat(65536));
        thenOutputIs("..." + "\uD83D\uDCA9".repeat(65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset4() {
        givenRawTemplate("...." + "\uD83D\uDCA9".repeat(65536));
        thenOutputIs("...." + "\uD83D\uDCA9".repeat(65536));
    }

    @Test
    void templateWithoutParametersLongMultibyteOffset5() {
        givenRawTemplate("....." + "\uD83D\uDCA9".repeat(65536));
        thenOutputIs("....." + "\uD83D\uDCA9".repeat(65536));
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
        givenTemplate("@for (i in model.array)" +
                "${i}" +
                "@endfor");
        thenOutputIs("123");
    }

    @Test
    void loopWithCondition() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("@for (i in model.array)" +
                "@if (i > 1)" +
                "${i}" +
                "@endif" +
                "@endfor");
        thenOutputIs("23");
    }

    @Test
    void classicLoop() {
        model.array = new int[]{10, 20, 30};
        givenTemplate("@for (i in model.array.indices)" +
                "Index ${i} is ${model.array[i]}" +
                "@if (i < model.array.size - 1)" +
                "<br>" +
                "@endif" +
                "@endfor");
        thenOutputIs("Index 0 is 10<br>Index 1 is 20<br>Index 2 is 30");
    }

    @Test
    void loopWithVariable() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("@for (i in model.array)" +
                "!{var y = i + 1}" +
                "${y}" +
                "@endfor");
        thenOutputIs("234");
    }

    @Test
    void loopInContentBlock() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("${@`@for (i in model.array)" +
                "${i}" +
                "@endfor`}");
        thenOutputIs("123");
    }


    @Test
    void loopElse_empty() {
        model.array = new int[]{};
        givenTemplate("@for (i in model.array)" +
                "${i}" +
                "@else" +
                "Empty" +
                "@endfor");
        thenOutputIs("Empty");
    }

    @Test
    void loopElse_notEmpty() {
        model.array = new int[]{1};
        givenTemplate("@for (i in model.array)" +
                "${i}" +
                "@else" +
                "Empty" +
                "@endfor");
        thenOutputIs("1");
    }

    @Test
    void loopElseNested_innerEmpty() {
        templateEngine.setTrimControlStructures(true);
        model.array = new int[]{1};
        model.doubleArray = new double[]{};
        givenTemplate("""
                @for (i in model.array)
                  @for (d in model.doubleArray)
                    ${d}
                  @else
                    Inner empty
                  @endfor
                @else
                  Outer empty
                @endfor""");
        thenOutputIs("Inner empty\n");
    }

    @Test
    void loopElseNestedContentBlock_innerEmpty() {
        templateEngine.setTrimControlStructures(true);
        model.array = new int[]{1};
        model.doubleArray = new double[]{};
        givenTemplate("""
                !{var content = @`@for (i in model.array)
                  @for (d in model.doubleArray)
                    ${d}
                  @else
                    Inner empty
                  @endfor
                @else
                  Outer empty
                @endfor`;}
                Result: ${content}""");
        thenOutputIs("\nResult: Inner empty\n");
    }

    @Test
    void loopElse_if() {
        model.array = new int[]{};
        givenTemplate("@for (i in model.array)" +
                "@if(i > 0)${i}@else@endif" +
                "@else" +
                "Empty" +
                "@endfor");
        thenOutputIs("Empty");
    }

    @Test
    void loopElseNested_innerEmptyThrowsException() {
        templateEngine.setTrimControlStructures(true);
        model.array = new int[]{1};
        model.doubleArray = new double[]{};
        givenTemplate("""
                @for (i in model.array)
                  @for (d in model.doubleArray)
                    ${d}
                  @else
                    Inner empty
                    ${model.getThatThrows()}
                  @endfor
                @else
                  Outer empty
                @endfor""");

        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.kte, error at test/template.kte:7");
    }

    @Test
    void unsafeInContentBlock() {
        model.array = new int[]{1, 2, 3};
        givenTemplate("${@`$unsafe{\"\" + model.array.size}`}");
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
        givenTemplate("${@`!{var x = 5;}${x}`}");
        thenOutputIs("5");
    }

    @Test
    void jsStringInterpolationInContentBlock() {
        givenTemplate("!{var content = @`\n"
              + "    <p>Hello World!</p>\n"
              + "    @raw\n"
              + "    <script>\n"
              + "        const hello = \"Hello!\"\n"
              + "        console.log(`${hello}`)\n"
              + "    </script>\n"
              + "    @endraw\n"
              + "`}\n"
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
        givenTemplate("${if (model.x > 0) \"Yay\" else \"Nay\"}");
        thenOutputIs("Yay");
    }

    @Test
    void variable() {
        givenTemplate("!{val y = 50}${y}");
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
        givenTemplate("@template.tag.card(model.hello, model.x), That was a tag!");
        thenOutputIs("One: Hello, two: 42, That was a tag!");
    }

    @Test
    void tag_content() {
        givenTag("card", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        givenTemplate("@template.tag.card(@`<b>${model.hello}</b>`), That was a tag!");
        thenOutputIs("<span><b>Hello</b></span>, That was a tag!");
    }

    @Test
    void tag_content_comma() {
        givenTag("card", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        givenTemplate("@template.tag.card(@`<b>Hello, ${model.hello}</b>`), That was a tag!");
        thenOutputIs("<span><b>Hello, Hello</b></span>, That was a tag!");
    }

    @Test
    void tagWithGenericParam() {
        givenTag("entry", "@param list:List<String>?\n" +
                "${list.toString()}");
        givenRawTemplate("@param map:Map<String, List<String>>\n" +
                "@template.tag.entry(map[\"one\"])\n" +
                "@template.tag.entry(map[\"two\"])\n");

        Map<String, List<String>> model = new TreeMap<>();
        model.put("one", Arrays.asList("1", "2"));
        model.put("two", Arrays.asList("6", "7"));

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, model, output);

        assertThat(output.toString()).isEqualTo("[1, 2]\n[6, 7]\n");
    }

    @Test
    void tagWithMethodCallForParam() {
        givenTag("card", "@param firstParam:String\n" +
                "@param secondParam:Int\n" +
                "One: ${firstParam}, two: ${secondParam}");
        givenTemplate("@template.tag.card(model.anotherWorld, model.x), That was a tag!");
        thenOutputIs("One: Another World, two: 42, That was a tag!");
    }

    @Test
    void tagInTag() {
        givenTag("divTwo", "@param amount:Int\n" +
                "Divided by two is ${amount / 2}!");
        givenTag("card", "@param firstParam:String\n" +
                "@param secondParam:Int\n" +
                "${firstParam}, @template.tag.divTwo(secondParam)");
        givenTemplate("@template.tag.card (model.hello, model.x) That was a tag in a tag!");
        thenOutputIs("Hello, Divided by two is 21! That was a tag in a tag!");
    }

    @Test
    void sameTagReused() {
        givenTag("divTwo", "@param amount:Int\n" +
                "${amount / 2}!");
        givenTemplate("@template.tag.divTwo(model.x),@template.tag.divTwo(2 * model.x)");
        thenOutputIs("21!,42!");
    }

    @Test
    void tagRecursion() {
        givenTag("recursion", "@param amount:Int\n" +
                "${amount}" +
                "@if (amount > 0)" +
                "@template.tag.recursion(amount - 1)" +
                "@endif"
        );
        givenTemplate("@template.tag.recursion(5)");
        thenOutputIs("543210");
    }

    @Test
    void tagWithoutParams() {
        givenTag("basic", "I do nothing!");
        givenTemplate("@template.tag.basic()");
        thenOutputIs("I do nothing!");
    }

    @Test
    void tagWithoutParams_paramPassed() {
        givenTag("basic", "I do nothing!");
        givenTemplate("@template.tag.basic(42)");
        thenRenderingFailsWithException().hasMessageStartingWith("Failed to compile template, error at test/template.kte:2");
    }

    @Test
    void tagWithPackage() {
        givenTag("my/basic", "I have a custom package");
        givenTemplate("@template.tag.my.basic()");
        thenOutputIs("I have a custom package");
    }

    @Test
    void tagWithNamedParam() {
        givenTag("named", "@param one:Int\n" +
                "@param two:Int\n" +
                "${one}, ${two}");
        givenTemplate("@template.tag.named(two = 2, one = 1)");
        thenOutputIs("1, 2");
    }

    @Test
    void tagWithNamedParamString() {
        givenTag("named", "@param one:Int\n" +
                "@param two:Int\n" +
                "@param three:String\n" +
                "${one}, ${two}, ${three}");
        givenTemplate("@template.tag.named(\n" +
                "two = 2,\n" +
                "three = \"Hello, there ;-)\",\n" +
                "one = 1)");
        thenOutputIs("1, 2, Hello, there ;-)");
    }

    @Test
    void tagWithNamedParam_ternary() {
        givenTag("named", "@param one:Int\n" +
                "@param two:Int\n" +
                "${one}, ${two}");
        givenTemplate("@template.tag.named(two = if(1 == 2) 1 else 0, one = 1)");
        thenOutputIs("1, 0");
    }

    @Test
    void tagWithDefaultParam() {
        givenTag("named", "@param one:Int = 1\n" +
                "@param two:Int = 2\n" +
                "${one}, ${two}");
        givenTemplate("@template.tag.named()");

        thenOutputIs("1, 2");
    }

    @Test
    void tagWithDefaultContentParam() {
        givenTag("named", "@param one:Int = 1\n" +
                          "@param content:gg.jte.Content = @`Some Content`\n" +
                          "First param = ${one}, Content param = ${content}");
        givenTemplate("@template.tag.named()");

        thenOutputIs("First param = 1, Content param = Some Content");
    }

    @Test
    void tagWithDefaultParam_generic() {
        givenTag("named", "@param files: Map<String, ByteArray> = emptyMap()\n" +
                "${files.size}");
        givenTemplate("@template.tag.named()");

        thenOutputIs("0");
    }

    @Test
    void tagWithDefaultParam_generic_nullDefault() {
        givenTag("named", "@param files: Map<String, ByteArray>? = null\n" +
                "${files?.size}");
        givenTemplate("@template.tag.named()");

        thenOutputIs("");
    }

    @Test
    void tagWithDefaultParam_generic_nullDefault_withValue() {
        givenTag("named", "@param files: Map<String, ByteArray>? = null\n" +
                "${files?.size}");
        givenTemplate("@template.tag.named(mapOf(\"foo\" to ByteArray(1)))");

        thenOutputIs("1");
    }

    @Test
    void tagWithDefaultParam_firstSet() {
        givenTag("named", "@param one:Int = 1\n" +
                "@param two:Int = 2\n" +
                "${one}, ${two}");
        givenTemplate("@template.tag.named(one = 6)");

        thenOutputIs("6, 2");
    }

    @Test
    void tagWithDefaultParam_secondSet() {
        givenTag("named", "@param one:Int = 1\n" +
                "@param two:Int = 2\n" +
                "${one}, ${two}");
        givenTemplate("@template.tag.named(two= 5)");

        thenOutputIs("1, 5");
    }

    @Test
    void templateWithDefaultParam_content() {
        givenTag("named", "@param one:gg.jte.Content = @`This is`\n" +
                "@param two:Int = 2\n" +
                "${one}: ${two}");
        givenTemplate("@template.tag.named()");

        thenOutputIs("This is: 2");
    }

    @Test
    void tagWithVarArgs1() {
        givenTag("varargs",
                "@param vararg values:String\n" +
                "@for(value in values)${value} @endfor");
        givenTemplate("@template.tag.varargs(\"Hello\")");
        thenOutputIs("Hello ");
    }

    @Test
    void tagWithVarArgs2() {
        givenTag("varargs",
                "@param vararg values:String\n" +
                "@for(value in values)${value} @endfor");
        givenTemplate("@template.tag.varargs(\"Hello\", \"World\")");
        thenOutputIs("Hello World ");
    }

    @Test
    void tagWithVarArgs3() {
        givenTag("localize",
                "@param key:String\n" +
                "@param vararg values:String\n" +
                "${key} with @for(value in values)${value} @endfor");
        givenTemplate("@template.tag.localize(key = \"test.key\", \"Hello\", \"World\")");
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
        givenRawTemplate("<%--This is a comment--%>@import gg.jte.kotlin.TemplateEngineTest.Model\n@param model:Model\n" + "!{model.setX(12)}${model.x}");
        thenOutputIs("12");
    }

    @Test
    void commentBeforeParams() {
        givenRawTemplate("<%--This is a comment--%>@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" + "!{model.setX(12)}${model.x}");
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
        thenRenderingFailsWithException().hasMessage("Failed to compile test/template.kte, error at line 1: Missing parameter type: '@param int'");
    }

    @Test
    void paramWithDefaultValue() {
        givenRawTemplate("@param age: Int = 10\nYour age is ${age}");

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, TemplateUtils.toMap(), output);

        assertThat(output.toString()).isEqualTo("Your age is 10");
    }

    @Test
    void contentParamWithDefaultValue() {
        givenRawTemplate("@param content:gg.jte.Content = @`Some Content`\nThe default content is ${content}");

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, TemplateUtils.toMap(), output);

        assertThat(output.toString()).isEqualTo("The default content is Some Content");
    }

    @Test
    void layout() {
        givenLayout("main", "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                "@param content:gg.jte.Content\n" +
                "@param footer:gg.jte.Content\n" +
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
        givenLayout("main",
                "@param header:gg.jte.Content? = null\n" +
                "@param content:gg.jte.Content\n" +
                "@param footer:gg.jte.Content? = null\n" +
                "@if(header != null)<header>${header}</header>@endif" +
                "<content>${content}</content>" +
                "@if(footer != null)<footer>${footer}</footer>@endif");
        givenLayout("mainExtended",
                "@param header:gg.jte.Content? = null\n" +
                "@param contentPrefix:gg.jte.Content? = null\n" +
                "@param content:gg.jte.Content\n" +
                "@param contentSuffix:gg.jte.Content? = null\n" +
                "@param footer:gg.jte.Content? = null\n" +
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
        givenLayout("main",
                "@param status:Int = 5\n" +
                "@param duration:Int = -1\n" +
                "@param content:gg.jte.Content\n" +
                "Hello, ${content} your status is ${status}, the duration is ${duration}");

        givenTemplate("@template.layout.main(content = @`" +
                "Sir`)");

        thenOutputIs("Hello, Sir your status is 5, the duration is -1");
    }

    @Test
    void layoutWithNamedParams_noNames() {
        givenLayout("main",
                "@param status:Int = 5\n" +
                "@param duration:Int = -1\n" +
                "@param content:gg.jte.Content\n" +
                "Hello, ${content} your status is ${status}, the duration is ${duration}");

        givenTemplate("@template.layout.main(42, 10, @`Sir`)");

        thenOutputIs("Hello, Sir your status is 42, the duration is 10");
    }

    @Test
    void layoutWithVarArgs() {
        givenLayout("varargs",
                "@param vararg values:String\n" +
                        "@for(value in values)${value} @endfor");
        givenTemplate("@template.layout.varargs(\"Hello\", \"World\")");
        thenOutputIs("Hello World ");
    }

    @Test
    void enumCheck() {
        givenRawTemplate(
                "@import gg.jte.kotlin.TemplateEngineTest.Model\n" +
                        "@import gg.jte.kotlin.TemplateEngineTest.ModelType\n" +
                        "@param model:Model\n" +
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
        templateName = "snake-case.kte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void classPrefix() {
        templateName = "test/404.kte";
        givenTemplate("Hello");
        thenOutputIs("Hello");
    }

    @Test
    void escaping() {
        givenTemplate("\" \n \t \r \f \b \\ $");
        thenOutputIs("\" \n \t \r \f \b \\ $");
    }

    @Test
    void npe_internal() {
        givenTemplate("This is ${model.getThatThrows()} world");

        thenRenderingFailsWithExceptionCausedBy(NullPointerException.class);
    }

    @Test
    void exceptionLineNumber1() {
        givenRawTemplate(
                "@import gg.jte.kotlin.TemplateEngineTest.Model\n" +
                "\n" +
                "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                "\n" +
                "${model.getThatThrows()}\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.kte, error at test/template.kte:5")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.test.JtetemplateGenerated$Companion.render(test/template.kte:5)");
    }

    @Test
    void exceptionLineNumber2() {
        givenRawTemplate(
                "@import gg.jte.kotlin.TemplateEngineTest.Model\n" +
                        "\n\n\n" +
                        "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                        "\n" +
                        "${model.getThatThrows()}\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.kte, error at test/template.kte:7");
    }

    @Test
    void exceptionLineNumber3() {
        givenRawTemplate(
                "@import gg.jte.kotlin.TemplateEngineTest.Model\n" +
                        "\n" +
                        "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                        "\n" +
                        "${model.hello} ${model.getThatThrows()}\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.kte, error at test/template.kte:5");
    }

    @Test
    void exceptionLineNumber4() {
        givenRawTemplate(
                "@import gg.jte.kotlin.TemplateEngineTest.Model\n" +
                        "\n" +
                        "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                        "\n" +
                        "${model.hello}\n" +
                        "@for(i in 1..3)\n" +
                        "\t${i}\n" +
                        "\t${model.getThatThrows()}\n" +
                        "@endfor\n"
        );
        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.kte, error at test/template.kte:8");
    }

    @Test
    void exceptionLineNumber5() {
        givenTag("model", "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                "@param i:Int = 0\n" +
                "i is: ${i}\n" +
                "${model.getThatThrows()}");
        givenTemplate("@template.tag.model(model)");

        thenRenderingFailsWithException()
                .hasCauseInstanceOf(NullPointerException.class)
                .hasMessage("Failed to render test/template.kte, error at tag/model.kte:4")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.tag.JtemodelGenerated$Companion.render(tag/model.kte:4)")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.test.JtetemplateGenerated$Companion.render(test/template.kte:2)");
    }

    @Test
    void exceptionLineNumber6() {
        givenTag("model", "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                "@param i:Int = 0\n" +
                "i is: ${i}\n" +
                "${model.getThatThrows()}");

        @SuppressWarnings("resource")
        StringOutput output = new StringOutput();
        Throwable throwable = catchThrowable(() -> templateEngine.render("tag/model.kte", TemplateUtils.toMap("model", model, "i", 1L), output));

        assertThat(throwable)
                .hasCauseInstanceOf(ClassCastException.class)
                .hasMessage("Failed to render tag/model.kte, error at tag/model.kte:2")
                .hasStackTraceContaining("at gg.jte.generated.ondemand.tag.JtemodelGenerated$Companion.renderMap(tag/model.kte:2)");
    }

    @Test
    void emptyTemplate() {
        givenRawTemplate("");
        thenOutputIs("");
    }

    @Test
    void emptyTag() {
        givenTag("test", "");
        givenTemplate("@template.tag.test()");
        thenOutputIs("");
    }

    @Test
    void emptyLayout() {
        givenLayout("test", "");
        givenTemplate("@template.layout.test()");
        thenOutputIs("");
    }

    @Test
    void compileError0() {
        thenRenderingFailsWithException()
            .hasMessage("test/template.kte not found");
    }

    @Test
    void compileError1() {
        givenTemplate("@template.tag.model(model)");
        thenRenderingFailsWithException()
            .hasMessage("tag/model.kte not found, referenced at test/template.kte:2");
    }

    @Test
    void compileError2() {
        givenTemplate("Hello\n@template.layout.page(model)");
        thenRenderingFailsWithException()
                .hasMessage("layout/page.kte not found, referenced at test/template.kte:3");
    }

    @Test
    void compileError3() {
        givenTemplate("${model.hello}\n" +
                "${model.hello}\n" +
                "${model.helloUnknown}");

        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at test/template.kte:4\n")
                .hasMessageContaining("Unresolved reference")
                .hasMessageContaining("model.helloUnknown");
    }

    @Test
    void compileError4() {
        givenTag("test", "@param model:gg.jte.kotlin.TemplateEngineTest.Model\nThis will not compile!\n${model.helloUnknown}\n!!");
        givenTemplate("@template.tag.test(model)");
        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at tag/test.kte:3\n")
                .hasMessageContaining("Unresolved reference")
                .hasMessageContaining("model.helloUnknown");
    }

    @Test
    void compileError5() {
        givenTag("test", "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" +
                "${\n" +
                "@`\n" +
                "This will not compile!\n${model.helloUnknown}\n!!\n" +
                "`}");
        givenTemplate("@template.tag.test(model)");
        thenRenderingFailsWithException()
                .hasMessageStartingWith("Failed to compile template, error at tag/test.kte:5\n")
                .hasMessageContaining("Unresolved reference")
                .hasMessageContaining("model.helloUnknown");
    }

    @Test
    void compileError6() {
        givenTag("test", "@param model:gg.jte.TemplateEngineTest.Model\n" +
                "test");
        givenTemplate("@template.tag.test(\n" +
                "model = model,\n" +
                "param2 = \"foo\")");
        thenRenderingFailsWithException()
                .hasMessage("Failed to compile template, error at test/template.kte:3. No parameter with name param2 is defined in tag/test.kte");
    }

    @Test
    void calledWithWrongParam1() {
        givenRawTemplate("@param hello:String\n${hello}");
        thenRenderingFailsWithException().hasMessage("Failed to render test/template.kte, type mismatch for parameter: Expected java.lang.String, got gg.jte.kotlin.TemplateEngineTest$Model");
    }

    @Test
    void calledWithWrongParam2() {
        givenRawTemplate("@param x:Int\n${x}");
        thenRenderingFailsWithException().hasMessage("Failed to render test/template.kte, type mismatch for parameter: Expected int, got gg.jte.kotlin.TemplateEngineTest$Model");
    }

    @Test
    void calledWithWrongParam3() {
        model = null;
        givenRawTemplate("@param x:Int\n${x}");
        thenRenderingFailsWithException().hasMessage("Failed to render test/template.kte, type mismatch for parameter: Expected int, got null");
    }

    @Test
    void getParamInfo_none() {
        givenRawTemplate("Hello World!");
        Map<String, Class<?>> params = templateEngine.getParamInfo(templateName);
        assertThat(params).isEmpty();
    }

    @Test
    void getParamInfo_one() {
        givenRawTemplate("@param foo:Int\nHello World!");
        Map<String, Class<?>> params = templateEngine.getParamInfo(templateName);
        assertThat(params).hasSize(1);
        assertThat(params).containsEntry("foo", int.class);
    }

    @Test
    void getParamInfo_some() {
        givenRawTemplate("@import gg.jte.Content\n@param foo:Int\n@param content:Content\nHello World!");
        Map<String, Class<?>> params = templateEngine.getParamInfo(templateName);
        assertThat(params).hasSize(2);
        assertThat(params).containsEntry("foo", int.class);
        assertThat(params).containsEntry("content", Content.class);
    }

    @Test
    void getParamInfo_lazy() {
        givenRawTemplate("@param foo:Int\nHello World!");
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
    void mixedTemplates() {
        StringOutput jteOutput = new StringOutput();
        StringOutput kteOutput = new StringOutput();
        dummyCodeResolver.givenCode("tag/foo.jte", "Hello jte");
        dummyCodeResolver.givenCode("tag/bar.kte", "Hello kte");

        templateEngine.render("tag/foo.jte", Collections.emptyMap(), jteOutput);
        templateEngine.render("tag/bar.kte", Collections.emptyMap(), kteOutput);

        assertThat(jteOutput.toString()).isEqualTo("Hello jte");
        assertThat(kteOutput.toString()).isEqualTo("Hello kte");
    }

    @Test
    void mixedTemplates_callKteFromJte() {
        dummyCodeResolver.givenCode(templateName = "main.jte", "Hello @template.tag.name()");
        dummyCodeResolver.givenCode("tag/name.kte", "Kotlin!");

        thenOutputIs("Hello Kotlin!");
    }

    @Test
    void mixedTemplates_callJteFromKte() {
        dummyCodeResolver.givenCode(templateName = "main.kte", "Hello @template.tag.name()");
        dummyCodeResolver.givenCode("tag/name.jte", "Java!");

        thenOutputIs("Hello Java!");
    }

    @Test
    void mixedTemplates_callKteFromJte_withParams() {
        dummyCodeResolver.givenCode(templateName = "main.jte", "Hello, @template.tag.name(b = \"foo\", a = 42)");
        dummyCodeResolver.givenCode("tag/name.kte", "@param a:Int\n@param b:String\nKotlin says ${a}, ${b}!");

        thenOutputIs("Hello, Kotlin says 42, foo!");
    }

    @Test
    void mixedTemplates_callJteFromKte_withParams() {
        dummyCodeResolver.givenCode(templateName = "main.kte", "Hello, @template.tag.name(b = \"foo\", a = 42)");
        dummyCodeResolver.givenCode("tag/name.jte", "@param int a\n@param String b\nJava says ${a}, ${b}!");

        thenOutputIs("Hello, Java says 42, foo!");
    }

    @Test
    void mixedTemplates_callJteFromKte_generateAll() {
        dummyCodeResolver.givenCode("a.jte", "Java!"); // Hack: named 'a' to be generated first
        dummyCodeResolver.givenCode(templateName = "b.kte", "Hello @template.a()");

        List<String> generated = templateEngine.generateAll();

        assertThat(generated).hasSize(2);
    }

    @Test
    void nestedKotlinStringTemplates() {
        givenRawTemplate("@import java.time.format.DateTimeFormatter\n" +
                "@import java.time.LocalDateTime\n" +
                "@param now: LocalDateTime\n" +
                "@param datePattern: String\n" +
                "println(\"Today is ${now.format(DateTimeFormatter.ofPattern(\"${datePattern} HH:mm\"))}\")");

        LocalDateTime now = LocalDateTime.of(2021, 8, 21, 7, 50);

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, TemplateUtils.toMap("now", now, "datePattern", "YYYY-MM-dd"), output);

        assertThat(output.toString()).isEqualTo("println(\"Today is 2021-08-21 07:50\")");
    }

    @Test
    void nestedKotlinStringTemplates_unsafe() {
        givenRawTemplate("@import java.time.format.DateTimeFormatter\n" +
                "@import java.time.LocalDateTime\n" +
                "@param now: LocalDateTime\n" +
                "@param datePattern: String\n" +
                "println(\"Today is $unsafe{now.format(DateTimeFormatter.ofPattern(\"${datePattern} HH:mm\"))}\")");

        LocalDateTime now = LocalDateTime.of(2021, 8, 21, 7, 50);

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, TemplateUtils.toMap("now", now, "datePattern", "YYYY-MM-dd"), output);

        assertThat(output.toString()).isEqualTo("println(\"Today is 2021-08-21 07:50\")");
    }

    @Test
    void nestedKotlinStringTemplates_variable() {
        givenRawTemplate("@import java.time.format.DateTimeFormatter\n" +
                "@import java.time.LocalDateTime\n" +
                "@param now: LocalDateTime\n" +
                "@param datePattern: String\n" +
                "!{val variable = now.format(DateTimeFormatter.ofPattern(\"${datePattern} HH:mm\"))}" +
                "println(\"Today is ${variable}\")");

        LocalDateTime now = LocalDateTime.of(2021, 8, 21, 7, 50);

        StringOutput output = new StringOutput();
        templateEngine.render(templateName, TemplateUtils.toMap("now", now, "datePattern", "YYYY-MM-dd"), output);

        assertThat(output.toString()).isEqualTo("println(\"Today is 2021-08-21 07:50\")");
    }

    @Test
    void spaceAfterParameterName() {
        givenRawTemplate("@param model :gg.jte.kotlin.TemplateEngineTest.Model\nHello ${model.x}");
        thenOutputIs("Hello 42");
    }

    @Test
    void rawWithJavaScript() {
        givenTemplate("@raw\n" +
                      "<script>\n" +
                      "  const foo = \"foo\";\n" +
                      "  console.log(`This is ${foo}`);\n" +
                      "</script>\n" +
                      "@endraw");
        thenOutputIs("\n" +
                     "<script>\n" +
                     "  const foo = \"foo\";\n" +
                     "  console.log(`This is ${foo}`);\n" +
                     "</script>\n");
    }

    private void givenTag(String name, String code) {
        dummyCodeResolver.givenCode("tag/" + name + ".kte", code);
    }

    private void givenTemplate(String template) {
        template = "@param model:gg.jte.kotlin.TemplateEngineTest.Model\n" + template;
        givenRawTemplate(template);
    }

    private void givenRawTemplate(String template) {
        dummyCodeResolver.givenCode(templateName, template);
    }

    private void givenLayout(String name, String code) {
        dummyCodeResolver.givenCode("layout/" + name + ".kte", code);
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