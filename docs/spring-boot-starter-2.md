---
title: jte Spring Boot 2 Starter
description: A Spring Boot 2 Starter to swiflty integrate jte
---

# Spring Boot 2 Starter

This [starter](https://docs.spring.io/spring-boot/docs/2.7.x/reference/htmlsingle/#using.build-systems.starters) is compatible with Spring Boot 2.x!

## Add required dependencies

=== "Maven"

    ```xml linenums="1"
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte-spring-boot-starter-2</artifactId>
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
    implementation "gg.jte:jte-spring-boot-starter-2:{{ POM_VERSION }}"
    implementation "gg.jte:jte:{{ POM_VERSION }}"
    ```

## Usage

The starter configures a `org.springframework.web.servlet.ViewResolver` and a jte Template engine. Now you can return a string, pointing to template file name  and the resolver will take care to instantiate the view and render the template.

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

By default, the template files are expected in `src/main/jte`.  If any active profile is named `prod` the template engine will be configured
to use [precompiled templates](pre-compiling.md) otherwise the jte [file watcher](hot-reloading.md) will watch for changes in templates and recompile them.

Both options can be changed via

```properties linenums="1"
gg.jte.productionProfileName=k8s
gg.jte.templateLocation=src/main/templates
```

