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

By default, the template files are expected in `src/main/jte`,
You can also set the templateSuffix of your jte templates

````properties
gg.jte.templateLocation=src/main/jte
gg.jte.templateSuffix=.jte
````


### Development 

If you set development = true the jte file watcher will watch for changes in templates and recompile them.

! This only works with a JDK !
````properties
gg.jte.developmentMode=true
````


### Production

To use precompiled Templates in Production for use with a JRE environment you need to configure the Maven/Gradle Plugin to precompile your templates:
https://github.com/casid/jte/blob/main/DOCUMENTATION.md#precompiling-templates

````properties
gg.jte.usePrecompiledTemplates=true
````

