---
title: jte Spring Boot 4 Starter
description: A Spring Boot 4 Starter to swiftly integrate jte
---

# Spring Boot Starter

This [starter](https://docs.spring.io/spring-boot/4.0/reference/using/build-systems.html#using.build-systems.starters) is compatible with Spring Boot 4.x!

## Add required dependencies

=== "Maven"

    ```xml linenums="1"
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte-spring-boot-starter-4</artifactId>
        <version>{{ latest-git-tag }}</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy linenums="1"
    implementation "gg.jte:jte-spring-boot-starter-4:{{ latest-git-tag }}"
    ```

## Usage

The starter configures a `org.springframework.web.servlet.ViewResolver` and a jte Template engine. Now you can return a string, pointing to template file name and the resolver will take care to instantiate the view and render the template.

!!! info

    By default, the templates are expected at `src/main/jte`.

```html linenums="1"
@import com.example.demo.DemoModel

@param DemoModel model

Hello ${model.text}!
```

```java linenums="1"
@GetMapping("/") 
public String view(Model model, HttpServletResponse response) {
    model.addAttribute("model", new DemoModel("Hello World"));
    return "demo";
}
```

You can use it with Spring WebMVC as well as with Spring WebFlux.

## Versioned static resources

For Spring MVC servlet applications, the starter exposes a `gg.jte.springframework.ResourceUrls`
helper as the `resources` model attribute during each view render. It uses Spring's
configured resource resolver chain, including content and fixed version strategies.
No controller advice or provider injection is needed.

Enable Spring Boot's content version strategy, for example:

```properties linenums="1"
spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**
```

Place your resources in a Spring Boot static resource location, such as
`src/main/resources/static/css/bundle.css`, and explicitly resolve their URLs:

```html linenums="1"
@param gg.jte.springframework.ResourceUrls resources

<link href="${resources.url("/css/bundle.css")}" rel="stylesheet" />
<script type="module" src="${resources.url("/js/bundle.js")}"></script>
```

With content versioning enabled, Spring resolves these paths to URLs such as
`/css/bundle-<content-hash>.css`. Literal URLs such as `href="/css/bundle.css"`
remain literal. `ResourceUrlEncodingFilter` is unnecessary for this helper because
it resolves URLs directly through Spring MVC's `ResourceUrlProvider`.

Use paths starting with `/`, relative to the MVC resource mappings. The helper adds
the current context path and, for a DispatcherServlet mapped to a prefix such as
`/mvc/*`, the servlet prefix to resolved URLs. An input already containing the full
current context and servlet prefix is also supported without adding it twice.
Default `/` and prefix servlet mappings are supported; extension servlet mappings
are outside this helper's scope.

Query strings and fragments are preserved. External URLs, protocol-relative URLs,
data URLs, fragment-only links and relative paths are returned unchanged. Missing
resources, unmatched mappings and requests without an MVC resource provider return
the original URL. With versioning disabled, resolved URLs retain their ordinary
resource path, with the current context and servlet prefix when applicable.

The helper returns strings, so jte's normal HTML attribute escaping still applies.
It works with both development compilation and precompiled templates. Pass the
parameter explicitly to layouts or nested templates that need it:

```html linenums="1"
@param gg.jte.springframework.ResourceUrls resources

@template.layout(resources = resources)
```

The receiving `layout.jte` must also declare the `ResourceUrls resources` parameter.
An existing `resources` model attribute, including a null value, takes precedence;
the starter does not replace it. The helper is added to a render-local copy of the
model and stores no current request in a shared view or singleton. It is available
only through the Spring MVC servlet view integration in `jte-spring-boot-starter-4`,
not through direct `TemplateEngine.render()` calls or WebFlux views.

## Configuration 

By default, the template files are expected in `src/main/jte`, You can also set the templateSuffix of your jte templates

````properties linenums="1"
gg.jte.templateLocation=src/main/jte
gg.jte.templateSuffix=.jte
````

### Development 

If you set `developmentMode = true` the jte [file watcher](hot-reloading.md) will watch for changes in templates and recompile them.

!!! warning

    This only works with a JDK!

```properties linenums="1"
gg.jte.developmentMode=true
```

Runtime-compiled templates are written to `jte-classes` relative to the application's working directory by default.
In the Spring Boot 4 starters, you can set `gg.jte.class-directory` to an absolute or relative path:

```properties linenums="1"
gg.jte.class-directory=target/jte-classes
```

This setting applies to both `jte-spring-boot-starter-4` and `jte-spring-boot-starter-4-core`, including Spring WebMVC and WebFlux. It does not configure the Maven or Gradle plugin's precompiled output.

In development mode, you can also set `trimControlStructures = true` which will trim control structures.

```properties linenums="1"
gg.jte.trimControlStructures=true
```

### Production

To use [precompiled Templates](pre-compiling.md) in production, for use with a JRE environment, you need to configure the Maven/Gradle Plugin to precompile your templates:

````properties linenums="1"
gg.jte.usePrecompiledTemplates=true
````

The `jte-spring-boot-starter-4` package includes auto-configuration for both a `org.springframework.web.servlet.ViewResolver` and the jte `TemplateEngine`.
If you only need the `TemplateEngine`, use `jte-spring-boot-starter-4-core` instead, which provides the same `TemplateEngine` auto-configuration without registering a `ViewResolver`.

=== "Maven"

    ```xml linenums="1"
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte-spring-boot-starter-4-core</artifactId>
        <version>{{ latest-git-tag }}</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy linenums="1"
    implementation "gg.jte:jte-spring-boot-starter-4-core:{{ latest-git-tag }}"
    ```
