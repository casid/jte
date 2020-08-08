<img align="left" alt="jte" src="jte.gif" style="max-width: 100%;" width="128">
jte is a simple, yet powerful template engine for Java. All jte templates are compiled to Java class files, meaning jte adds essentially zero overhead to your application. jte is designed to introduce as few new keywords as possible and builds upon existing Java features, so that it is very easy to reason about what a template does. The <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> offers full completion and refactoring support for Java parts as well as for jte keywords. Supports Java 11 or higher.
<br clear="left">

---

[![Build Status](https://travis-ci.org/casid/jte.svg?branch=master&v=2)](https://travis-ci.org/casid/jte)
[![Coverage Status](https://coveralls.io/repos/github/casid/jte/badge.svg?branch=master&v=2)](https://coveralls.io/github/casid/jte?branch=master)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/casid/jte/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.jusecase/jte.svg)](http://mvnrepository.com/artifact/org.jusecase/jte)

## Features
- Intuitive, easy to learn [syntax](DOCUMENTATION.md).
- Blazing fast execution, about [100k templates rendered per second](#performance) on a MacBook Pro 2015.
- Very small footprint, just <a href="https://github.com/OWASP/owasp-java-encoder/">one external dependency for output escaping</a>.
- <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> offering completion and refactoring support.
- Hot reloading of templates during development.
- Compile time checked, context-sensitive, high performance HTML output escaping (since 0.8.0).

## TLDR

jte is a lot of fun to work with! Have a look how it feels like in IntelliJ with the <a href="https://plugins.jetbrains.com/plugin/14521-jte">jte plugin</a> installed:

<img alt="jte in IntelliJ" src="jte-intellij.gif" />

## 5 minutes example

Here is a small jte template `example.jte`:
```htm
@import org.example.Page

@param Page page

<head>
    @if(page.getDescription() != null)
        <meta name="description" content="${page.getDescription()}">
    @endif
    <title>${page.getTitle()}</title>
</head>
<body>
    <h1>${page.getTitle()}</h1>
    <p>Welcome to my example page!</p>
</body>
```

So what is going on here?
- `@import` directly translates to Java imports, in this case so that `org.example.Page` is known to the template.
- `@param Page page` is the parameter that needs to be passed to this template.
- `@if`/`@endif` is an if-block. The stuff inside the braces (`page.getDescription() != null`) is plain Java code. @JSP users: Yes, there is `@elseif()` and `@else` in jte ❤️.
- `${}` writes to the underlying template output, as known from various other template engines.

To render this template, an instance of `TemplateEngine` is required. Typically you create it once per application (it is safe to share the engine between threads):
```java
CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("jte")); // This is the directory where your .jte files are located.
TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html); // Two choices: Plain or Html
```

The content type passed to the engine, determines how user output will be escaped. If you render HTML files, `Html` is highly recommended. This enables the engine to analyse HTML templates at compile time and perform context sensitive output escaping of user data, to prevent you from XSS attacks.

With the `TemplateEngine` ready, templates are rendered like this:
```java
TemplateOutput output = new StringOutput();
templateEngine.render("example.jte", page, output);
System.out.println(output);
```

> Besides `StringOutput`, there are several other `TemplateOutput` implementations you can use, or create your own if required.

`example.jte` works, but imagine you have more than one page. You would have to duplicate a lot of shared template code. Let's extract the shared code into a tag. Tags are template snippets that can be called by other templates.

> All tags must be created in a directory called `tag` in your template root directory.

Let's move stuff from our example page to `tag/page.jte`:

```htm
@import org.example.Page
@import org.jusecase.jte.Content

@param Page page
@param Content content

<head>
    @if(page.getDescription() != null)
        <meta name="description" content="${page.getDescription()}">
    @endif
    <title>${page.getTitle()}</title>
</head>
<body>
    <h1>${page.getTitle()}</h1>
    ${content}
</body>
```

The `@param Content content` is a content block that can be provided by callers of the template. `${content}` renders this content block. Let's refactor `example.jte` to use the new tag:

```htm
@import org.example.Page

@param Page page

@tag.page(page = page, content = @`
    <p>Welcome to my example page!</p>
`)
```

The shorthand to create content blocks within jte templates is an `@`followed by two backticks. For advanced stuff, you can even create Java methods that return custom `Content` implementation and call it from your template code!

Check out the [syntax documentation](DOCUMENTATION.md), for a more comprehensive introduction.

## Performance
By design, jte provides very fast output. This is a <a href="https://github.com/casid/template-benchmark/">fork of mbosecke/template-benchmark</a> with jte included, running on a MacBook Pro 2015:

![alt Template Benchmark](https://raw.githubusercontent.com/casid/template-benchmark/master/results.png)

Note that the above is with `ContentType.Plain`, so no output escaping is done. This is basically what the other engines in the benchmark are set-up with. Well, except Thymeleaf I think. Since jte 0.8.0, you will want to render HTML pages with `ContentType.Html`, so that output is automatically escaped by the engine, depending on where in the HTML data is written to. With `ContentType.Html`, jte is still extremly fast, thanks to [owasp-java-encoder](https://github.com/OWASP/owasp-java-encoder):

![alt Template Benchmark](https://raw.githubusercontent.com/casid/template-benchmark/jte-html/results.png)

## Getting started

jte is available on <a href="http://mvnrepository.com/artifact/org.jusecase/jte">Maven Central</a>:

### Maven
```xml
<dependency>
    <groupId>org.jusecase</groupId>
    <artifactId>jte</artifactId>
    <version>0.8.0</version>
</dependency>
```

### Gradle
```groovy
compile group: 'org.jusecase', name: 'jte', version: '0.8.0'
```

No further dependencies required! Check out the [syntax documentation](DOCUMENTATION.md) and start hacking :-)

## Websites rendered with jte

- <a href="https://mazebert.com">Mazebert TD (game website)</a>
