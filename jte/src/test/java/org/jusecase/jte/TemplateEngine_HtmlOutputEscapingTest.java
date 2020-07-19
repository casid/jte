package org.jusecase.jte;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.jusecase.jte.output.StringOutput;


public class TemplateEngine_HtmlOutputEscapingTest {

    DummyCodeResolver codeResolver = new DummyCodeResolver();
    TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

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

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Illegal tag name ${tag}! Expressions in tag names are not allowed.");
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

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Illegal attribute name ${attribute}! Expressions in attribute names are not allowed.");
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
        // TODO
        // onclick
        // onmouseover
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
        // TODO
    }

    @Test
    void forbidUnquotedAttributeValues() {
        codeResolver.givenCode("template.jte", "@param String id\n\n<span id=${id}></span>");

        Throwable throwable = catchThrowable(() -> templateEngine.render("template.jte", "test", output));

        assertThat(throwable).isInstanceOf(TemplateException.class).hasMessage("Failed to compile template.jte, error at line 3: Unquoted attribute values are not allowed.");
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
    void forbidMoreThanOneOutputPerAttribute() {
        // TODO check if we really want to do this
    }
}
