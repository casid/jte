---
title: jte Spring Boot 3 Starter
description: A Spring Boot 3 Starter to swiflty integrate jte
---

# Spring Boot Starter

This [starter](https://docs.spring.io/spring-boot/docs/3.2.x/reference/htmlsingle/#using.build-systems.starters) is compatible with Spring Boot 3.x!

## Add required dependencies

=== "Maven"

    ```xml linenums="1"
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte-spring-boot-starter-3</artifactId>
        <version>{{ POM_VERSION }}</version>
    </dependency>
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte</artifactId>
        <version>{{ POM_VERSION }}</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy linenums="1"
    implementation "gg.jte:jte-spring-boot-starter-3:{{ POM_VERSION }}"
    implementation "gg.jte:jte:{{ POM_VERSION }}"
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

### Production

To use [precompiled Templates](pre-compiling.md) in production, for use with a JRE environment, you need to configure the Maven/Gradle Plugin to precompile your templates:

````properties linenums="1"
gg.jte.usePrecompiledTemplates=true
````

