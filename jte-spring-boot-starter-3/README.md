# jte Spring Boot Starter

This starter is compatible with spring boot 3.x!

## Add required dependencies

````xml
<dependency>
    <groupId>gg.jte</groupId>
    <artifactId>jte-spring-boot-starter-3</artifactId>
    <version>INSERT_LATEST_VERSION</version>
</dependency>
<dependency>
    <groupId>gg.jte</groupId>
    <artifactId>jte</artifactId>
    <version>INSERT_LATEST_VERSION</version>
</dependency>
````

````groovy
implementation "gg.jte:jte-spring-boot-starter-3:INSERT_LATEST_VERSION"
implementation "gg.jte:jte:INSERT_LATEST_VERSION"
````

## Usage

The starter configures a ViewResolver and a jte Template engine.

Now you can return a string, pointing to template file name 
and the resolver will take care to instantiate the view and render the template.

By default, the templates are expected at `src/main/jte`.

````
@import com.example.demo.DemoModel

@param DemoModel model

Hello ${model.text}!
````

````java
@GetMapping("/") 
public String view(Model model, HttpServletResponse response) {
    model.addAttribute("model", new DemoModel("Hello World"));
    return "demo";
}
````

You can use it with Spring WebMVC as well as with Spring WebFlux.

## Configuration 

By default, the template files are expected in `src/main/jte`.

If any active profile is named prod the template engine will be configured
to use precompiled templates otherwise the jte file watcher will watch for changes in templates and recompile them.

Both options can be changed via

````
gg.jte.productionProfileName=k8s
gg.jte.templateLocation=src/main/templates
````

