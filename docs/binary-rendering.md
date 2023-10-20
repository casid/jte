---
title: jte Binary Rendering
description: How to use binary rendering for max throughput
---

# Binary rendering for max throughput

!!! info "Version note"

    Available since jte ^^**1.7.0**^^.

Most template parts are static content, and only a few parts of a template are dynamic. Encoding those static parts repeatedly on every request is wasteful if your web framework sends binary UTF-8 content to the user. jte makes it is possible to encode those static parts at compile time:

=== "Java"

    ```java linenums="1"
    templateEngine.setBinaryStaticContent(true);
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    templateEngine.binaryStaticContent = true
    ```

This generates a binary content resource for each template at compile time. Those pre-encoded UTF-8 `#!java byte[]` arrays are loaded in memory from the resource file together with the template class. This also implies that the constant pool is released from holding template strings.

To fully utilize binary templates, you must use a binary template output, like [`gg.jte.Utf8ByteOutput`](https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg.jte.runtime/gg/jte/output/Utf8ByteOutput.html). This output is heavily optimized to consume as little CPU and memory as possible when using binary templates.

!!! info

    You will only see a performance increase if you use [`binaryStaticContent`](https://www.javadoc.io/doc/gg.jte/jte-runtime/{{ latest-git-tag }}/gg.jte.runtime/gg/jte/TemplateEngine.html) with a binary output. Other outputs convert the pre-encoded `#!java byte[]` arrays back to Java Strings, defeating this optimization.

Example usage with `HttpServletResponse`:

=== "Java"

    ```java linenums="1"
    Utf8ByteOutput output = new Utf8ByteOutput();
    templateEngine.render(template, page, output);

    response.setContentLength(output.getContentLength());
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(page.getStatusCode());

    try (OutputStream os = response.getOutputStream()) {
        output.writeTo(os);
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    val output = new Utf8ByteOutput()
    templateEngine.render(template, page, output)

    response.contentLength = output.contentLength
    response.contentType = "text/html"
    response.characterEncoding = "UTF-8"
    response.status = page.statusCode

    response.outputStream.use { os ->
        output.writeTo(os)
    }
    ```

There are a few pretty cool things going on here:

- We know about the binary `Content-Length` directly after rendering, at no additional cost
- All static parts are streamed directly to the output stream without any copying/encoding overhead
- Dynamic parts are usually small - and written very efficiently to internal chunks during rendering

With binary content, you can render millions of pages per second (in case there's no DB or other external service interaction) with minimal CPU, memory and GC usage.
