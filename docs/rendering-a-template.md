---
title: jte template rendering
description: How to render a jte template.
---

# Rendering a template

To render any template, an instance of `gg.jte.TemplateEngine` is required. Typically, you create it once per application (it is safe to share the engine between threads):

=== "Java"

    ```java linenums="1"
    import gg.jte.ContentType;
    import gg.jte.TemplateEngine;
    import gg.jte.CodeResolver;
    import gg.jte.resolve.DirectoryCodeResolver;
    
    // ...
    
    CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("jte")); // This is the directory where your .jte files are located.
    TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    import gg.jte.ContentType
    import gg.jte.TemplateEngine
    import gg.jte.CodeResolver
    import gg.jte.resolve.DirectoryCodeResolver
    
    // ...
    
    val codeResolver = DirectoryCodeResolver(Path.of("jte")) // This is the directory where your .jte files are located.
    val templateEngine = TemplateEngine.create(codeResolver, ContentType.Html)
    ```

With the TemplateEngine ready, templates are rendered like this:

=== "Java"

    ```java linenums="1"
    import gg.jte.TemplateOutput;
    import gg.jte.output.StringOutput;
    
    // ...
    
    TemplateOutput output = new StringOutput();
    templateEngine.render("example.jte", model, output);
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    import gg.jte.TemplateOutput
    import gg.jte.output.StringOutput
    
    // ...
    
    val output = StringOutput()
    templateEngine.render("example.jte", model, output)
    ```

Where `output` specifies where the template is rendered and `model` is the data passed to this template, which can be an instance of any class. Ideally, root templates have exactly one data parameter passed to them. If multiple parameters are required, there is a `render` method overload that takes a `Map<String, Object>`.

!!! info "`TemplateOutput` implementations"

    Besides `gg.jet.output.StringOutput`, you can use several other `gg.jte.TemplateOutput` implementations or create your own if required.

    - `gg.jte.output.StringOutput` - writes to a `String`
    - `gg.jte.output.FileOutput` - writes to the given `java.io.File`
    - `gg.jte.output.PrintWriterOutput` - writes to a `PrintWriter`, for instance, the writer provided by `HttpServletRequest`
    - `gg.jte.output.WriterOutput` - writes to a `java.io.Writer`
