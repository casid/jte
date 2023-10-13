---
title: jte Localization
description: How to implement Localization/Internationalization in jte.
---

# Localization

jte has no built-in keywords for localization. Instead, it provides a flexible interface so that you can easily use the same localization mechanism you're used to in your project. This has several advantages:

- No need to learn yet another way to localize things
- No need to reverse engineer another opinionated localization implementation
- Your users receive the exact localization through jte as they do from the rest of your application

Let's implement `gg.jte.support.LocalizationSupport`. There's only one method to implement:

=== "Java"

    ```java linenums="1"
    public class JteLocalizer implements gg.jte.support.LocalizationSupport {

        private final OtherFrameworkLocalizer frameworkLocalizer;
        private final Locale locale;

        public JteLocalizer(OtherFrameworkLocalizer frameworkLocalizer, Locale locale) {
            this.frameworkLocalizer = frameworkLocalizer;
            this.locale = locale;
        }

        @Override
        public String lookup(String key) {
            // However this works in your localization framework
            return frameworkLocalizer.get(locale, key);
        }
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    class JteLocalizer(frameworkLocalizer: OtherFrameworkLocalizer, locale: Locale) : gg.jte.support.LocalizationSupport {
        override fun lookup(String key): String {
            // However this works in your localization framework
            return frameworkLocalizer.get(locale, key)
        }
    }
    ```

Now, you can create a `JteLocalizer` whenever you render a page and pass it to the page template:

=== "Java"

    ```html linenums="1"
    @param JteLocalizer localizer

    <h1>${localizer.localize("my.title")}</h1>
    <p>${localizer.localize("my.greetings", user.getName())}</p>
    ```

=== "Kotlin"

    ```html linenums="1"
    @param localizer: JteLocalizer

    <h1>${localizer.localize("my.title")}</h1>
    <p>${localizer.localize("my.greetings", user.name)}</p>
    ```

!!! note

    Why is the `gg.jte.support.LocalizationSupport` interface even needed? It mainly helps with proper output escaping in HTML mode. Localized texts are considered safe and are not output escaped, but all user-provided parameters are! Here are some good examples [in the form of unit tests](https://github.com/casid/jte/blob/0daa676174a2ed9f1b303b927f252ce5bc9ef653/jte/src/test/java/gg/jte/TemplateEngine_HtmlOutputEscapingTest.java#L1099).

This works fine, but passing a parameter to every template might feel repetitive. If it does, you could use a [`java.lang.ThreadLocal`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ThreadLocal.html), filled with the required information before rendering a page and destroyed afterwards.

=== "Java"

    ```java linenums="1"
    public class JteContext {
        private static final ThreadLocal<JteLocalizer> context = new ThreadLocal<>();

        public static Content localize(String key) {
            context.get().localize(key);
        }

        public static Content localize(String key, Object... params) {
            context.get().localize(key, params);
        }

        static void init(JteLocalizer localizer) {
            context.set(localizer);
        }

        static void dispose() {
            context.remove();
        }
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    object JteContext {
        private val context = ThreadLocal<JteLocalizer>()

        fun localize(key: String): Content = context.get().localize(key)
        fun localize(key: String, vararg params: Any): Content = context.get().localize(key, params)

        fun init(localizer: JteLocalizer) = context.set(localizer)
        fun dispose() = context.remove()
    }
    ```

And the, when rendering the page:

=== "Java"

    ```java linenums="1"
    public void renderPage(String template, Locale locale) {
        try {
            JteContext.init(new JteLocalizer(this.frameworkLocalizer, locale));
            templateEngine.render(template);
        } finally {
            JteContext.dispose();
        }
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    fun renderPage(template: String, locale: Locale) {
        try {
            JteContext.init(JteLocalizer(this.frameworkLocalizer, locale))
            templateEngine.render(template)
        } finally {
            JteContext.dispose()
        }
    }
    ```

Localization in the template is now possible with a simple static method call:

```html
@import static my.JteContext.*

<h1>${localize("my.title")}</h1>
<p>${localize("my.greetings", user.getName())}</p>
```

Whether you prefer a parameter or a static method call is a matter of taste. The nice thing about both ways is that everything is under your control, and if you want to know what happens under the hood, that is just a click away in your IDE.

Further reading:

- Javalin example app [with localization support](https://github.com/casid/jte-javalin-tutorial)
- The localization part of the [Javalin jte tutorial](https://javalin.io/tutorials/jte)
