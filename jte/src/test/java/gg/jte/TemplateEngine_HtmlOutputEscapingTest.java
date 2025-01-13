package gg.jte;

import gg.jte.html.HtmlTemplateOutput;
import gg.jte.html.OwaspHtmlPolicy;
import gg.jte.html.policy.*;
import gg.jte.output.StringOutput;
import gg.jte.runtime.TemplateUtils;
import gg.jte.support.LocalizationSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
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
        codeResolver.givenCode("template.jte",
                """
                        @param String url
                        @param String title
                        Look at <a href="${url}">${title}</a>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("url", "https://www.test.com?param1=1&param2=2", "title", "<script>alert('hello');</script>"), output);

        assertThat(output.toString()).isEqualTo("Look at <a href=\"https://www.test.com?param1=1&amp;param2=2\">&lt;script&gt;alert('hello');&lt;/script&gt;</a>");
    }

    @Test
    void unclosedTag() {
        codeResolver.givenCode("unclosed.jte", "<span><a></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("unclosed.jte", null, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile unclosed.jte, error at line 1: Unclosed tag <a>, expected </a>, got </span>.");
    }

    @Test
    void unclosedTag_worksWithHyperScript() {
        codeResolver.givenCode("closed.jte", """
                <body>
                <p _="on load log 'hello world from console'">Hello world</p>

                <form action="/login" method="post" _="on submit toggle @disabled on <button[type='submit']/>">
                    <input type="email" name="username">
                    <input type="password" name="password">

                    <button type="submit">Login</button>
                </form>
                </body>""");

        Throwable throwable = catchThrowable(() -> templateEngine.render("closed.jte", null, output));

        assertThat(throwable).isNull();
    }

    @Test
    void unclosedTag_worksWithHtmlInAttribute() {
        codeResolver.givenCode("closed.jte", "<form><input name=\">\" disabled=\"${true}\"></form>");

        templateEngine.render("closed.jte", null, output);

        assertThat(output.toString()).isEqualTo("<form><input name=\">\" disabled></form>"); // tag processing must not end after name=">", otherwise disabled="true" instead of just disabled would be the output.
    }

    @Test
    void unclosedTag_worksWithClosingHtmlInAttribute() {
        codeResolver.givenCode("closed.jte", "<form><input text=\"</form>\"></form>");

        templateEngine.render("closed.jte", null, output);

        assertThat(output.toString()).isEqualTo("<form><input text=\"</form>\"></form>");
    }

    @Test
    void unclosedTag_form() {
        codeResolver.givenCode("unclosed.jte", "<form>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("unclosed.jte", null, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile unclosed.jte, error at line 1: Unclosed tag <form>. Maybe you want to use gg.jte.Content? More information: https://github.com/casid/jte/releases/tag/3.0.0#user-content-html-tags-must-be-properly-closed");
    }

    @Test
    void unclosedTag_input_doesNotNeedToBeClosed() {
        codeResolver.givenCode("unclosed.jte", "<input>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("unclosed.jte", null, output));

        assertThat(throwable).isNull();
    }

    @Test
    void unclosedTag_declaration() {
        codeResolver.givenCode("closed.jte", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        templateEngine.render("closed.jte", null, output);

        assertThat(output.toString()).isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    @Test
    void codeInTag() {
        codeResolver.givenCode("template.jte", "@param String tag\n\n<span><${tag}/></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "br", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Illegal HTML tag name ${tag}! Expressions in HTML tag names are not allowed.");
    }

    @Test
    void codeInTag_unsafe() {
        codeResolver.givenCode("template.jte", "@param String tag\n\n<span><$unsafe{tag}/></span>");

        templateEngine.render("template.jte", "br", output);

        assertThat(output.toString()).isEqualTo("\n<span><br/></span>");
    }

    @Test
    void codeInAttribute() {
        codeResolver.givenCode("template.jte", "@param String attribute\n\n<span ${attribute}=\"value\"></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "class", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Illegal HTML attribute name ${attribute}! Expressions in HTML attribute names are not allowed.");
    }

    @Test
    void codeInAttribute_unsafe() {
        codeResolver.givenCode("template.jte", "@param String attribute\n\n<span $unsafe{attribute}=\"value\"></span>");

        templateEngine.render("template.jte", "class", output);

        assertThat(output.toString()).isEqualTo("\n<span class=\"value\"></span>");
    }

    @Test
    void commentIsNotConfusedWithHtmlTag() {
        codeResolver.givenCode("template.jte", "@param String name\n<%-- Comment --%>\n<p>Hello ${name}</p>");

        templateEngine.render("template.jte", "John", output);

        assertThat(output.toString()).isEqualTo("\n<p>Hello John</p>");
    }

    @Test
    void attributes() {
        codeResolver.givenCode("template.jte", "@param String userName\n<div data-title=\"Hello ${userName}\"></div>");

        templateEngine.render("template.jte", "\"><script>alert('xss')</script>", output);

        assertThat(output.toString()).isEqualTo("<div data-title=\"Hello &#34;>&lt;script>alert(&#39;xss&#39;)&lt;/script>\"></div>");
    }

    @Test
    void attributes_null() {
        codeResolver.givenCode("template.jte", "@param String title\n<button data-title=\"${title}\">Click</button>");

        templateEngine.render("template.jte", (String)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_string() {
        codeResolver.givenCode("template.jte", "@param String title\n<button data-title=\"${title}\">Click</button>");

        templateEngine.render("template.jte", "The title", output);

        assertThat(output.toString()).isEqualTo("<button data-title=\"The title\">Click</button>");
    }

    @Test
    void attributes_byte() {
        codeResolver.givenCode("template.jte", "@param byte value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", (byte)42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_short() {
        codeResolver.givenCode("template.jte", "@param short value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", (short)42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_int() {
        codeResolver.givenCode("template.jte", "@param int value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", 42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_long() {
        codeResolver.givenCode("template.jte", "@param long value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", 42L, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_float() {
        codeResolver.givenCode("template.jte", "@param float value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", 42.5f, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42.5\">Click</button>");
    }

    @Test
    void attributes_double() {
        codeResolver.givenCode("template.jte", "@param double value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", 42.5, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42.5\">Click</button>");
    }

    @Test
    void attributes_Integer() {
        codeResolver.givenCode("template.jte", "@param Integer value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", 42, output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"42\">Click</button>");
    }

    @Test
    void attributes_Integer_null() {
        codeResolver.givenCode("template.jte", "@param Integer value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", (Integer)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_Boolean() {
        codeResolver.givenCode("template.jte", "@param Boolean value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", Boolean.FALSE, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_Boolean2() {
        codeResolver.givenCode("template.jte", "@param Boolean value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_Boolean_null() {
        codeResolver.givenCode("template.jte", "@param Boolean value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", (Boolean)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_content() {
        codeResolver.givenCode("template.jte", "@param gg.jte.Content content\n<button data-title = \"${content}\">Click</button>");

        templateEngine.render("template.jte", (Content) output -> output.writeContent("Hello \"My friend\"!"), output);

        assertThat(output.toString()).isEqualTo("<button data-title=\"Hello &#34;My friend&#34;!\">Click</button>");
    }

    @Test
    void attributes_content_null() {
        codeResolver.givenCode("template.jte", "@param gg.jte.Content content\n<button data-title = \"${content}\">Click</button>");

        templateEngine.render("template.jte", (Content)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_multipleOutputs() {
        codeResolver.givenCode("template.jte", "@param String title\n<button data-title-x2=\"${title} + ${title}\">Click</button>");

        templateEngine.render("template.jte", "The title", output);

        assertThat(output.toString()).isEqualTo("<button data-title-x2=\"The title + The title\">Click</button>");
    }

    @Test
    void attributes_multipleOutputs_null() {
        codeResolver.givenCode("template.jte", "@param String title\n<button data-title-x2=\"${title} + ${title}\">Click</button>");

        templateEngine.render("template.jte", (String)null, output);

        assertThat(output.toString()).isEqualTo("<button data-title-x2=\" + \">Click</button>");
    }

    @Test
    void attributes_boolean_true() {
        codeResolver.givenCode("template.jte", "@param boolean visible\n<button data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.jte", true, output);

        assertThat(output.toString()).isEqualTo("<button data-visible=\"true\">Click</button>");
    }

    @Test
    void attributes_boolean_false() {
        codeResolver.givenCode("template.jte", "@param boolean visible\n<button data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_boolean_false_attributeBefore() {
        codeResolver.givenCode("template.jte", "@param boolean visible\n<button data-test data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button data-test>Click</button>");
    }

    @Test
    void attributes_boolean_true_attributeBefore() {
        codeResolver.givenCode("template.jte", "@param boolean visible\n<button data-test data-visible=\"${visible}\">Click</button>");

        templateEngine.render("template.jte", true, output);

        assertThat(output.toString()).isEqualTo("<button data-test data-visible=\"true\">Click</button>");
    }

    @Test
    void attributes_booleanExpression1() {
        codeResolver.givenCode("template.jte", "@param boolean visible\n<button data-visible=\"${visible == true}\" data-invisible=\"${!visible}\">Click</button>");

        templateEngine.render("template.jte", true, output);

        assertThat(output.toString()).isEqualTo("<button data-visible=\"true\">Click</button>");
    }

    @Test
    void attributes_booleanExpression2() {
        codeResolver.givenCode("template.jte", "@param boolean visible\n<button data-visible=\"${visible == true}\" data-invisible=\"${!visible}\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button data-invisible=\"true\">Click</button>");
    }

    @Test
    void booleanAttributes_true() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button disabled=\"${disabled}\">Click</button>");

        templateEngine.render("template.jte", true, output);

        assertThat(output.toString()).isEqualTo("<button disabled>Click</button>");
    }

    @Test
    void booleanAttributes_false() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button disabled=\"${disabled}\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void booleanAttribute_noJavaExpression() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button disabled=\"disabled\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button disabled=\"disabled\">Click</button>");
    }

    @Test
    void booleanAttributes_noParams() {
        codeResolver.givenCode("template.jte", "@param String cssClass\n<button disabled class=\"${cssClass}\">Click</button>");

        templateEngine.render("template.jte", "dummy", output);

        assertThat(output.toString()).isEqualTo("<button disabled class=\"dummy\">Click</button>");
    }

    @Test
    void booleanAttributes_noParams2() {
        codeResolver.givenCode("template.jte", "@param String placeholder\n<input id=\"login-email\" type=\"text\" placeholder=\"${placeholder}\" required>\n<input id=\"login-password\" type=\"password\" placeholder=\"Savecode\" required>");

        templateEngine.render("template.jte", "dummy", output);

        assertThat(output.toString()).isEqualTo("""
                <input id="login-email" type="text" placeholder="dummy" required>
                <input id="login-password" type="password" placeholder="Savecode" required>\
                """);
    }

    @Test
    void booleanAttributes_withoutCondition() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button class=\"submit cta\" disabled=\"${disabled}\" data-item=\"id\">Do it</button>");

        templateEngine.render("template.jte", true, output);

        assertThat(output.toString()).isEqualTo("<button class=\"submit cta\" disabled data-item=\"id\">Do it</button>");
    }

    @Test
    void booleanAttributes_expression1() {
        codeResolver.givenCode("template.jte", "@param String disabled\n<button disabled=\"${\"true\".equals(disabled)}\">Click</button>");

        templateEngine.render("template.jte", "true", output);

        assertThat(output.toString()).isEqualTo("<button disabled>Click</button>");
    }

    @Test
    void booleanAttributes_expression2() {
        codeResolver.givenCode("template.jte", "@param String disabled\n<button disabled=\"${\"true\".equals(disabled)}\">Click</button>");

        templateEngine.render("template.jte", (Object)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void booleanAttributes_followedByOutput() {
        codeResolver.givenCode("template.jte", "@param String label\n<input required><label>${label}</label>");

        templateEngine.render("template.jte", "Label", output);

        assertThat(output.toString()).isEqualTo("<input required><label>Label</label>");
    }

    @Test
    void attributes_String() {
        codeResolver.givenCode("template.jte", "@param String value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"Hello\">Click</button>");
    }

    @Test
    void attributes_String_null() {
        codeResolver.givenCode("template.jte", "@param String value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", (String)null, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }

    @Test
    void attributes_String_empty() {
        codeResolver.givenCode("template.jte", "@param String value\n<button data-value=\"${value}\">Click</button>");

        templateEngine.render("template.jte", "", output);

        assertThat(output.toString()).isEqualTo("<button data-value=\"\">Click</button>");
    }

    /**
     * This is essentially the same test as gg.jte.TemplateEngine_HtmlOutputEscapingTest#attributes_String_empty()
     * But it especially shows that there is a semantic difference between an empty String and null.
     */
    @Test
    void attributes_String_empty_option() {
        codeResolver.givenCode("template.jte", "@param String value\n<option value=\"${value}\">Empty</option>");

        templateEngine.render("template.jte", "", output);

        assertThat(output.toString()).isEqualTo("<option value=\"\">Empty</option>");
    }

    @Test
    void cssClasses() {
        codeResolver.givenCode("template.jte", "@import static gg.jte.html.support.HtmlSupport.*\n@param boolean visible\n<button class=\"mb-3 ${addClass(!visible, \"hide\").addClass(visible, \"show\")}\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button class=\"mb-3 hide\">Click</button>");
    }

    @Test
    void cssClasses_empty() {
        codeResolver.givenCode("template.jte", "@import static gg.jte.html.support.HtmlSupport.*\n@param boolean visible\n<button class=\"${addClass(!visible, \"hide\")}\">Click</button>");

        templateEngine.render("template.jte", true, output);

        assertThat(output.toString()).isEqualTo("<button>Click</button>");
    }


    @Test
    void doctype() {
        codeResolver.givenCode("template.jte", "@param String x\n<!DOCTYPE html>");
        templateEngine.render("template.jte", (Object)null, output);
        assertThat(output.toString()).isEqualTo("<!DOCTYPE html>");
    }

    @Test
    void commentBetweenAttributes() {
        templateEngine.setTrimControlStructures(true);
        codeResolver.givenCode("template.jte", """
                @param String url
                <form hx-post="${url}" <%-- (1) --%>
                      hx-target="${url}" <%-- (2) --%>
                      hx-swap="outerHTML"> <%-- (2) --%>
                    <select>
                        @for(var i = 0; i < 3; i++) <%-- (3) --%>
                            <option value="${i}">${i}</option>
                        @endfor
                    </select>
                    <button type="submit">Add User to group</button>
                </form>
                """);

        templateEngine.render("template.jte", "https://jte.gg", output);

        assertThat(output.toString()).isEqualTo("""
                <form hx-post="https://jte.gg"\s
                      hx-target="https://jte.gg"\s
                      hx-swap="outerHTML">\s
                    <select>
                        <option value="0">0</option>
                        <option value="1">1</option>
                        <option value="2">2</option>
                    </select>
                    <button type="submit">Add User to group</button>
                </form>
                """);
    }

    @Test
    void htmlComment() {
        codeResolver.givenCode("template.jte", "@param String url\n<!-- html comment --><a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.jte", "https://jte.gg", output);

        assertThat(output.toString()).isEqualTo("<a href=\"https://jte.gg\">Click me!</a>");
    }

    @Test
    void htmlComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.jte", "@param String url\n<!-- html comment --><a href=\"${url}\">Click me!</a>");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.jte", "https://jte.gg", output);

        assertThat(output.toString()).isEqualTo("<!-- html comment --><a href=\"https://jte.gg\">Click me!</a>");
    }

    @Test
    void htmlComment_withCode() {
        codeResolver.givenCode("template.jte", "@param String name\n\n<!--Comment here with ${name}-->\n<span>Test</span>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n\n<span>Test</span>");
    }

    @Test
    void htmlComment_withCode_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.jte", "@param String name\n\n<!--Comment here with ${name}-->\n<span>Test</span>");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n<!--Comment here with Hello-->\n<span>Test</span>");
    }

    @Test
    void htmlComment_andNothingElse() {
        codeResolver.givenCode("template.jte", "<!--Comment here-->");

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualTo("");
    }

    @Test
    void htmlComment_beforeParams() {
        codeResolver.givenCode("template.jte", "<!--Comment here-->\n@param String name\n<span>${name}</span>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<span>Hello</span>");
    }

    @Test
    void htmlComment_ignoredIfInAttribute() {
        codeResolver.givenCode("template.jte", "@param String name\n\n<span name=\"<!--this is not a comment ${name}-->\">Test</span>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n<span name=\"<!--this is not a comment Hello-->\">Test</span>");
    }

    @Test
    void htmlComment_ignoredIfInJavaScript() {
        codeResolver.givenCode("template.jte", "@param String name\n<script>var name=\"<!--this is not a comment ${name}-->\"</script>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var name=\"<!--this is not a comment Hello-->\"</script>");
    }

    @Test
    void htmlComment_ignoredIfInCss() {
        codeResolver.givenCode("template.jte", "@param String name\n<style type=\"text/css\"><!--this is not a comment ${name}--></style>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\"><!--this is not a comment Hello--></style>");
    }

    @Test
    void htmlComment_ignoredIfInJavaPart() {
        codeResolver.givenCode("template.jte", "@param String name\n<span>${\"<!--this is not a comment \" + name + \"-->\"}</span>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<span>&lt;!--this is not a comment Hello--&gt;</span>");
    }

    @Test
    void htmlComment_ignoredIfInJavaPart2() {
        codeResolver.givenCode("template.jte", "@param String name\n<span>!{String x = \"<!--this is not a comment \" + name + \"-->\";}${x}</span>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<span>&lt;!--this is not a comment Hello--&gt;</span>");
    }

    @Test
    void htmlComment_raw() {
        codeResolver.givenCode("template.jte", "@param String url\n@raw<!-- html comment -->@endraw<a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.jte", "https://jte.gg", output);

        assertThat(output.toString()).isEqualTo("<!-- html comment --><a href=\"https://jte.gg\">Click me!</a>");
    }

    @Test
    void htmlComment_inContentBlock() {
        codeResolver.givenCode("template.jte", """
        !{var content = @`
            <!-- test comment (breaks) -->
            <div class="test"></div>
        `;}
        ${content}
        """);

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualToIgnoringWhitespace("<div class=\"test\"></div>");
    }

    @Test
    void jsComment() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>// hello\nvar x = 'hello';</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>\nvar x = 'hello';</script>Hello");
    }

    @Test
    void jsComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>// hello\nvar x = 'hello';</script>${hello}");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>// hello\nvar x = 'hello';</script>Hello");
    }

    @Test
    void jsComment_onlyComment() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>// hello</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script></script>Hello");
    }

    @Test
    void jsComment_string_singleQuote() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>var x = '// hello, hello';</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = '// hello, hello';</script>Hello");
    }

    @Test
    void jsComment_string_doubleQuote() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>var x = \"// hello, hello\";</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = \"// hello, hello\";</script>Hello");
    }

    @Test
    void jsComment_lineBreaks() {
        codeResolver.givenCode("template.jte", """
                @param String hello
                <script>
                   x.value = "false" // foo
                   x.test() // bar
                </script>
                ${hello}""");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("""
                <script>
                   x.value = "false"\s
                   x.test()\s
                </script>
                Hello""");
    }

    @Test
    void jsBlockComment() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>/* hello*/var x = 'hello';</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = 'hello';</script>Hello");
    }

    @Test
    void jsBlockComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>/* hello*/var x = 'hello';</script>${hello}");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>/* hello*/var x = 'hello';</script>Hello");
    }

    @Test
    void jsBlockComment_onlyComment() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>/* hello*/</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script></script>Hello");
    }

    @Test
    void jsBlockComment_string_singleQuote() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>var x = '/* hello, hello';</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = '/* hello, hello';</script>Hello");
    }

    @Test
    void jsBlockComment_string_doubleQuote() {
        codeResolver.givenCode("template.jte", "@param String hello\n<script>var x = \"/* hello, hello\";</script>${hello}");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<script>var x = \"/* hello, hello\";</script>Hello");
    }

    @Test
    void cssComment() {
        codeResolver.givenCode("template.jte", "<style type=\"text/css\">/*This is it!*/html { height: 100%;}</style>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">html { height: 100%;}</style>");
    }

    @Test
    void cssComment_withHtmlCommentsPreserved() {
        codeResolver.givenCode("template.jte", "<style type=\"text/css\">/*This is it!*/html { height: 100%;}</style>");
        templateEngine.setHtmlCommentsPreserved(true);

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">/*This is it!*/html { height: 100%;}</style>");
    }

    @Test
    void cssComment_string_singleQuote() {
        codeResolver.givenCode("template.jte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: '/*cough*/';/*funny*/}</style>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: '/*cough*/';}</style>");
    }

    @Test
    void cssComment_string_singleQuote_escape() {
        codeResolver.givenCode("template.jte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: 'Let\\'s /*cough*/';/*funny*/}</style>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: 'Let\\'s /*cough*/';}</style>");
    }

    @Test
    void cssComment_string_doubleQuote() {
        codeResolver.givenCode("template.jte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: \"/*cough*/\";/*funny*/}</style>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: \"/*cough*/\";}</style>");
    }

    @Test
    void cssComment_string_doubleQuote_escape() {
        codeResolver.givenCode("template.jte", "<style type=\"text/css\">/*This is it!*/.smog::after { content: \"Let\\'s /*cough*/\";/*funny*/}</style>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">.smog::after { content: \"Let\\'s /*cough*/\";}</style>");
    }

    @Test
    void script1() {
        codeResolver.givenCode("template.jte", "@param String userName\n<script>var x = 'Hello, ${userName}';</script>");

        templateEngine.render("template.jte", "'; alert('Visit my site!'); var y ='", output);

        assertThat(output.toString()).isEqualTo("<script>var x = 'Hello, \\'; alert(\\'Visit my site!\\'); var y =\\'';</script>");
    }

    @Test
    void script2() {
        codeResolver.givenCode("template.jte", "@param int amount\n<script>if (amount<${amount}) {alert('Amount is ' + ${amount})};</script>");

        templateEngine.render("template.jte", 5, output);

        assertThat(output.toString()).isEqualTo("<script>if (amount<5) {alert('Amount is ' + 5)};</script>");
    }

    @Test
    void script3() {
        codeResolver.givenCode("template.jte", "@param int amount\n<script>writeToDom('<p>test ${amount}</p>');</script>");

        templateEngine.render("template.jte", 5, output);

        assertThat(output.toString()).isEqualTo("<script>writeToDom('<p>test 5</p>');</script>");
    }

    @Test
    void script4() {
        codeResolver.givenCode("template.jte", """
                <div class="container">
                    <script>
                        $(document).ready(function() {
                            var orderId = getUrlParameter('orderId');
                            $.get('shop/order?orderId=' + orderId, {}, function (data) {
                                var address = data.deliveryAddress.firstName + ' ' + data.deliveryAddress.lastName + '<br/>';
                                address += data.deliveryAddress.street + '<br/>';
                                if (data.deliveryAddress.company !== null && data.deliveryAddress.company.length > 0) {
                                    address += data.deliveryAddress.company + '<br/>';
                                }
                                address += data.deliveryAddress.postCode + '<br/>';
                                address += data.deliveryAddress.city + '<br/>';
                                address += data.deliveryAddress.country + '<br/>';
                                $('#shipping-address').html(address);
                                $('.physical-item').html(data.physicalItemName);
                            });
                        });
                    </script>
                </div>""");

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualTo("""
                <div class="container">
                    <script>
                        $(document).ready(function() {
                            var orderId = getUrlParameter('orderId');
                            $.get('shop/order?orderId=' + orderId, {}, function (data) {
                                var address = data.deliveryAddress.firstName + ' ' + data.deliveryAddress.lastName + '<br/>';
                                address += data.deliveryAddress.street + '<br/>';
                                if (data.deliveryAddress.company !== null && data.deliveryAddress.company.length > 0) {
                                    address += data.deliveryAddress.company + '<br/>';
                                }
                                address += data.deliveryAddress.postCode + '<br/>';
                                address += data.deliveryAddress.city + '<br/>';
                                address += data.deliveryAddress.country + '<br/>';
                                $('#shipping-address').html(address);
                                $('.physical-item').html(data.physicalItemName);
                            });
                        });
                    </script>
                </div>""");
    }

    @Test
    void onMethods() {
        codeResolver.givenCode("template.jte", "@param String userName\n\n<span onclick=\"showName('${userName}')\">Click me</span>");

        templateEngine.render("template.jte", "'); alert('xss", output);

        assertThat(output.toString()).isEqualTo("\n<span onclick=\"showName('\\x27); alert(\\x27xss')\">Click me</span>");
    }

    @Test
    void onMethods_policy() {
        OwaspHtmlPolicy policy = new OwaspHtmlPolicy();
        policy.addPolicy(new PreventInlineEventHandlers());
        templateEngine.setHtmlPolicy(policy);
        codeResolver.givenCode("template.jte", "@param String name\n\n<span data-title=\"${name}\" onclick=\"showName('${name}')\">Click me</span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "'); alert('xss", output));

        assertThat(throwable).hasMessage("Failed to compile template.jte, error at line 3: Inline event handlers are not allowed: onclick");
    }

    @Test
    void css() {
        codeResolver.givenCode("template.jte", """
                <style type="text/css">
                /*<![CDATA[*/
                body {
                \tcolor: #333333;
                \tline-height: 150%;
                }

                thead {
                \tfont-weight: bold;
                \tbackground-color: #CCCCCC;
                }
                /*]]>*/
                </style>""");

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualTo("""
                <style type="text/css">

                body {
                \tcolor: #333333;
                \tline-height: 150%;
                }

                thead {
                \tfont-weight: bold;
                \tbackground-color: #CCCCCC;
                }

                </style>""");
    }

    @Test
    void inlineStyle() {
        // TODO We will probably forbid this by HtmlPolicy, so not needed atm
    }

    @Test
    void forbidUnquotedAttributeValues() {
        codeResolver.givenCode("template.jte", "@param String id\n\n<span id=${id}></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "test", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Unquoted HTML attribute values are not allowed: id");
    }

    @Test
    void forbidUnqotedAttributeValues_attributeContentIsIgnored() {
        codeResolver.givenCode("template.jte", "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualTo("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
    }

    @Test
    void forbidUnqotedAttributeValues_emptyAttribute() {
        codeResolver.givenCode("template.jte", "<div data-test-important-content></div>");

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualTo("<div data-test-important-content></div>");
    }

    @Test
    void singleQuotedAttributeValues() {
        codeResolver.givenCode("template.jte", "@param String id\n\n<span id='${id}'></span>");

        templateEngine.render("template.jte", "<script>console.log(\"Hello\")</script>", output);

        assertThat(output.toString()).isEqualTo("\n<span id='&lt;script>console.log(&#34;Hello&#34;)&lt;/script>'></span>");
    }

    @Test
    void forbidSingleQuotedAttributeValues() {
        OwaspHtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
        htmlPolicy.addPolicy(new PreventSingleQuotedAttributes());
        templateEngine.setHtmlPolicy(htmlPolicy);
        codeResolver.givenCode("template.jte", "@param String id\n\n<span id='${id}'></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "test", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: HTML attribute values must be double quoted: id");
    }

    @Test
    void forbidSingleQuotedAttributeValues_boolean() {
        OwaspHtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
        htmlPolicy.addPolicy(new PreventSingleQuotedAttributes());
        templateEngine.setHtmlPolicy(htmlPolicy);
        codeResolver.givenCode("template.jte", "@param String id\n<input id=\"${id}\" required>");

        templateEngine.render("template.jte", "test", output);

        assertThat(output.toString()).isEqualTo("<input id=\"test\" required>");
    }

    @Test
    void forbidSingleQuotedAttributeValues_empty() {
        OwaspHtmlPolicy htmlPolicy = new OwaspHtmlPolicy();
        htmlPolicy.addPolicy(new PreventSingleQuotedAttributes());
        templateEngine.setHtmlPolicy(htmlPolicy);
        codeResolver.givenCode("template.jte", "@param String id\n<input data-webtest>");

        templateEngine.render("template.jte", "test", output);

        assertThat(output.toString()).isEqualTo("<input data-webtest>");
    }

    @Test
    void enumInTagBody() {
        codeResolver.givenCode("template.jte", "@param gg.jte.TemplateEngineTest.ModelType type\n<div>${type}</div>");

        templateEngine.render("template.jte", TemplateEngineTest.ModelType.Two, output);

        assertThat(output.toString()).isEqualTo("<div>Two</div>");
    }

    @Test
    void enumInTagAttribute() {
        codeResolver.givenCode("template.jte", "@param gg.jte.TemplateEngineTest.ModelType type\n<div data-type=\"${type}\"></div>");

        templateEngine.render("template.jte", TemplateEngineTest.ModelType.Two, output);

        assertThat(output.toString()).isEqualTo("<div data-type=\"Two\"></div>");
    }

    @Test
    void nullInTagBody() {
        codeResolver.givenCode("template.jte", "@param String type\n<div>${type}</div>");

        templateEngine.render("template.jte", Collections.singletonMap("type", null), output);

        assertThat(output.toString()).isEqualTo("<div></div>");
    }

    @Test
    void nullInTagAttribute() {
        codeResolver.givenCode("template.jte", "@param String type\n<div data-type=\"${type}\"></div>");

        templateEngine.render("template.jte", Collections.singletonMap("type", null), output);

        assertThat(output.toString()).isEqualTo("<div></div>");
    }

    @Test
    void uppercaseTag() {
        codeResolver.givenCode("template.jte", "@param String url\n<A href=\"${url}\">Click me!</A>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "javascript:alert(1)", output));

        assertThat(throwable.getMessage()).isEqualTo("Failed to compile template.jte, error at line 2: HTML tags are expected to be lowercase: A");
    }

    @Test
    void uppercaseAttribute() {
        codeResolver.givenCode("template.jte", "@param String url\n<a HREF=\"${url}\">Click me!</a>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "javascript:alert(1)", output));

        assertThat(throwable.getMessage()).isEqualTo("Failed to compile template.jte, error at line 2: HTML attributes are expected to be lowercase: HREF");
    }

    @Test
    void contentBlockInAttribute() {
        codeResolver.givenCode("template.jte", "@param gg.jte.Content content = @`This is \"the way\"!`\n<span data-title=\"${content}\">Info</span>");

        templateEngine.render("template.jte", new HashMap<>(), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is &#34;the way&#34;!\">Info</span>");
    }

    @Test
    void contentBlockInAttribute2() {
        codeResolver.givenCode("template.jte", "@param gg.jte.Content content = @`This is <b>the way</b>!`\n<span data-title=\"${content}\" foo=\"bar\">${content}</span>");

        templateEngine.render("template.jte", new HashMap<>(), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is &lt;b>the way&lt;/b>!\" foo=\"bar\">This is <b>the way</b>!</span>");
    }

    @Test
    void contentBlockInAttribute3() {
        codeResolver.givenCode("template.jte",
                """
                        @param String url
                        !{gg.jte.Content content = @`<a href="${url}" class="foo">Hello</a>`;}
                        ${content}""");

        templateEngine.render("template.jte", TemplateUtils.toMap("url", "https://jte.gg"), output);

        assertThat(output.toString()).isEqualTo("\n<a href=\"https://jte.gg\" class=\"foo\">Hello</a>");
    }

    @Test
    void contentBlockInAttribute4() {
        codeResolver.givenCode("template.jte",
                """
                        @param String url
                        !{gg.jte.Content content = @`<a href="${url}" class="foo">Hello</a>`;}
                        <span data-content="${content}">${content}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("url", "https://jte.gg"), output);

        assertThat(output.toString()).isEqualTo("""
                
                <span data-content="&lt;a href=&#34;https://jte.gg&#34; class=&#34;foo&#34;>Hello&lt;/a>"><a href="https://jte.gg" class="foo">Hello</a></span>\
                """);
    }

    @Test
    void javascriptUrl() {
        codeResolver.givenCode("template.jte", "@param String url\n<a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.jte", "javascript:alert(1)", output);

        assertThat(output.toString()).isEqualTo("<a href=\"\">Click me!</a>");
    }

    @Test
    void javascriptUrl_uppercase() {
        codeResolver.givenCode("template.jte", "@param String url\n<a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.jte", "JAVASCRIPT:alert(1)", output);

        assertThat(output.toString()).isEqualTo("<a href=\"\">Click me!</a>");
    }

    @Test
    void javascriptUrl_mixedcase() {
        codeResolver.givenCode("template.jte", "@param String url\n<a href=\"${url}\">Click me!</a>");

        templateEngine.render("template.jte", " \n\t jAvaScRipT:alert(1)", output);

        assertThat(output.toString()).isEqualTo("<a href=\"\">Click me!</a>");
    }

    @Test
    void tagCallInScript() {
        codeResolver.givenCode("tag/snippet.jte", "var x = y;");
        codeResolver.givenCode("template.jte", "@param String ignored\n<script>\nfunction() {\n@template.tag.snippet()\n}\n</script>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "ignored", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 4: Template calls in <script> blocks are not allowed.");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void customPolicy_null() {
        Throwable throwable = catchThrowable(() -> templateEngine.setHtmlPolicy(null));
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    void customOutput() {
        class CustomHtmlTemplateOutput implements HtmlTemplateOutput {
            final StringBuilder history = new StringBuilder();

            @Override
            public void writeContent(String value) {
                output.writeContent(value);
            }

            @Override
            public void writeContent(String value, int beginIndex, int endIndex) {
                output.writeContent(value, beginIndex, endIndex);
            }

            @Override
            public void setContext(String tagName, String attributeName) {
                history.append("{").append(tagName).append(",").append(attributeName).append("}");
            }
        }

        CustomHtmlTemplateOutput customHtmlOutput = new CustomHtmlTemplateOutput();
        codeResolver.givenCode("template.jte", "@param String p\n<span data-title=\"${p}\">foo</span>");

        templateEngine.render("template.jte", "hello", customHtmlOutput);

        assertThat(output.toString()).isEqualTo("<span data-title=\"hello\">foo</span>");
        assertThat(customHtmlOutput.history.toString()).isEqualTo("{span,data-title}{span,null}");
    }

    @Test
    void layoutCallInScript() {
        codeResolver.givenCode("layout/snippet.jte", "var x = y;");
        codeResolver.givenCode("template.jte", "@param String ignored\n<script>\nfunction() {\n@template.layout.snippet()\n}\n</script>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "ignored", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 4: Template calls in <script> blocks are not allowed.");
    }

    @Test
    void ifInAttributes() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button class=\"submit cta\" @if(disabled)disabled=\"disabled\"@endif data-item=\"${id}\">Do it</button>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", true, output));

        assertThat(throwable).hasMessage("Failed to compile template.jte, error at line 2: Illegal HTML attribute name @if(disabled)disabled! @if expressions in HTML attribute names are not allowed. In case you're trying to optimize the generated output, smart attributes will do just that: https://jte.gg/html-rendering/#smart-attributes");
    }

    @Test
    void forInAttributes() {
        codeResolver.givenCode("template.jte", "<div @for(int i = 0; i < 1; ++i)x@endfor>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", true, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 1: Illegal HTML attribute name @for(int! @for loops in HTML attribute names are not allowed.");
    }

    @Test
    void layoutInAttributes() {
        codeResolver.givenCode("template.jte", "<div @template.layout.foo()>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", localizer, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 1: Illegal HTML attribute name @template.layout.foo()! Template calls in HTML attribute names are not allowed.");
    }

    @Test
    void tagInAttributes() {
        codeResolver.givenCode("template.jte", "<div @template.tag.foo()>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", localizer, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 1: Illegal HTML attribute name @template.tag.foo()! Template calls in HTML attribute names are not allowed.");
    }

    @Test
    void contentBlockInAttributes() {
        codeResolver.givenCode("template.jte", "<div @`foo`>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", localizer, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 1: Illegal HTML attribute name @`foo`! Content blocks in HTML attribute names are not allowed.");
    }

    @Test
    void alpineJsAttributes() {
        codeResolver.givenCode("template.jte", "<div @click.away=\"open = false\" x-data=\"{ open: false }\"></div>");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<div @click.away=\"open = false\" x-data=\"{ open: false }\"></div>");
    }

    @Test
    void unsafe_null() {
        codeResolver.givenCode("template.jte", "@param String x\nHello, $unsafe{x}");

        templateEngine.render("template.jte", (String)null, output);

        assertThat(output.toString()).isEqualTo("Hello, ");
    }

    @Test
    void invalidAttribute_semicolon() {
        codeResolver.givenCode("template.jte", "<p class=\"my-class\" style=\"margin: 0\";>This is a text</p>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", null, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 1: Invalid HTML attribute name ;!");
    }

    @Test
    void templateStringInJavaScriptBlock_backtick() {
        codeResolver.givenCode("template.jte", """
                @param String someMessage
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>XSS Test</title>
                    <script>window.someVariable = `${someMessage}`;</script>
                </head>
                <body>
                <h1>XSS Test</h1>
                </body>
                </html>
                """);

        templateEngine.render("template.jte", "` + alert(`xss`) + `", output);

        assertThat(output.toString()).isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>XSS Test</title>
                    <script>window.someVariable = `\\` + alert(\\`xss\\`) + \\``;</script>
                </head>
                <body>
                <h1>XSS Test</h1>
                </body>
                </html>
                """);
    }

    @Test
    void templateStringInJavaScriptBlock_dollar() {
        codeResolver.givenCode("template.jte", """
                @param String someMessage
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>XSS Test</title>
                    <script>window.someVariable = `${someMessage}`;</script>
                </head>
                <body>
                <h1>XSS Test</h1>
                </body>
                </html>
                """);

        templateEngine.render("template.jte", "${secret}", output);

        assertThat(output.toString()).isEqualTo("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>XSS Test</title>
                    <script>window.someVariable = `\\${secret}`;</script>
                </head>
                <body>
                <h1>XSS Test</h1>
                </body>
                </html>
                """);
    }

    @Test
    void templateStringInJavaScriptAttribute_backtick() {
        codeResolver.givenCode("template.jte", "@param String p\n<span onClick=\"console.log(`${p}`)\">foo</span>");

        templateEngine.render("template.jte", "` + alert(`xss`) + `", output);

        assertThat(output.toString()).isEqualTo("<span onClick=\"console.log(`\\x60 + alert(\\x60xss\\x60) + \\x60`)\">foo</span>");
    }

    @Test
    void templateStringInJavaScriptAttribute_dollar() {
        codeResolver.givenCode("template.jte", "@param String p\n<span onClick=\"console.log(`${p}`)\">foo</span>");

        templateEngine.render("template.jte", "${secret}", output);

        assertThat(output.toString()).isEqualTo("<span onClick=\"console.log(`\\x24{secret}`)\">foo</span>");
    }

    @Test
    void localization_notFound_noParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("unknown")}</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_notFound_withParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("unknown", 1)}</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_emptyString_noParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("empty")}</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_emptyString_withParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("empty", 1)}</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_emptyString_resultsInNullContent() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>@if(localizer.localize("empty") == null)nothing is here@endif</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>nothing is here</span>");
    }

    @Test
    void localization_emptyString_resultsInNullContent_withParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>@if(localizer.localize("empty", 1) == null)nothing is here@endif</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>nothing is here</span>");
    }

    @Test
    void localization_noParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span alt="${localizer.localize("no-params")}">${localizer.localize("no-params")}</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span alt=\"This is a key without params\">This is a key without params</span>");
    }

    @Test
    void localization_html() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("no-params-html")}</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>This is a key without params but with <b>html content</b></span>");
    }

    @Test
    void localization_oneParam() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("one-param", param)}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: &lt;script&gt;evil()&lt;/script&gt;.</span>");
    }

    @Test
    void localization_html_oneParam() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("one-param-html", param)}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>&lt;script&gt;evil()&lt;/script&gt;</b>. Including HTML in key!</span>");
    }

    @Test
    void localization_inception() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("one-param-html", localizer.localize("one-param-html", param))}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b>&lt;script&gt;evil()&lt;/script&gt;</b>. Including HTML in key!</b>. Including HTML in key!</span>");
    }

    @Test
    void localization_quotesInAttribute() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span data-title="${localizer.localize("quotes")}"></span>\
                """);

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is a key with &#34;quotes&#34;\"></span>");
    }

    @Test
    void localization_quotesInAttributeWithParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String p1
                @param String p2
                @param String p3
                <span data-title="${localizer.localize("quotes-params", p1, p2, p3)}"></span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "p1", "<script>evil()</script>", "p2", "p2", "p3", "p3"), output);

        assertThat(output.toString()).isEqualTo("<span data-title=\"This is a key with &#34;quotes&#34; and params &lt;i>&#34;&lt;script>evil()&lt;/script>&#34;&lt;/i>, &lt;b>&#34;p2&#34;&lt;/b>, &#34;p3&#34;...\"></span>");
    }

    @Test
    void localization_manyParams_noneSet() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("many-params-html")}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>{0}</i>, <b>{1}</b>, {2}</span>");
    }

    @Test
    void localization_manyParams_primitives() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("many-params-html")}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "p1", true, "p2", 1, "p3", 2), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>{0}</i>, <b>{1}</b>, {2}</span>");
    }

    @Test
    void localization_manyParams_oneSet() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("many-params-html", param)}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>&lt;script&gt;evil()&lt;/script&gt;</i>, <b></b>, </span>");
    }

    @Test
    void localization_manyParams_allSame() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("many-params-html", param, param, param)}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>&lt;script&gt;evil()&lt;/script&gt;</i>, <b>&lt;script&gt;evil()&lt;/script&gt;</b>, &lt;script&gt;evil()&lt;/script&gt;</span>");
    }

    @Test
    void localization_badPattern() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String param
                <span>${localizer.localize("bad-pattern", param)}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello {foo}</span>");
    }

    @Test
    void localization_primitives() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("all-primitives", false, (byte)1, (short)2, 3, 4L, 5.0f, 6.0, 'c')}</span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>boolean: false, byte: 1, short: 2, int: 3, long: 4, float: 5.0, double: 6.0, char: c</span>");
    }

    @Test
    void localization_primitives_inAttribute() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span alt="${localizer.localize("all-primitives", false, (byte)1, (short)2, 3, 4L, 5.0f, 6.0, 'c')}"></span>\
                """);

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span alt=\"boolean: false, byte: 1, short: 2, int: 3, long: 4, float: 5.0, double: 6.0, char: c\"></span>");
    }

    @Test
    void localization_enum() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param gg.jte.ContentType contentType
                <span alt="${localizer.localize("enum", contentType)}">${localizer.localize("enum", contentType)}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "contentType", ContentType.Html), output);

        assertThat(output.toString()).isEqualTo("<span alt=\"Content type is: Html\">Content type is: Html</span>");
    }

    @Test
    void localization_null() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param gg.jte.ContentType contentType
                <span alt="${localizer.localize("enum", contentType)}">${localizer.localize("enum", contentType)}</span>""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "contentType", null), output);

        assertThat(output.toString()).isEqualTo("<span alt=\"Content type is: \">Content type is: </span>");
    }

    @Test
    void localization_unsupportedType() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param java.util.Date date
                ${localizer.localize("enum", date)}""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "date", new Date()), output);

        assertThat(output.toString()).isEqualTo("Content type is: ");
    }

    @Test
    void localization_tag() {
        codeResolver.givenCode("tag/card.jte", """
                    @param gg.jte.Content content
                    <span>${content}</span>\
                    """);
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String name
                @template.tag.card(content = @`<b>${localizer.localize("one-param", name)}</b>`)""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span><b>This is a key with user content: &lt;script&gt;.</b></span>");
    }

    @Test
    void localization_tag2() {
        codeResolver.givenCode("tag/card.jte", """
                @param gg.jte.Content content
                <span>${content}</span>\
                """);
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String name
                @template.tag.card(content = localizer.localize("one-param", name))""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: &lt;script&gt;.</span>");
    }

    @Test
    void localization_tag3() {
        codeResolver.givenCode("tag/card.jte", """
                @param gg.jte.Content content
                <span>${content}</span>\
                """);
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String name
                @template.tag.card(content = localizer.localize("one-param", @`<b>${name}</b>`))""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>&lt;script&gt;</b>.</span>");
    }

    @Test
    void localization_tag4() {
        codeResolver.givenCode("tag/card.jte", """
                @param gg.jte.Content content
                <span>${content}</span>\
                """);
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String name
                @template.tag.card(content = localizer.localize("many-params-html", @`<span>${name}</span>`, @`<span>${name}</span>`, @`<span>${name}</span>`))""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i><span>&lt;script&gt;</span></i>, <b><span>&lt;script&gt;</span></b>, <span>&lt;script&gt;</span></span>");
    }

    @Test
    void localization_tag5() {
        codeResolver.givenCode("tag/card.jte", """
                @param gg.jte.Content content
                <span>${content}</span>\
                """);
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String name
                @template.tag.card(content = localizer.localize("one-param", @`<b>${localizer.localize("one-param-html", @`<i>${name}</i>`)}</b>`))""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</b>.</span>");
    }

    @Test
    void localization_tag6() {
        codeResolver.givenCode("tag/card.jte", """
                @param gg.jte.Content content
                <span>${content}</span>\
                """);
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String name
                @template.tag.card(content = localizer.localize("one-param", @`<b>@template.tag.card(content = localizer.localize("one-param-html", @`<i>${name}</i>`))</b>`))""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b><span>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</span></b>.</span>");
    }

    @Test
    void localization_tag7() {
        codeResolver.givenCode("tag/card.jte", """
                @param gg.jte.Content content
                <span>${content}</span>\
                """);
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                @param String name
                !{gg.jte.Content content = localizer.localize("one-param", @`<b>${localizer.localize("one-param-html", @`<i>${name}</i>`)}</b>`);}@template.tag.card(content = content)""");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</b>.</span>");
    }

    @Test
    void localization_tag8() {
        codeResolver.givenCode("tag/card.jte", """
                @param gg.jte.Content content = @`My default is ${42}`
                <span>${content}</span>\
                """);
        codeResolver.givenCode("template.jte", "@template.tag.card()");

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>My default is 42</span>");
    }

    @Test
    void localization_contentParams() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("link", @`<a href="${"foo"}">`, @`</a>`)}</span>\
                """);

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer), output);

        assertThat(output.toString()).isEqualTo("<span>Hello? <a href=\"foo\">Click here</a></span>");
    }

    @Test
    void localization_contentParams2() {
        codeResolver.givenCode("template.jte", """
                @param gg.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer
                <span>${localizer.localize("link", @`<a style="color: ${"foo"};">`, @`</a>`)}</span>\
                """);

        templateEngine.render("template.jte", TemplateUtils.toMap("localizer", localizer), output);

        assertThat(output.toString()).isEqualTo("<span>Hello? <a style=\"color: foo;\">Click here</a></span>");
    }

    @Nested
    class DynamicAttributesForHotwire {

        static class HotwireHtmlPolicy extends PolicyGroup {
            HotwireHtmlPolicy() {
                addPolicy(new PreventUppercaseTagsAndAttributes());
                addPolicy(new PreventOutputInTagsAndAttributes(false));
                addPolicy(new PreventUnquotedAttributes());
            }
        }

        @BeforeEach
        void setUp() {
            templateEngine.setHtmlPolicy(new HotwireHtmlPolicy());
        }

        @Test
        void attributes_dynamicNameForHotwire() {
            codeResolver.givenCode("template.jte", "@param String controller\n@param String target\n<div data-controller=\"hello\">\n<input data-${controller}-target=\"${target}\"/></div>");

            templateEngine.render("template.jte", TemplateUtils.toMap("controller", "hello", "target", "name"), output);

            assertThat(output.toString()).isEqualTo("<div data-controller=\"hello\">\n<input data-hello-target=\"name\"/></div>");
        }

        @Test
        void attributes_dynamicNameForHotwire_unsafe() {
            codeResolver.givenCode("template.jte", "@param String controller\n@param String target\n<div data-controller=\"hello\">\n<input data-$unsafe{controller}-target=\"${target}\"/></div>");

            templateEngine.render("template.jte", TemplateUtils.toMap("controller", "hello", "target", "name"), output);

            assertThat(output.toString()).isEqualTo("<div data-controller=\"hello\">\n<input data-hello-target=\"name\"/></div>");
        }

        @Test
        void attributes_dynamicNameForHotwire_unsafe_worksWithDefaultPolicyToo() {
            templateEngine.setHtmlPolicy(new OwaspHtmlPolicy());
            codeResolver.givenCode("template.jte", "@param String controller\n@param String target\n<div data-controller=\"hello\">\n<input data-$unsafe{controller}-target=\"${target}\"/></div>");

            templateEngine.render("template.jte", TemplateUtils.toMap("controller", "hello", "target", "name"), output);

            assertThat(output.toString()).isEqualTo("<div data-controller=\"hello\">\n<input data-hello-target=\"name\"/></div>");
        }

        @Test
        void attributes_dynamicAttributesForHtmx() {
            codeResolver.givenCode("template.jte", "@param String htmx\n<div ${htmx}/>");

            templateEngine.render("template.jte", TemplateUtils.toMap("htmx", "hx-get=/helloWorld hx-swap=outerHTML"), output);

            assertThat(output.toString()).isEqualTo("<div hx-get=/helloWorld hx-swap=outerHTML/>");
        }

        @Test
        void attributes_dynamicAttributesForHtmx_unsafe() {
            codeResolver.givenCode("template.jte", "@param String htmx\n<div $unsafe{htmx}/>");

            templateEngine.render("template.jte", TemplateUtils.toMap("htmx", "hx-get=/helloWorld hx-swap=outerHTML"), output);

            assertThat(output.toString()).isEqualTo("<div hx-get=/helloWorld hx-swap=outerHTML/>");
        }
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
                "empty", "",
                "link", "Hello? {0}Click here{1}"
        );

        @Override
        public String lookup(String key) {
            return (String)resources.get(key);
        }
    }
}
