# jte (Java Template Engine)
[![Build Status](https://travis-ci.org/casid/jte.svg?branch=master)](https://travis-ci.org/casid/jte)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://raw.githubusercontent.com/casid/jte/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.jusecase/inject.svg)](http://mvnrepository.com/artifact/org.jusecase/jte)

jte is a simple, yet powerful template engine for Java. jte is designed to introduce as few new keywords as possible and build upon existing Java features, so that it is very easy to reason about what a template does. The <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> offers full completion and refactoring support for Java parts as well as for jte keywords. Supports Java 11 or higher.

## Features
- Intuitive, easy to learn [syntax](DOCUMENTATION.md).
- Blazing fast execution, about <a href="https://github.com/casid/template-benchmark/">100k templates rendered per second</a> on a MacBook Pro 2015.
- Small footprint, no external dependencies. The jar is ~60 KB.
- <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> offering completion and refactoring support.
- Hot reloading of templates during development.

## 5 minutes example

To get a quick impression how jte behaves and feels, here's a small page template `example.jte`:
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

To render this template, you first need an instance of `TemplateEngine`. Typically you create it once per application (it is safe to share the engine between threads):
```java
CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("jte")); // This is the directory where your .jte files are located.
TemplateEngine templateEngine = new TemplateEngine(codeResolver);
```

Now we can render the template like this:
```java
TemplateOutput output = new StringOutput();
templateEngine.render("example.jte", page, output);
System.out.println(output.toString());
```

> Besides `StringOutput`, there are several other `TemplateOutput` implementations you can use, or create your own if required.

In most cases you have multiple pages that share a lot of common html. This is where jte layouts shine! 

> All layouts must be created in a directory called `layout` in your template root directory.

Let's move stuff from our example page to `layout/page.jte`:

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
    @render(content)
</body>
```

`@render` is what makes layouts very powerful. The `@render(content)` is a placeholder that can be provided by callers of the template. Let's refactor `example.jte` to use the new layout:

```htm
@import org.example.Page

@param Page page

@layout.page(page)
    @define(content)
        <p>Welcome to my example page!</p>
    @enddefine
@endlayout
```

Check out the [syntax documentation](DOCUMENTATION.md), for a more comprehensive introduction.

## Performance
By design, jte provides very fast output. This is a <a href="https://github.com/casid/template-benchmark/">fork of mbosecke/template-benchmark</a> with jte included, running on a MacBook Pro 2015:

![alt text](https://raw.githubusercontent.com/casid/template-benchmark/master/results.png)

## Getting started

jte is available on <a href="http://mvnrepository.com/artifact/org.jusecase/jte">maven central</a>:

```xml
<dependency>
    <groupId>org.jusecase</groupId>
    <artifactId>jte</artifactId>
    <version>0.1.1</version>
</dependency>
```


