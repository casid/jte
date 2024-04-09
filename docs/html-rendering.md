---
title: jte HTML Rendering
description: How jte safely renders HTML content.
---

# HTML Rendering

For rendering HTML documents, [`gg.jte.ContentType.Html`](https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg.jte.runtime/gg/jte/ContentType.html) is highly recommended for [security](#html-escaping) but also for convenience.

### Smart Attributes

Expressions in HTML attributes are evaluated, so that optimal output is generated. This means attributes with a single output that evaluates to `#!java null` or `#!java false` are not rendered. For instance:

```html linenums="1"
<span data-title="${null}">Info</span>
```

Will be rendered as:

```html linenums="1"
<span>Info</span>
```

If an HTML attribute is boolean, jte requires you to provide a boolean expression, and it will omit the attribute if that expression evaluates to `#!java false`. For example:

```html linenums="1"
<select id="cars">
<option value="volvo" selected="${false}">Volvo</option>
<option value="saab" selected="${true}">Saab</option>
<option value="opel" selected="${false}">Opel</option>
<option value="audi" selected="${false}">Audi</option>
</select>
```

Will render this HTML:

```html linenums="1"
<select id="cars">
<option value="volvo">Volvo</option>
<option value="saab" selected>Saab</option>
<option value="opel">Opel</option>
<option value="audi">Audi</option>
</select>
```

### Natural comments

jte does not render HTML, CSS and JavaScript comments. You can use the natural comment syntax without worrying about leaking information/data to the outside.

## HTML Escaping { #html-escaping }

Output escaping depends on the [`gg.jte.ContentType`](https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg.jte.runtime/gg/jte/ContentType.html) the engine is created with:

- With `ContentType.Plain`, there is no output escaping.
- With `ContentType.Html`, the [OwaspHtmlTemplateOutput](https://github.com/casid/jte/blob/{{ latest-git-tag }}/jte-runtime/src/main/java/gg/jte/html/OwaspHtmlTemplateOutput.java) is used for context sensitive output escaping.

In `Html` mode, user content `${}` is automatically escaped, depending on what part of the template it is placed into:

- HTML tag bodies
- HTML attributes
- JavaScript attributes, e.g. `#!html onclick`
- `#!html <script>` blocks

### HTML tag bodies

User output in HTML tag bodies is escaped with `gg.jte.html.escape.Escape.htmlContent`.

```html linenums="1"
<div>${userName}</div>
```

With `userName` being `#!html <script>alert('xss');</script>`, the output would be:

```html linenums="1"
<div>&lt;script&gt;alert('xss');&lt;/script&gt;</div>`
```

### HTML attributes

User output in HTML attributes is escaped with `gg.jte.html.escape.Escape.htmlAttribute`. It ensures that all quotes are escaped, so an attacker cannot escape the attribute.

```html linenums="1"
<div data-title="Hello ${userName}"></div>
```

With `userName` being `#!html "><script>alert('xss')</script>`, the output would be:

```html linenums="1"
<div data-title="Hello &#34;>&lt;script>alert(&#39;xss&#39;)&lt;/script>"></div>
```

The quote `"` is escaped with `&#34;` and the attacker cannot escape the attribute.

### JavaScript attributes

User output in HTML attributes is escaped with `gg.jte.html.escape.Escape.javaScriptAttribute`. Those are all HTML attributes starting with `on`.

```html linenums="1"
<span onclick="showName('${userName}')">Click me</span>
```

With `userName` being `'); alert('xss`, the output would be 

```html linenums="1"
<span onclick="showName('\x27); alert(\x27xss')">Click me</span>
```

In case you run a [strict content security policy](https://csp.withgoogle.com/docs/strict-csp.html) without `unsafe-inline`, you could configure jte to run with `gg.jte.html.policy.PreventInlineEventHandlers`. Then, using inline event handlers would cause errors at compile time. See [this issue](https://github.com/casid/jte/issues/20) for additional context.

=== "Java"

    ```java linenums="1"
    public class MyHtmlPolicy extends OwaspHtmlPolicy {
        public MyHtmlPolicy() {
            addPolicy(new PreventInlineEventHandlers());
        }
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    class MyHtmlPolicy : OwaspHtmlPolicy {
        init {
            addPolicy(PreventInlineEventHandlers())
        }
    }
    ```

Then, you set it with `templateEngine.setHtmlPolicy(new MyHtmlPolicy());`.

For more examples, you may want to check out the [`TemplateEngine_HtmlOutputEscapingTest`](https://github.com/casid/jte/blob/{{ latest-git-tag }}/jte/src/test/java/gg/jte/TemplateEngine_HtmlOutputEscapingTest.java).

### Unsafe

In rare cases, you may want to skip output escaping for a specific element. You can do this by using `$unsafe{}` instead of `${}`. For instance, to trust the `userName`, you would write:

```html linenums="1"
<div>$unsafe{userName}</div>
```

The syntax `$unsafe{}` was picked on purpose. Whenever you use it, you're risking [XSS attacks](https://owasp.org/www-community/attacks/xss/), and you should carefully consider if it really is okay to trust the data you're outputting.

### Custom output escaping

It is possible to provide your own implementation of `gg.jte.html.HtmlTemplateOutput`. Maybe you want to extend the default [`gg.jte.html.OwaspHtmlTemplateOutput`](https://github.com/casid/jte/blob/{{ latest-git-tag }}/jte-runtime/src/main/java/gg/jte/html/OwaspHtmlTemplateOutput.java), or use your implementation.

Before rendering, you'd simply wrap the actual `gg.jte.TemplateOutput` you are using:

=== "Java"

    ```java linenums="1"
    TemplateOutput output = new MySecureHtmlOutput(new StringOutput());
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    val output = MySecureHtmlOutput(StringOutput())
    ```

## Raw output

Sometimes, it is required to output text as is. Use the `@raw` keyword to open a raw section unprocessed by jte.

```html linenums="1"
@raw
<script>
    const foo = "foo";
    console.log(`This is ${foo}`);
</script>
@endraw
```
