package gg.jte.kotlin;

import gg.jte.Content;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateException;
import gg.jte.html.OwaspHtmlPolicy;
import gg.jte.html.policy.PreventInlineEventHandlers;
import gg.jte.html.policy.PreventSingleQuotedAttributes;
import gg.jte.output.StringOutput;
import gg.jte.runtime.TemplateUtils;
import gg.jte.support.LocalizationSupport;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


public class TemplateEngine_HtmlOutputEscapingTest {

    DummyCodeResolver codeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

    MyLocalizer localizer = new MyLocalizer();
    StringOutput output = new StringOutput();

    @Test
    void outputEscaping() {
        codeResolver.givenCode("template.kte",
                "@param url:String\n" +
                "@param title:String\n" +
                "Look at <a href=\"${url}\">${title}</a>");

        templateEngine.render("template.kte", TemplateUtils.toMap("url", "https://www.test.com?param1=1&param2=2", "title", "<script>alert('hello');</script>"), output);

        assertThat(output.toString()).isEqualTo("Look at <a href=\"https://www.test.com?param1=1&amp;param2=2\">&lt;script&gt;alert('hello');&lt;/script&gt;</a>");
    }

    @Test
    void unclosedTag() {
        codeResolver.givenCode("unclosed.kte", "<span><a></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("unclosed.kte", null, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile unclosed.kte, error at line 1: Unclosed tag <a>, expected </a>, got </span>.");
    }

    @Test
    void codeInTag() {
        codeResolver.givenCode("template.kte", "@param String tag\n\n<span><${tag}/></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "br", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.kte, error at line 3: Illegal HTML tag name ${tag}! Expressions in HTML tag names are not allowed.");
    }

    @Test
    void codeInTag_unsafe() {
        codeResolver.givenCode("template.kte", "@param tag:String\n\n<span><$unsafe{tag}/></span>");

        templateEngine.render("template.kte", "br", output);

        assertThat(output.toString()).isEqualTo("\n<span><br/></span>");
    }

    @Test
    void codeInAttribute() {
        codeResolver.givenCode("template.kte", "@param String attribute\n\n<span ${attribute}=\"value\"></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "class", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.kte, error at line 3: Illegal HTML attribute name ${attribute}! Expressions in HTML attribute names are not allowed.");
    }

    @Test
    void codeInAttribute_unsafe() {
        codeResolver.givenCode("template.kte", "@param attribute:String\n\n<span $unsafe{attribute}=\"value\"></span>");

        templateEngine.render("template.kte", "class", output);

        assertThat(output.toString()).isEqualTo("\n<span class=\"value\"></span>");
    }

    @Test
    void commentIsNotConfusedWithHtmlTag() {
        codeResolver.givenCode("template.kte", "@param name:String\n<%-- Comment --%>\n<p>Hello ${name}</p>");

        templateEngine.render("template.kte", "John", output);

        assertThat(output.toString()).isEqualTo("\n<p>Hello John</p>");
    }

    @Test
    void attributes() {
        codeResolver.givenCode("template.kte", "@param userName:String\n<div data-title=\"Hello ${userName}\"></div>");

        templateEngine.render("template.kte", "\"><script>alert('xss')</script>", output);

        assertThat(output.toString()).isEqualTo("<div data-title=\"Hello &#34;>&lt;script>alert(&#39;xss&#39;)&lt;/script>\"></div>");
    }

