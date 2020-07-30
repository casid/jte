package org.jusecase.jte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.output.StringOutput;
import org.jusecase.jte.support.LocalizationSupport;


public class TemplateEngine_HtmlOutputEscapingTest {

    DummyCodeResolver codeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

    MyLocalizer localizer = new MyLocalizer();
    StringOutput output = new StringOutput();

    @Test
    void outputEscaping() {
        codeResolver.givenCode("template.jte",
                "@param String url\n" +
                "@param String title\n" +
                "Look at <a href=\"${url}\">${title}</a>");

        templateEngine.render("template.jte", Map.of("url", "https://www.test.com?param1=1&param2=2", "title", "<script>alert('hello');</script>"), output);

        assertThat(output.toString()).isEqualTo("Look at <a href=\"https://www.test.com?param1=1&amp;param2=2\">&lt;script&gt;alert(&#39;hello&#39;);&lt;/script&gt;</a>");
    }

    @Test
    void unclosedTag() {
        codeResolver.givenCode("unclosed.jte", "<span><a></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("unclosed.jte", null, output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile unclosed.jte, error at line 1: Unclosed tag <a>, expected </a>, got </span>.");
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
    void booleanAttributes_true() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button disabled=\"${disabled}\">Click</button>");

        templateEngine.render("template.jte", true, output);

