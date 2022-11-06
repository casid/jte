<img align="left" alt="jte" src="jte.svg" width="128">jte is a secure and lightweight template engine for Java and Kotlin. All jte templates are compiled to Java class files, meaning jte adds essentially zero overhead to your application. jte is designed to introduce as few new keywords as possible and builds upon existing Java features, so that it is very easy to reason about what a template does. The <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> offers full completion and refactoring support for Java parts as well as for jte keywords. Supports Java 8 or higher.
<br clear="left">

[![Build Status](https://github.com/casid/jte/workflows/Test%20all%20JDKs%20on%20all%20OSes/badge.svg)](https://github.com/casid/jte/actions)
[![Coverage Status](https://codecov.io/gh/casid/jte/branch/main/graph/badge.svg)](https://codecov.io/gh/casid/jte)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/casid/jte/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/gg.jte/jte.svg)](http://mvnrepository.com/artifact/gg.jte/jte)

## Features
- Intuitive and easy syntax, you'll rarely need to check the [documentation](DOCUMENTATION.md)
- Write plain Java or Kotlin for expressions
- Context-sensitive [HTML escaping](https://github.com/casid/jte/blob/master/DOCUMENTATION.md#html-escaping) at compile time
- <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> with completion and refactoring support
- Hot reloading of templates during development
- Blazing fast execution ([see benchmarks](#performance))

## TLDR

jte gives you the same productive, type safe experience you're used to from writing Java or Kotlin. This is IntelliJ with the <a href="https://plugins.jetbrains.com/plugin/14521-jte">jte plugin</a> installed:

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
- `@if`/`@endif` is an if-block. The stuff inside the braces (`page.getDescription() != null`) is plain Java code.
- `${}` writes to the underlying template output, as known from various other template engines.

To render this template, an instance of `TemplateEngine` is required. Typically you create it once per application (it is safe to share the engine between threads):
```java
CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("jte")); // This is the directory where your .jte files are located.
TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html); // Two choices: Plain or Html
```

The content type passed to the engine determines how user output will be escaped. If you render HTML files, `Html` is highly recommended. This enables the engine to analyze HTML templates at compile time and perform context sensitive output escaping of user data, to prevent you from XSS attacks.

With the `TemplateEngine` ready, templates are rendered like this:
```java
TemplateOutput output = new StringOutput();
templateEngine.render("example.jte", page, output);
System.out.println(output);
```

> Besides `StringOutput`, there are several other `TemplateOutput` implementations you can use, or create your own if required.

If you had more than one page like `example.jte`, you would have to duplicate a lot of shared template code. Let's extract the shared code into another template, so that it can be reused.

Let's move stuff from our example page to `layout.jte`:

```htm
@import org.example.Page
@import gg.jte.Content

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

The `@param Content content` is a content block that can be provided by callers of the template. `${content}` renders this content block. Let's refactor `example.jte` to use the new template:

```htm
@import org.example.Page

@param Page page

@template.layout(page = page, content = @`
    <p>Welcome to my example page!</p>
`)
```

The shorthand to create content blocks within jte templates is an `@` followed by two backticks. For advanced stuff, you can even create Java methods that return custom `Content` implementation and call it from your template code!

Check out the [syntax documentation](DOCUMENTATION.md), for a more comprehensive introduction.

## Performance
By design, jte provides very fast output. This is a <a href="https://github.com/casid/template-benchmark/">fork of mbosecke/template-benchmark</a> with jte included, running on AMD Ryzen 5950x (single thread):

![alt Template Benchmark](https://raw.githubusercontent.com/casid/template-benchmark/master/results.png)

### High concurrency
This is the same benchmark as above, but the amount of threads was set to `@Threads(16)`, to fully utilize all cores. jte has pretty much zero serialization bottlenecks and runs  very concurrent on servers with a lot of CPU cores:
![alt Template Benchmark](https://raw.githubusercontent.com/casid/template-benchmark/ryzen-5950x/results.png)

## Getting started

jte is available on <a href="http://mvnrepository.com/artifact/gg.jte/jte">Maven Central</a>:

### Maven
```xml
<dependency>
    <groupId>gg.jte</groupId>
    <artifactId>jte</artifactId>
    <version>2.2.4</version>
</dependency>
```

### Gradle
```groovy
implementation group: 'gg.jte', name: 'jte', version: '2.2.4'
```

No further dependencies required! Check out the [syntax documentation](DOCUMENTATION.md) and have fun with jte.

## Framework integration

- [Javalin](https://javalin.io/tutorials/jte)
- [Eclipse Vert.x](https://github.com/vert-x3/vertx-web/tree/master/vertx-template-engines/vertx-web-templ-jte)
- [Spring Boot](https://github.com/casid/jte-spring-boot-demo)
- [Spring Web MVC](https://github.com/izogfif/demo-spring-jte)
- [Ktor](https://ktor.io/docs/jte.html)
- [Micronaut](https://micronaut-projects.github.io/micronaut-views/latest/guide/#jte)
- [Quarkus](https://github.com/renannprado/quarkus-jte-extension/)
- [Severell](https://github.com/severell/severell-jte-plugin)

## Websites rendered with jte

- <a href="https://jte.gg">The jte website</a> (<a href="https://github.com/casid/jte-website">Source</a>)
- <a href="https://mazebert.com">Mazebert TD (game website)</a>
- <a href="https://github.com/casid/jte-javalin-tutorial">Javalin website example with login and multiple languages</a>
- <a href="https://www.mitchdennett.com/">Mitch Dennett's Blog</a>
- <a href="https://flowcrypt.com/docs/business/enterprise-admin-panel.html">FlowCrypt Admin Panel</a>