    @Test
    void attributes_null() {
        codeResolver.givenCode("template.kte", "@param title:String?\n<button data-title=\"${title}\">Click</button>");

        templateEngine.render("template.kte", (String)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_string() {
        codeResolver.givenCode("template.kte", "@param title:String\n<button data-title=\"${title}\">Click</button>");

        templateEngine.render("template.kte", "The title", output);

        assertThat(output.toString()).isEqualTo("<button data-title=\"The title\">Click</button>");
    }

    @Test
    void attributes_byte() {
        codeResolver.givenCode("template.kte", "@param value:Byte\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", (byte)42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_short() {
        codeResolver.givenCode("template.kte", "@param value:Short\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", (short)42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_int() {
        codeResolver.givenCode("template.kte", "@param value:Int\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", 42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_long() {
        codeResolver.givenCode("template.kte", "@param value:Long\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", 42L, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_float() {
        codeResolver.givenCode("template.kte", "@param value:Float\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", 42.5f, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42.5\">Click</button>");
    }

    @Test
    void attributes_double() {
        codeResolver.givenCode("template.kte", "@param value:Double\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", 42.5, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42.5\">Click</button>");
    }

    @Test
    void attributes_Integer() {
        codeResolver.givenCode("template.kte", "@param value:Int?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", 42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_Integer_null() {
        codeResolver.givenCode("template.kte", "@param value:Int?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", (Integer)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_Character() {
        codeResolver.givenCode("template.kte", "@param value:Char?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", 'x', output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"x\">Click</button>");
    }

    @Test
    void attributes_Character_null() {
        codeResolver.givenCode("template.kte", "@param value:Char?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", (Character)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_Boolean() {
        codeResolver.givenCode("template.kte", "@param value:Boolean?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", Boolean.FALSE, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_Boolean2() {
        codeResolver.givenCode("template.kte", "@param value:Boolean?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", false, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_Boolean_null() {
        codeResolver.givenCode("template.kte", "@param value:Boolean?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", (Boolean)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_content() {
        codeResolver.givenCode("template.kte", "@param content:gg.jte.Content?\n<button data-title = \"${content}\">Click</button>");

        templateEngine.render("template.kte", (Content) output -> output.writeContent("Hello \"My friend\"!"), output);

        assertThat(output.toString()).isEqualTo("<button data-title=\"Hello &#34;My friend&#34;!\">Click</button>");
    }

    @Test
    void attributes_content_null() {
        codeResolver.givenCode("template.kte", "@param content:gg.jte.Content?\n<button data-title = \"${content}\">Click</button>");

        templateEngine.render("template.kte", (Content)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_multipleOutputs() {
        codeResolver.givenCode("template.kte", "@param title:String?\n<button data-title-x2=\"${title} + ${title}\">Click</button>");

        templateEngine.render("template.kte", "The title", output);

        assertThat(output.toString()).isEqualTo("<button data-title-x2=\"The title + The title\">Click</button>");
    }

    @Test
    void attributes_multipleOutputs_null() {
        codeResolver.givenCode("template.kte", "@param title:String?\n<button data-title-x2=\"${title} + ${title}\">Click</button>");

        templateEngine.render("template.kte", (String)null, output);

        assertThat(output.toString()).isEqualTo("<button data-title-x2=\" + \">Click</button>");
    }

    @Test
    void attributes_boolean_true() {
        codeResolver.givenCode("template.kte", "@param visible:Boolean\n<button data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.kte", true, output);

        assertThat(output.toString()).isEqualTo("<button data-visible=\"true\">Click</button>");
    }

    @Test
    void attributes_boolean_false() {
        codeResolver.givenCode("template.kte", "@param visible:Boolean\n<button data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.kte", false, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_boolean_false_attributeBefore() {
        codeResolver.givenCode("template.kte", "@param visible:Boolean\n<button data-test data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.kte", false, output);

        assertThat(output.toString()).isEqualTo("<button data-test>Click</button>");
    }

    @Test
    void attributes_boolean_true_attributeBefore() {
        codeResolver.givenCode("template.kte", "@param visible:Boolean\n<button data-test data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.kte", true, output);

        assertThat(output.toString()).isEqualTo("<button data-test data-visible=\"true\">Click</button>");
    }

    @Test
    void attributes_booleanExpression1() {
        codeResolver.givenCode("template.kte", "@param visible:Boolean\n<button data-visible=\"${visible == true}\" data-invisible=\"${!visible}\">Click</button>");

        templateEngine.render("template.kte", true, output);

        assertThat(output.toString()).isEqualTo("<button data-visible=\"true\">Click</button>");
    }

    @Test
    void attributes_booleanExpression2() {
        codeResolver.givenCode("template.kte", "@param visible:Boolean\n<button data-visible=\"${visible == true}\" data-invisible=\"${!visible}\">Click</button>");

        templateEngine.render("template.kte", false, output);

        assertThat(output.toString()).isEqualTo("<button data-invisible=\"true\">Click</button>");
    }

    @Test
    void booleanAttributes_true() {
        codeResolver.givenCode("template.kte", "@param disabled:Boolean\n<button disabled=\"${disabled}\">Click</button>");

        templateEngine.render("template.kte", true, output);

        assertThat(output.toString()).isEqualTo("<button disabled>Click</button>");
    }

    @Test
    void booleanAttributes_false() {
        codeResolver.givenCode("template.kte", "@param disabled:Boolean\n<button disabled=\"${disabled}\">Click</button>");

        templateEngine.render("template.kte", false, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void booleanAttribute_noJavaExpression() {
        codeResolver.givenCode("template.kte", "@param disabled:Boolean\n<button disabled=\"disabled\">Click</button>");

        templateEngine.render("template.kte", false, output);

        assertThat(output.toString()).isEqualTo("<button disabled=\"disabled\">Click</button>");
    }

    @Test
    void booleanAttributes_noParams() {
        codeResolver.givenCode("template.kte", "@param cssClass:String\n<button disabled class=\"${cssClass}\">Click</button>");

        templateEngine.render("template.kte", "dummy", output);

        assertThat(output.toString()).isEqualTo("<button disabled class=\"dummy\">Click</button>");
    }

    @Test
    void booleanAttributes_noParams2() {
        codeResolver.givenCode("template.kte", "@param placeholder:String\n<input id=\"login-email\" type=\"text\" placeholder=\"${placeholder}\" required>\n<input id=\"login-password\" type=\"password\" placeholder=\"Savecode\" required>");

        templateEngine.render("template.kte", "dummy", output);

        assertThat(output.toString()).isEqualTo("<input id=\"login-email\" type=\"text\" placeholder=\"dummy\" required>\n" +
                "<input id=\"login-password\" type=\"password\" placeholder=\"Savecode\" required>");
    }

    @Test
    void booleanAttributes_condition() {
        codeResolver.givenCode("template.kte", "@param disabled:Boolean\n<button class=\"submit cta\" @if(disabled)disabled=\"disabled\"@endif data-item=\"${id}\">Do it</button>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", true, output));

        assertThat(throwable).hasMessageContaining("Failed to compile template.kte, error at line 2: Illegal HTML attribute name @if(disabled)disabled! @if expressions in HTML attribute names are not allowed.");
    }

    @Test
    void booleanAttributes_withoutCondition() {
        codeResolver.givenCode("template.kte", "@param disabled:Boolean\n<button class=\"submit cta\" disabled=\"${disabled}\" data-item=\"id\">Do it</button>");

        templateEngine.render("template.kte", true, output);

        assertThat(output.toString()).isEqualTo("<button class=\"submit cta\" disabled data-item=\"id\">Do it</button>");
    }

    @Test
    void booleanAttributes_expression1() {
        codeResolver.givenCode("template.kte", "@param disabled:String\n<button disabled=\"${\"true\".equals(disabled)}\">Click</button>");

        templateEngine.render("template.kte", "true", output);

        assertThat(output.toString()).isEqualTo("<button disabled>Click</button>");
    }

    @Test
    void booleanAttributes_expression2() {
        codeResolver.givenCode("template.kte", "@param disabled:String?\n<button disabled=\"${\"true\".equals(disabled)}\">Click</button>");

        templateEngine.render("template.kte", (Object)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void booleanAttributes_followedByOutput() {
        codeResolver.givenCode("template.kte", "@param label:String\n<input required><label>${label}</label>");

        templateEngine.render("template.kte", "Label", output);

        assertThat(output.toString()).isEqualTo("<input required><label>Label</label>");
    }

    @Test
    void attributes_String() {
        codeResolver.givenCode("template.kte", "@param value:String\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"Hello\">Click</button>");
    }

    @Test
    void attributes_String_null() {
        codeResolver.givenCode("template.kte", "@param value:String?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", (String)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_String_empty() {
        codeResolver.givenCode("template.kte", "@param value:String?\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.kte", "", output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void cssClasses() {
        codeResolver.givenCode("template.kte", "@import gg.jte.html.support.HtmlSupport.*\n@param visible:Boolean\n<button class=\"mb-3 ${addClass(!visible, \"hide\").addClass(visible, \"show\")}\">Click</button>");

        templateEngine.render("template.kte", false, output);

        assertThat(output.toString()).isEqualTo("<button class=\"mb-3 hide\">Click</button>");
    }

    @Test
    void cssClasses_empty() {
        codeResolver.givenCode("template.kte", "@import gg.jte.html.support.HtmlSupport.*\n@param visible:Boolean\n<button class=\"${addClass(!visible, \"hide\")}\">Click</button>");

        templateEngine.render("template.kte", true, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }


    @Test
    void doctype() {
        codeResolver.givenCode("template.kte", "@param x:String?\n<!DOCTYPE html>");
        templateEngine.render("template.kte", (Object)null, output);
        assertThat(output.toString()).isEqualTo("<!DOCTYPE html>");
    }

    @Test
    void htmlComment() {
        codeResolver.givenCode("template.kte", "@param url:String?\n<!-- html comment --><a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.kte", "https://jte.gg", output);

        assertThat(output.toString()).isEqualTo("<a href=\"https://jte.gg\">Click me!</a>");
    }

    @Test
    void htmlComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.kte", "@param url:String\n<!-- html comment --><a href=\"${url}\">Click me!</a>");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.kte", "https://jte.gg", output);

        assertThat(output.toString()).isEqualTo("<!-- html comment --><a href=\"https://jte.gg\">Click me!</a>");
    }

    @Test
    void htmlComment_withCode() {
        codeResolver.givenCode("template.kte", "@param name:String\n\n<!--Comment here with ${name}-->\n<span>Test</span>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n\n<span>Test</span>");
    }

    @Test
    void htmlComment_withCode_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.kte", "@param name:String\n\n<!--Comment here with ${name}-->\n<span>Test</span>");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n<!--Comment here with Hello-->\n<span>Test</span>");
    }

    @Test
    void htmlComment_andNothingElse() {
        codeResolver.givenCode("template.kte", "<!--Comment here-->");

        templateEngine.render("template.kte", (Object)null, output);

        assertThat(output.toString()).isEqualTo("");
    }

    @Test
    void htmlComment_beforeParams() {
        codeResolver.givenCode("template.kte", "<!--Comment here-->\n@param name:String\n<span>${name}</span>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<span>Hello</span>");
    }

    @Test
    void htmlComment_ignoredIfInAttribute() {
        codeResolver.givenCode("template.kte", "@param name:String\n\n<span name=\"<!--this is not a comment ${name}-->\">Test</span>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n<span name=\"<!--this is not a comment Hello-->\">Test</span>");
    }

    @Test
    void htmlComment_ignoredIfInJavaScript() {
        codeResolver.givenCode("template.kte", "@param name:String\n<script>var name=\"<!--this is not a comment ${name}-->\"</script>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var name=\"<!--this is not a comment Hello-->\"</script>");
    }

    @Test
    void htmlComment_ignoredIfInCss() {
        codeResolver.givenCode("template.kte", "@param name:String\n<style type=\"text/css\"><!--this is not a comment ${name}--></style>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\"><!--this is not a comment Hello--></style>");
    }

    @Test
    void htmlComment_ignoredIfInJavaPart() {
        codeResolver.givenCode("template.kte", "@param name:String\n<span>${\"<!--this is not a comment \" + name + \"-->\"}</span>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<span>&lt;!--this is not a comment Hello--&gt;</span>");
    }

    @Test
    void htmlComment_ignoredIfInJavaPart2() {
        codeResolver.givenCode("template.kte", "@param name:String\n<span>!{var x = \"<!--this is not a comment \" + name + \"-->\";}${x}</span>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<span>&lt;!--this is not a comment Hello--&gt;</span>");
    }

    @Test
    void jsComment() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>// hello\nvar x = 'hello';</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>\nvar x = 'hello';</script>Hello");
    }

    @Test
    void jsComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>// hello\nvar x = 'hello';</script>${hello}");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>// hello\nvar x = 'hello';</script>Hello");
    }

    @Test
    void jsComment_onlyComment() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>// hello</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script></script>Hello");
    }

    @Test
    void jsComment_string_singleQuote() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>var x = '// hello, hello';</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = '// hello, hello';</script>Hello");
    }

    @Test
    void jsComment_string_doubleQuote() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>var x = \"// hello, hello\";</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = \"// hello, hello\";</script>Hello");
    }

    @Test
    void jsBlockComment() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>/* hello*/var x = 'hello';</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = 'hello';</script>Hello");
    }

    @Test
    void jsBlockComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>/* hello*/var x = 'hello';</script>${hello}");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>/* hello*/var x = 'hello';</script>Hello");
    }

    @Test
    void jsBlockComment_onlyComment() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>/* hello*/</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script></script>Hello");
    }

    @Test
    void jsBlockComment_string_singleQuote() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>var x = '/* hello, hello';</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = '/* hello, hello';</script>Hello");
    }

    @Test
    void jsBlockComment_string_doubleQuote() {
        codeResolver.givenCode("template.kte", "@param hello:String\n<script>var x = \"/* hello, hello\";</script>${hello}");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = \"/* hello, hello\";</script>Hello");
    }

    @Test
    void cssComment() {
        codeResolver.givenCode("template.kte", "<style type=\"text/css\">/*This is it!*/html { height: 100%;}</style>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">html { height: 100%;}</style>");
    }

    @Test
    void cssComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.kte", "<style type=\"text/css\">/*This is it!*/html { height: 100%;}</style>");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">/*This is it!*/html { height: 100%;}</style>");
    }

    @Test
    void cssComment_string_singleQuote() {
        codeResolver.givenCode("template.kte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: '/*cough*/';/*funny*/}</style>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: '/*cough*/';}</style>");
    }

    @Test
    void cssComment_string_singleQuote_escape() {
        codeResolver.givenCode("template.kte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: 'Let\\'s /*cough*/';/*funny*/}</style>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: 'Let\\'s /*cough*/';}</style>");
    }

    @Test
    void cssComment_string_doubleQuote() {
        codeResolver.givenCode("template.kte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: \"/*cough*/\";/*funny*/}</style>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: \"/*cough*/\";}</style>");
    }

    @Test
    void cssComment_string_doubleQuote_escape() {
        codeResolver.givenCode("template.kte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: \"Let\\'s /*cough*/\";/*funny*/}</style>");

        templateEngine.render("template.kte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: \"Let\\'s /*cough*/\";}</style>");
    }

    @Test
    void script1() {
        codeResolver.givenCode("template.kte", "@param userName:String\n<script>var x = 'Hello, ${userName}';</script>");

        templateEngine.render("template.kte", "'; alert('Visit my site!'); var y ='", output);

        assertThat(output.toString()).isEqualTo("<script>var x = 'Hello, \\'; alert(\\'Visit my site!\\'); var y =\\'';</script>");
    }

    @Test
    void script2() {
        codeResolver.givenCode("template.kte", "@param amount:Int\n<script>if (amount<${amount}) {alert('Amount is ' + ${amount})};</script>");

        templateEngine.render("template.kte", 5, output);

        assertThat(output.toString()).isEqualTo("<script>if (amount<5) {alert('Amount is ' + 5)};</script>");
    }

    @Test
    void script3() {
        codeResolver.givenCode("template.kte", "@param amount:Int\n<script>writeToDom('<p>test ${amount}</p>');</script>");

        templateEngine.render("template.kte", 5, output);

        assertThat(output.toString()).isEqualTo("<script>writeToDom('<p>test 5</p>');</script>");
    }

    @Test
    void script4() {
        codeResolver.givenCode("template.kte", "<div class=\"container\">\n" +
                "    <script>\n" +
                "        $(document).ready(function() {\n" +
                "            var orderId = getUrlParameter('orderId');\n" +
                "            $.get('shop/order?orderId=' + orderId, {}, function (data) {\n" +
                "                var address = data.deliveryAddress.firstName + ' ' + data.deliveryAddress.lastName + '<br/>';\n" +
                "                address += data.deliveryAddress.street + '<br/>';\n" +
                "                if (data.deliveryAddress.company !== null && data.deliveryAddress.company.length > 0) {\n" +
                "                    address += data.deliveryAddress.company + '<br/>';\n" +
                "                }\n" +
                "                address += data.deliveryAddress.postCode + '<br/>';\n" +
                "                address += data.deliveryAddress.city + '<br/>';\n" +
                "                address += data.deliveryAddress.country + '<br/>';\n" +
                "                $('#shipping-address').html(address);\n" +
                "                $('.physical-item').html(data.physicalItemName);\n" +
                "            });\n" +
                "        });\n" +
                "    </script>\n" +
                "</div>");

        templateEngine.render("template.kte", (Object)null, output);

        assertThat(output.toString()).isEqualTo("<div class=\"container\">\n" +
                "    <script>\n" +
                "        $(document).ready(function() {\n" +
                "            var orderId = getUrlParameter('orderId');\n" +
                "            $.get('shop/order?orderId=' + orderId, {}, function (data) {\n" +
                "                var address = data.deliveryAddress.firstName + ' ' + data.deliveryAddress.lastName + '<br/>';\n" +
                "                address += data.deliveryAddress.street + '<br/>';\n" +
                "                if (data.deliveryAddress.company !== null && data.deliveryAddress.company.length > 0) {\n" +
                "                    address += data.deliveryAddress.company + '<br/>';\n" +
                "                }\n" +
                "                address += data.deliveryAddress.postCode + '<br/>';\n" +
                "                address += data.deliveryAddress.city + '<br/>';\n" +
                "                address += data.deliveryAddress.country + '<br/>';\n" +
                "                $('#shipping-address').html(address);\n" +
                "                $('.physical-item').html(data.physicalItemName);\n" +
                "            });\n" +
                "        });\n" +
                "    </script>\n" +
                "</div>");
    }

    @Test
    void onMethods() {
        codeResolver.givenCode("template.kte", "@param userName:String\n\n<span onclick=\"showName('${userName}')\">Click me</span>");

        templateEngine.render("template.kte", "'); alert('xss", output);

        assertThat(output.toString()).isEqualTo("\n<span onclick=\"showName('\\x27); alert(\\x27xss')\">Click me</span>");
    }

    @Test
    void onMethods_policy() {
        OwaspHtmlPolicy policy = new OwaspHtmlPolicy();
        policy.addPolicy(new PreventInlineEventHandlers());
        templateEngine.setHtmlPolicy(policy);
        codeResolver.givenCode("template.kte", "@param name:String\n\n<span data-title=\"${name}\" onclick=\"showName('${name}')\">Click me</span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "'); alert('xss", output));

        assertThat(throwable).hasMessage("Failed to compile template.kte, error at line 3: Inline event handlers are not allowed: onclick");
    }

    @Test
    void css() {
        codeResolver.givenCode("template.kte", "<style type=\"text/css\">\n" +
                "/*<![CDATA[*/\n" +
                "body {\n" +
                "\tcolor: #333333;\n" +
                "\tline-height: 150%;\n" +
                "}\n" +
                "\n" +
                "thead {\n" +
                "\tfont-weight: bold;\n" +
                "\tbackground-color: #CCCCCC;\n" +
                "}\n" +
                "/*]]>*/\n" +
                "</style>");

        templateEngine.render("template.kte", (Object)null, output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">\n" +
                "\n" +
                "body {\n" +
                "\tcolor: #333333;\n" +
                "\tline-height: 150%;\n" +
                "}\n" +
                "\n" +
                "thead {\n" +
                "\tfont-weight: bold;\n" +
                "\tbackground-color: #CCCCCC;\n" +
                "}\n" +
                "\n" +
                "</style>");
    }

    @Test
    void forbidUnquotedAttributeValues() {
        codeResolver.givenCode("template.kte", "@param id:String\n\n<span id=${id}></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "test", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.kte, error at line 3: Unquoted HTML attribute values are not allowed: id");
    }

    @Test
    void forbidUnqotedAttributeValues_attributeContentIsIgnored() {
        codeResolver.givenCode("template.kte", "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");

        templateEngine.render("template.kte", (Object)null, output);

        assertThat(output.toString()).isEqualTo("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
    }

    @Test
    void forbidUnqotedAttributeValues_emptyAttribute() {
        codeResolver.givenCode("template.kte", "<div data-test-important-content>");

        templateEngine.render("template.kte", (Object)null, output);

        assertThat(output.toString()).isEqualTo("<div data-test-important-content>");
    }

    @Test
    void singleQuotedAttributeValues() {
        codeResolver.givenCode("template.kte", "@param id:String\n\n<span id='${id}'></span>");

        templateEngine.render("template.kte", "<script>console.log(\"Hello\")</script>", output);

        assertThat(output.toString()).isEqualTo("\n<span id='&lt;script>console.log(&#34;Hello&#34;)&lt;/script>'></span>");
    }

    @Test
    void forbidSingleQuotedAttributeValues() {
        OwaspHtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
        htmlPolicy.addPolicy(new PreventSingleQuotedAttributes());
        templateEngine.setHtmlPolicy(htmlPolicy);
        codeResolver.givenCode("template.kte", "@param id:String\n\n<span id='${id}'></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "test", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.kte, error at line 3: HTML attribute values must be double quoted: id");
    }

    @Test
    void forbidSingleQuotedAttributeValues_boolean() {
        OwaspHtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
        htmlPolicy.addPolicy(new PreventSingleQuotedAttributes());
        templateEngine.setHtmlPolicy(htmlPolicy);
        codeResolver.givenCode("template.kte", "@param id:String\n<input id=\"${id}\" required>");

        templateEngine.render("template.kte", "test", output);

        assertThat(output.toString()).isEqualTo("<input id=\"test\" required>");
    }

    @Test
    void forbidSingleQuotedAttributeValues_empty() {
        OwaspHtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
        htmlPolicy.addPolicy(new PreventSingleQuotedAttributes());
        templateEngine.setHtmlPolicy(htmlPolicy);
        codeResolver.givenCode("template.kte", "@param id:String\n<input data-webtest>");

        templateEngine.render("template.kte", "test", output);

        assertThat(output.toString()).isEqualTo("<input data-webtest>");
    }

    @Test
    void enumInTagBody() {
        codeResolver.givenCode("template.kte", "@param type:gg.jte.kotlin.TemplateEngineTest.ModelType\n<div>${type}</div>");

        templateEngine.render("template.kte", TemplateEngineTest.ModelType.Two, output);

        assertThat(output.toString()).isEqualTo("<div>Two</div>");
    }

    @Test
    void enumInTagAttribute() {
        codeResolver.givenCode("template.kte", "@param type:gg.jte.kotlin.TemplateEngineTest.ModelType\n<div data-type=\"${type}\"></div>");

        templateEngine.render("template.kte", TemplateEngineTest.ModelType.Two, output);

        assertThat(output.toString()).isEqualTo("<div data-type=\"Two\"></div>");
    }

    @Test
    void nullInTagBody() {
        codeResolver.givenCode("template.kte", "@param type:String?\n<div>${type}</div>");

        templateEngine.render("template.kte", Collections.singletonMap("type", null), output);

        assertThat(output.toString()).isEqualTo("<div></div>");
    }

    @Test
    void nullInTagAttribute() {
        codeResolver.givenCode("template.kte", "@param type:String?\n<div data-type=\"${type}\"></div>");

        templateEngine.render("template.kte", Collections.singletonMap("type", null), output);

        assertThat(output.toString()).isEqualTo("<div></div>");
    }

    @Test
    void uppercaseTag() {
        codeResolver.givenCode("template.kte", "@param url:String\n<A href=\"${url}\">Click me!</A>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "javascript:alert(1)", output));

        assertThat(throwable.getMessage()).isEqualTo("Failed to compile template.kte, error at line 2: HTML tags are expected to be lowercase: A");
    }

    @Test
    void uppercaseAttribute() {
        codeResolver.givenCode("template.kte", "@param url:String\n<a HREF=\"${url}\">Click me!</a>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "javascript:alert(1)", output));

        assertThat(throwable.getMessage()).isEqualTo("Failed to compile template.kte, error at line 2: HTML attributes are expected to be lowercase: HREF");
    }

    @Test
    void contentBlockInAttribute() {
        codeResolver.givenCode("template.kte", "@param content:gg.jte.Content = @`This is \"the way\"!`\n<span data-title=\"${content}\">Info</span>");

        templateEngine.render("template.kte", new HashMap<>(), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is &#34;the way&#34;!\">Info</span>");
    }

    @Test
    void contentBlockInAttribute2() {
        codeResolver.givenCode("template.kte", "@param content:gg.jte.Content = @`This is <b>the way</b>!`\n<span data-title=\"${content}\" foo=\"bar\">${content}</span>");

        templateEngine.render("template.kte", new HashMap<>(), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is &lt;b>the way&lt;/b>!\" foo=\"bar\">This is <b>the way</b>!</span>");
    }

    @Test
    void contentBlockInAttribute3() {
        codeResolver.givenCode("template.kte",
                "@param url:String\n" +
                "!{val content = @`<a href=\"${url}\" class=\"foo\">Hello</a>`}\n" +
                "${content}");

        templateEngine.render("template.kte", TemplateUtils.toMap("url", "https://jte.gg"), output);

        assertThat(output.toString()).isEqualTo("\n<a href=\"https://jte.gg\" class=\"foo\">Hello</a>");
    }

    @Test
    void contentBlockInAttribute4() {
        codeResolver.givenCode("template.kte",
                "@param url:String\n" +
                        "!{val content = @`<a href=\"${url}\" class=\"foo\">Hello</a>`;}\n" +
                        "<span data-content=\"${content}\">${content}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("url", "https://jte.gg"), output);

        assertThat(output.toString()).isEqualTo("\n" +
                "<span data-content=\"&lt;a href=&#34;https://jte.gg&#34; class=&#34;foo&#34;>Hello&lt;/a>\"><a href=\"https://jte.gg\" class=\"foo\">Hello</a></span>");
    }

    @Test
    void javascriptUrl() {
        codeResolver.givenCode("template.kte", "@param url:String\n<a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.kte", "javascript:alert(1)", output);

        assertThat(output.toString()).isEqualTo("<a href=\"\">Click me!</a>");
    }

    @Test
    void javascriptUrl_uppercase() {
        codeResolver.givenCode("template.kte", "@param url:String\n<a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.kte", "JAVASCRIPT:alert(1)", output);

        assertThat(output.toString()).isEqualTo("<a href=\"\">Click me!</a>");
    }

    @Test
    void javascriptUrl_mixedcase() {
        codeResolver.givenCode("template.kte", "@param url:String\n<a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.kte", " \n\t jAvaScRipT:alert(1)", output);

        assertThat(output.toString()).isEqualTo("<a href=\"\">Click me!</a>");
    }

    @Test
    void tagCallInScript() {
        codeResolver.givenCode("tag/snippet.kte", "var x = y;");
        codeResolver.givenCode("template.kte", "@param ignored:String\n<script>\nfunction() {\n@template.tag.snippet()\n}\n</script>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "ignored", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.kte, error at line 4: Template calls in <script> blocks are not allowed.");
    }

    @Test
    void layoutCallInScript() {
        codeResolver.givenCode("layout/snippet.kte", "var x = y;");
        codeResolver.givenCode("template.kte", "@param String ignored\n<script>\nfunction() {\n@template.layout.snippet()\n}\n</script>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.kte", "ignored", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.kte, error at line 4: Template calls in <script> blocks are not allowed.");
    }

    @Test
    void unsafe_null() {
        codeResolver.givenCode("template.kte", "@param x:String?\nHello, $unsafe{x}");

        templateEngine.render("template.kte", (String)null, output);

        assertThat(output.toString()).isEqualTo("Hello, ");
    }

    @Test
    void localization_notFound_noParams() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span>${localizer.localize(\"unknown\")}</span>");

        templateEngine.render("template.kte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_notFound_withParams() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span>${localizer.localize(\"unknown\", 1)}</span>");

        templateEngine.render("template.kte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_noParams() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span alt=\"${localizer.localize(\"no-params\")}\">${localizer.localize(\"no-params\")}</span>${localizer.localize(\"no-params\")}");

        templateEngine.render("template.kte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span alt=\"This is a key without params\">This is a key without params</span>This is a key without params");
    }

    @Test
    void localization_html() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span>${localizer.localize(\"no-params-html\")}</span>");

        templateEngine.render("template.kte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>This is a key without params but with <b>html content</b></span>");
    }

    @Test
    void localization_oneParam() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String\n" +
                "<span>${localizer.localize(\"one-param\", param)}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: &lt;script&gt;evil()&lt;/script&gt;.</span>");
    }

    @Test
    void localization_html_oneParam() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String\n" +
                "<span>${localizer.localize(\"one-param-html\", param)}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>&lt;script&gt;evil()&lt;/script&gt;</b>. Including HTML in key!</span>");
    }

    @Test
    void localization_inception() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String\n" +
                "<span>${localizer.localize(\"one-param-html\", localizer.localize(\"one-param-html\", param))}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b>&lt;script&gt;evil()&lt;/script&gt;</b>. Including HTML in key!</b>. Including HTML in key!</span>");
    }

    @Test
    void localization_quotesInAttribute() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span data-title=\"${localizer.localize(\"quotes\")}\"></span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is a key with &#34;quotes&#34;\"></span>");
    }

    @Test
    void localization_quotesInAttributeWithParams() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param p1:String\n" +
                "@param p2:String\n" +
                "@param p3:String\n" +
                "<span data-title=\"${localizer.localize(\"quotes-params\", p1, p2, p3)}\"></span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "p1", "<script>evil()</script>", "p2", "p2", "p3", "p3"), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is a key with &#34;quotes&#34; and params &lt;i>&#34;&lt;script>evil()&lt;/script>&#34;&lt;/i>, &lt;b>&#34;p2&#34;&lt;/b>, &#34;p3&#34;...\"></span>");
    }

    @Test
    void localization_manyParams_noneSet() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String\n" +
                "<span>${localizer.localize(\"many-params-html\")}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>{0}</i>, <b>{1}</b>, {2}</span>");
    }

    @Test
    void localization_manyParams_primitives() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String?\n" +
                "<span>${localizer.localize(\"many-params-html\")}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "p1", true, "p2", 1, "p3", 2), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>{0}</i>, <b>{1}</b>, {2}</span>");
    }

    @Test
    void localization_manyParams_oneSet() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String\n" +
                "<span>${localizer.localize(\"many-params-html\", param)}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>&lt;script&gt;evil()&lt;/script&gt;</i>, <b></b>, </span>");
    }

    @Test
    void localization_manyParams_allSame() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String\n" +
                "<span>${localizer.localize(\"many-params-html\", param, param, param)}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>&lt;script&gt;evil()&lt;/script&gt;</i>, <b>&lt;script&gt;evil()&lt;/script&gt;</b>, &lt;script&gt;evil()&lt;/script&gt;</span>");
    }

    @Test
    void localization_badPattern() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param param:String\n" +
                "<span>${localizer.localize(\"bad-pattern\", param)}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello {foo}</span>");
    }

    @Test
    void localization_primitives() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span>${localizer.localize(\"all-primitives\", false, 1.toByte(), 2.toShort(), 3, 4L, 5.0f, 6.0, 'c')}</span>");

        templateEngine.render("template.kte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>boolean: false, byte: 1, short: 2, int: 3, long: 4, float: 5.0, double: 6.0, char: c</span>");
    }

    @Test
    void localization_primitives_inAttribute() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span alt=\"${localizer.localize(\"all-primitives\", false, 1.toByte(), 2.toShort(), 3, 4L, 5.0f, 6.0, 'c')}\"></span>");

        templateEngine.render("template.kte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span alt=\"boolean: false, byte: 1, short: 2, int: 3, long: 4, float: 5.0, double: 6.0, char: c\"></span>");
    }

    @Test
    void localization_enum() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param contentType:gg.jte.ContentType\n" +
                "<span alt=\"${localizer.localize(\"enum\", contentType)}\">${localizer.localize(\"enum\", contentType)}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "contentType", ContentType.Html), output);

        assertThat(output.toString()).isEqualTo("<span alt=\"Content type is: Html\">Content type is: Html</span>");
    }