        assertThat(output.toString()).isEqualTo("<button disabled>Click</button>");
    }

    @Test
    void booleanAttributes_false() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button disabled=\"${disabled}\">Click</button>");

        templateEngine.render("template.jte", false, output);

        assertThat(output.toString()).isEqualTo("<button >Click</button>");
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

        assertThat(output.toString()).isEqualTo("<input id=\"login-email\" type=\"text\" placeholder=\"dummy\" required>\n" +
                "<input id=\"login-password\" type=\"password\" placeholder=\"Savecode\" required>");
    }

    @Test
    void booleanAttributes_condition() {
        codeResolver.givenCode("template.jte", "@param boolean disabled\n<button class=\"submit cta\" @if(disabled)disabled=\"disabled\"@endif data-item=\"${id}\">Do it</button>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", true, output));

        assertThat(throwable).hasMessage("Failed to compile template.jte, error at line 2: Illegal HTML attribute name @if(disabled)disabled! Expressions in HTML attribute names are not allowed.");
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

        assertThat(output.toString()).isEqualTo("<button >Click</button>");
    }

    @Test
    void doctype() {
        codeResolver.givenCode("template.jte", "@param String x\n<!DOCTYPE html>");
        templateEngine.render("template.jte", (Object)null, output);
        assertThat(output.toString()).isEqualTo("<!DOCTYPE html>");
    }

    @Test
    void htmlComment() {
        codeResolver.givenCode("template.jte", "@param String name\n\n<!--Comment here with ${name}-->\n<span>Test</span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "Hello", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Expressions in HTML comments are not allowed.");
    }

    @Test
    void htmlComment_unsafe() {
        codeResolver.givenCode("template.jte", "@param String name\n\n<!--Comment here with $unsafe{name}-->\n<span>Test</span>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n<!--Comment here with Hello-->\n<span>Test</span>");
    }

    @Test
    void htmlComment_ignoredIfInAttribute() {
        codeResolver.givenCode("template.jte", "@param String name\n\n<span name=\"<!--this is not a comment ${name}-->\">Test</span>");

        templateEngine.render("template.jte", "Hello", output);

        assertThat(output.toString()).isEqualTo("\n<span name=\"<!--this is not a comment Hello-->\">Test</span>");
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
        codeResolver.givenCode("template.jte", "<div class=\"container\">\n" +
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

        templateEngine.render("template.jte", null, output);

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
        codeResolver.givenCode("template.jte", "@param String name\n\n<span onclick=\"showName('${name}')\">Click me</span>");

        templateEngine.render("template.jte", "'); alert('xss", output);

        assertThat(output.toString()).isEqualTo("\n<span onclick=\"showName('\\x27); alert(\\x27xss')\">Click me</span>");
    }

    @Test
    void css() {
        codeResolver.givenCode("template.jte", "<style type=\"text/css\">\n" +
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

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualTo("<style type=\"text/css\">\n" +
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
    }

    @Test
    void inlineStyle() {
        // TODO We will probably forbid this by HtmlPolicy, so not needed atm
    }

    @Test
    void forbidUnquotedAttributeValues() {
        codeResolver.givenCode("template.jte", "@param String id\n\n<span id=${id}></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "test", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Unquoted HTML attribute values are not allowed.");
    }

    @Test
    void forbidUnqotedAttributeValues_attributeContentIsIgnored() {
        codeResolver.givenCode("template.jte", "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");

        templateEngine.render("template.jte", null, output);

        assertThat(output.toString()).isEqualTo("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
    }

    @Test
    void singleQuotedAttributeValues() {
        codeResolver.givenCode("template.jte", "@param String id\n\n<span id='${id}'></span>");

        templateEngine.render("template.jte", "<script>console.log(\"Hello\")</script>", output);

        assertThat(output.toString()).isEqualTo("\n<span id='&lt;script>console.log(&#34;Hello&#34;)&lt;/script>'></span>");
    }

    @Test
    void enumInTagBody() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngineTest.ModelType type\n<div>${type}</div>");

        templateEngine.render("template.jte", TemplateEngineTest.ModelType.Two, output);

        assertThat(output.toString()).isEqualTo("<div>Two</div>");
    }

    @Test
    void enumInTagAttribute() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngineTest.ModelType type\n<div data-type=\"${type}\"></div>");

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

        assertThat(output.toString()).isEqualTo("<div data-type=\"\"></div>");
    }

    @Test
    void uppercaseTag() {
        codeResolver.givenCode("template.jte", "@param String url\n<A href=\"${url}\">Click me!</A>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "javascript:alert(1)", output));

        assertThat(throwable.getMessage()).isEqualTo("Failed to compile template.jte, error at line 2: HTML tags are expected to be lowercase.");
    }

    @Test
    void uppercaseAttribute() {
        codeResolver.givenCode("template.jte", "@param String url\n<a HREF=\"${url}\">Click me!</a>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "javascript:alert(1)", output));

        assertThat(throwable.getMessage()).isEqualTo("Failed to compile template.jte, error at line 2: HTML attributes are expected to be lowercase.");
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
        codeResolver.givenCode("template.jte", "@param String ignored\n<script>\nfunction() {\n@tag.snippet()\n}\n</script>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "ignored", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 4: @tag calls in <script> blocks are not allowed.");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void customPolicy_null() {
        Throwable throwable = catchThrowable(() -> templateEngine.setHtmlPolicy(null));
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }

    @Test
    void layoutCallInScript() {
        codeResolver.givenCode("layout/snippet.jte", "var x = y;");
        codeResolver.givenCode("template.jte", "@param String ignored\n<script>\nfunction() {\n@layout.snippet()\n}\n</script>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "ignored", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 4: @layout calls in <script> blocks are not allowed.");
    }

    @Test
    void localization_notFound_noParams() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "<span>${localizer.localize(\"unknown\")}</span>");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_notFound_withParams() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "<span>${localizer.localize(\"unknown\", 1)}</span>");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span></span>");
    }

    @Test
    void localization_noParams() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "<span alt=\"${localizer.localize(\"no-params\")}\">${localizer.localize(\"no-params\")}</span> $unsafe{localizer.localize(\"no-params\")}");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span alt=\"This is a key without params\">This is a key without params</span> This is a key without params");
    }

    @Test
    void localization_html() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "<span>${localizer.localize(\"no-params-html\")}</span>");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>This is a key without params but with <b>html content</b></span>");
    }

    @Test
    void localization_oneParam() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"one-param\", param)}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: &lt;script&gt;evil()&lt;/script&gt;.</span>");
    }

    @Test
    void localization_html_oneParam() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"one-param-html\", param)}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>&lt;script&gt;evil()&lt;/script&gt;</b>. Including HTML in key!</span>");
    }

    @Test
    void localization_inception() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"one-param-html\", localizer.localize(\"one-param-html\", param))}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b>&lt;script&gt;evil()&lt;/script&gt;</b>. Including HTML in key!</b>. Including HTML in key!</span>");
    }

    @Test
    void localization_manyParams_noneSet() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"many-params-html\")}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>{0}</i>, <b>{1}</b>, {2}</span>");
    }

    @Test
    void localization_manyParams_primitives() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"many-params-html\")}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "p1", true, "p2", 1, "p3", 2), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>{0}</i>, <b>{1}</b>, {2}</span>");
    }

    @Test
    void localization_manyParams_oneSet() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"many-params-html\", param)}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>&lt;script&gt;evil()&lt;/script&gt;</i>, <b></b>, </span>");
    }

    @Test
    void localization_manyParams_allSame() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"many-params-html\", param, param, param)}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i>&lt;script&gt;evil()&lt;/script&gt;</i>, <b>&lt;script&gt;evil()&lt;/script&gt;</b>, &lt;script&gt;evil()&lt;/script&gt;</span>");
    }

    @Test
    void localization_badPattern() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String param\n" +
                "<span>${localizer.localize(\"bad-pattern\", param)}</span>");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "param", "<script>evil()</script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello {foo}</span>");
    }

    @Test
    void localization_primitives() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "<span>${localizer.localize(\"all-primitives\", false, (byte)1, (short)2, 3, 4L, 5.0f, 6.0, 'c')}</span>");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>boolean: false, byte: 1, short: 2, int: 3, long: 4, float: 5.0, double: 6.0, char: c</span>");
    }

    @Test
    void localization_primitives_inAttribute() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "<span alt=\"${localizer.localize(\"all-primitives\", false, (byte)1, (short)2, 3, 4L, 5.0f, 6.0, 'c')}\"></span>");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span alt=\"boolean: false, byte: 1, short: 2, int: 3, long: 4, float: 5.0, double: 6.0, char: c\"></span>");
    }

    @Test
    void localization_primitives_unsafe() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "<span>$unsafe{localizer.localize(\"all-primitives\", false, (byte)1, (short)2, 3, 4L, 5.0f, 6.0, 'c')}</span>");

        templateEngine.render("template.jte", localizer, output);

        assertThat(output.toString()).isEqualTo("<span>boolean: false, byte: 1, short: 2, int: 3, long: 4, float: 5.0, double: 6.0, char: c</span>");
    }

    @Test
    void localization_enum() {
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param org.jusecase.jte.ContentType contentType\n" +
                "<span alt=\"${localizer.localize(\"enum\", contentType)}\">${localizer.localize(\"enum\", contentType)}</span> Unsafe: $unsafe{localizer.localize(\"enum\", contentType)}");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "contentType", ContentType.Html), output);

        assertThat(output.toString()).isEqualTo("<span alt=\"Content type is: Html\">Content type is: Html</span> Unsafe: Content type is: Html");
    }

    @Test
    void localization_tag() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content\n" +
                    "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String name\n" +
                "@tag.card(content = @`<b>${localizer.localize(\"one-param\", name)}</b>`)");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span><b>This is a key with user content: &lt;script&gt;.</b></span>");
    }

    @Test
    void localization_tag2() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String name\n" +
                "@tag.card(content = localizer.localize(\"one-param\", name))");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: &lt;script&gt;.</span>");
    }

    @Test
    void localization_tag3() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String name\n" +
                "@tag.card(content = localizer.localize(\"one-param\", @`<b>${name}</b>`))");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>&lt;script&gt;</b>.</span>");
    }

    @Test
    void localization_tag4() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String name\n" +
                "@tag.card(content = localizer.localize(\"many-params-html\", @`<span>${name}</span>`, @`<span>${name}</span>`, @`<span>${name}</span>`))");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>Hello <i><span>&lt;script&gt;</span></i>, <b><span>&lt;script&gt;</span></b>, <span>&lt;script&gt;</span></span>");
    }

    @Test
    void localization_tag5() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String name\n" +
                "@tag.card(content = localizer.localize(\"one-param\", @`<b>" +
                    "${localizer.localize(\"one-param-html\", @`<i>${name}</i>`)}" +
                "</b>`))");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</b>.</span>");
    }

    @Test
    void localization_tag6() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String name\n" +
                "@tag.card(content = localizer.localize(\"one-param\", @`<b>" +
                    "@tag.card(content = localizer.localize(\"one-param-html\", @`<i>${name}</i>`))" +
                "</b>`))");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b><span>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</span></b>.</span>");
    }

    @Test
    void localization_tag7() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@param org.jusecase.jte.TemplateEngine_HtmlOutputEscapingTest.MyLocalizer localizer\n" +
                "@param String name\n" +
                "!{var content = localizer.localize(\"one-param\", @`<b>" +
                "${localizer.localize(\"one-param-html\", @`<i>${name}</i>`)}" +
                "</b>`);}" +
                "@tag.card(content = content)");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>This is a key with user content: <b>This is a key with user content: <b><i>&lt;script&gt;</i></b>. Including HTML in key!</b>.</span>");
    }

    @Test
    void localization_tag8() {
        codeResolver.givenCode("tag/card.jte", "@param org.jusecase.jte.Content content = @`My default is ${42}`\n" +
                "<span>${content}</span>");
        codeResolver.givenCode("template.jte", "@tag.card()");

        templateEngine.render("template.jte", Map.of("localizer", localizer, "name", "<script>"), output);

        assertThat(output.toString()).isEqualTo("<span>My default is 42</span>");
    }

    @SuppressWarnings("unused")
    public static class MyLocalizer implements LocalizationSupport {
        Map<String, String> resources = Map.of(
                "no-params", "This is a key without params",
                "no-params-html", "This is a key without params but with <b>html content</b>",
                "one-param", "This is a key with user content: {0}.",
                "one-param-html", "This is a key with user content: <b>{0}</b>. Including HTML in key!",
                "many-params-html", "Hello <i>{0}</i>, <b>{1}</b>, {2}",
                "bad-pattern", "Hello {foo}",
                "all-primitives", "boolean: {0}, byte: {1}, short: {2}, int: {3}, long: {4}, float: {5}, double: {6}, char: {7}",
                "enum", "Content type is: {0}"
        );

        @Override
        public String lookup(String key) {
            return resources.get(key);
        }
    }
}
