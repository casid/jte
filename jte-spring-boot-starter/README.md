# jte Spring Boot Starter

## Add required dependency

````xml
<dependency>
    <groupId>gg.jte</groupId>
    <artifactId>jte-spring-boot-starter</artifactId>
    <version>INSERT_LATEST_VERSION</version>
</dependency>
````

````groovy
implementation "gg.jte:jte-spring-boot-starter:INSERT_LATEST_VERSION"
````

## Usage

The starter configures a ViewResolver and a jte Template engine.

Now you can return a string, pointing to template file name and the resolver will take care to instantiete the view and render the template.

````java
@GetMapping("/") 
public String view(Model model, HttpServletResponse response) {
    model.addAttribute("text", "Hello World");
    return "demo";
}
````

## Configuration 

By default, the template files are expected in `src/main/jte`.

If any active profile is named prod the template engine will be configured
to use precompiled templates otherwise the jte file watcher will watch for changes in templates and recompile them.

Both options can be changed via

````
gg.jte.productionProfileName=k8s
gg.jte.templateLocation=src/main/templates
````