    @Test
    void localization_tag() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content\n" +
                    "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param name:String\n" +
                "@template.tag.card(content = @`<b>${localizer.localize(\"one-param\", name)}</b>`)");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span><b>This is a key with user content: &lt;script&gt;.</b></span>");
    }

    @Test
    void localization_tag2() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param name:String\n" +
                "@template.tag.card(content = localizer.localize(\"one-param\", name))");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: &lt;script&gt;.</span>");
    }

    @Test
    void localization_tag3() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param name:String\n" +
                "@template.tag.card(content = localizer.localize(\"one-param\", @`<b>${name}</b>`))");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>&lt;script&gt;</b>.</span>");
    }

    @Test
    void localization_tag4() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param name:String\n" +
                "@template.tag.card(content = localizer.localize(\"many-params-html\", @`<span>${name}</span>`, @`<span>${name}</span>`, @`<span>${name}</span>`))");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i><span>&lt;script&gt;</span></i>, <b><span>&lt;script&gt;</span></b>, <span>&lt;script&gt;</span></span>");
    }

    @Test
    void localization_tag5() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param name:String\n" +
                "@template.tag.card(content = localizer.localize(\"one-param\", @`<b>" +
                    "${localizer.localize(\"one-param-html\", @`<i>${name}</i>`)}" +
                "</b>`))");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</b>.</span>");
    }

    @Test
    void localization_tag6() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param name:String\n" +
                "@template.tag.card(content = localizer.localize(\"one-param\", @`<b>" +
                    "@template.tag.card(content = localizer.localize(\"one-param-html\", @`<i>${name}</i>`))" +
                "</b>`))");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b><span>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</span></b>.</span>");
    }

    @Test
    void localization_tag7() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "@param name:String\n" +
                "!{var content = localizer.localize(\"one-param\", @`<b>" +
                "${localizer.localize(\"one-param-html\", @`<i>${name}</i>`)}" +
                "</b>`);}" +
                "@template.tag.card(content = content)");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</b>.</span>");
    }

    @Test
    void localization_tag8() {
        codeResolver.givenCode("tag/card.kte", "@param content:gg.jte.Content = @`My default is ${42}`\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.kte", "@template.tag.card()");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>My default is 42</span>");
    }

    @Test
    void localization_contentParams() {
        codeResolver.givenCode("template.kte", "@param localizer:gg.jte.kotlin.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer\n" +
                "<span>${localizer.localize(\"link\", @`<a href=\"${\"foo\"}\">`, @`</a>`)}</span>");

        templateEngine.render("template.kte", TemplateUtils.toMap("localizer", localizer), output);

        assertThat(output.toString()).isEqualTo("<span>Hello? <a href=\"foo\">Click here</a></span>");
    }

    @SuppressWarnings("unused")
    public static class MyLocalizer implements LocalizationSupport {
        Map<String, Object> resources = TemplateUtils.toMap(
                "no-params", "This is a key without params",
                "no-params-html", "This is a key without params but with <b>html content</b>",
                "one-param", "This is a key with user content: {0}.",
                "one-param-html", "This is a key with user content: <b>{0}</b>. Including HTML in key!",
                "many-params-html", "Hello <i>{0}</i>, <b>{1}</b>, {2}",
                "bad-pattern", "Hello {foo}",
                "all-primitives", "boolean: {0}, byte: {1}, short: {2}, int: {3}, long: {4}, float: {5}, double: {6}, char: {7}",
                "enum", "Content type is: {0}",
                "quotes", "This is a key with \"quotes\"",
                "quotes-params", "This is a key with \"quotes\" and params <i>\"{0}\"</i>, <b>\"{1}\"</b>, \"{2}\"...",
                "link", "Hello? {0}Click here{1}"
        );

        @Override
        public String lookup(String key) {
            return (String)resources.get(key);
        }
    }
}
