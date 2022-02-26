# jte Templates

jte is a simple, yet powerful templating engine for Java. All jte templates are compiled to Java class files, meaning jte adds essentially zero overhead to your application. All template files use the .jte file extension.

## Table of Contents

- [Rendering a template](#rendering-a-template)
- [Displaying data](#displaying-data)
- [Control structures](#control-structures)
  - [If Statements](#if-statements)
  - [Loops](#loops)
- [Comments](#comments)
- [Template Calls](#template-calls)
- [Content](#content)
- [Variables](#variables)
- [HTML Rendering](#html-rendering)
  - [Smart Attributes](#smart-attributes)
  - [Natural Comments](#natural-comments)
- [HTML Escaping](#html-escaping)
  - [HTML tag bodies](#html-tag-bodies)
  - [HTML attributes](#html-attributes)
  - [JavaScript attributes](#javascript-attributes)
- [Hot Reloading](#hot-reloading)
- [Precompiling Templates](#precompiling-templates)
  - [Using a directory on your server](#using-a-directory-on-your-server-recommended)
  - [Using the application class loader](#using-the-application-class-loader-since-120)
  - [GraalVM native-image support](#graalvm-native-image-support-since-1100)
- [Binary rendering for max throughput](#binary-rendering-for-max-throughput)
- [Localization](#localization)

## Rendering a template

To render any template, an instance of `TemplateEngine` is required. Typically you create it once per application (it is safe to share the engine between threads):

```java
CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("jte")); // This is the directory where your .jte files are located.
TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
```

With the TemplateEngine ready, templates are rendered like this:

```java
TemplateOutput output = new StringOutput();
templateEngine.render("example.jte", model, output);
```

Where `output` specifies where the template is rendered to and `model` is the data passed to this template, which can be an instance of any class. Ideally, root templates have exactly one data parameter passed to them. If multiple parameters are required, there is a `render` method overload that takes a `Map<String, Object>`.

> Besides `StringOutput`, there are several other `TemplateOutput` implementations you can use, or create your own if required.
  Currently the following implementations are available:

> - `StringOutput` - writes to a string
> - `FileOutput` - writes to the given file
> - `PrintWriterOutput` - writes to a `PrintWriter`, for instance the writer provided by `HttpServletRequest`.

A minimal template would look like this.

```xml
Hello world!
```

Rendering it with `templateEngine.render("example.jte", null, output);` will return `Hello world!`.

## Displaying data

Data passed to the template can be displayed by wrapping it in `${}`.

```xml
@import my.Model
@param Model model

Hello ${model.name}!
```

If your model class would look like this:

```java
package my;
public class Model {
    public String name = "jte";
}
```

The output of the above template would be `Hello jte!`.

## Control structures

jte provides convenient shortcuts for common Java control structures, such as conditional statements and loops. These shortcuts provide a very clean, terse way of working with  control structures, while also remaining familiar to their Java counterparts.

### If Statements

You may construct if statements using the `@if`, `@elseif`, `@else` and `@endif` keywords. These translate directly to their Java counterparts:

```xml
@if(model.entries.isEmpty())
    I have no entries!
@elseif(model.entries.size() == 1)
    I have one entry!
@else
    I have ${model.entries.size()} entries!
@endif
```

Since Java 14+ you can also use Pattern Matching for instanceof (https://openjdk.java.net/jeps/394):

```xml
@if (model instanceof SubModel subModel)
    ${subModel.getSpecial()}
@endif
```

### Loops

In addition to if statements, jte provides the `@for` and `@endfor` keywords to loop over iterable data. Again, `for` translates directly to its Java counterpart:

```xml
@for(Entry entry : model.entries)
    <li>${entry.title}</li>
@endfor

@for(var entry : model.entries)
    <li>${entry.title}</li>
@endfor

@for(int i = 0; i < 10; ++i)
    <li>i is ${i}</li>
@endfor
```

When looping, you may use the `ForSupport`class to gain information about the loop, such as whether you are in the first or last iteration through the loop.

```xml
@import gg.jte.support.ForSupport
<%-- ... --%>
@for(var entryLoop : ForSupport.of(model.entries))
    <tr class="${(entryLoop.getIndex() + 1) % 2 == 0 ? "even" : "odd"}">
        ${entryLoop.get().title}
    </tr>
@endfor
```

## Comments

jte allows you to define comments in your templates. jte comments are not included in the output of your template:

```xml
<%-- This comment will not be present in the rendered output --%>
```

## Template Calls

To share common functionality between templates, you can call other templates. All templates must be located within the jte root directory.

Here is an example template, located in `my/drawEntry.jte`

```xml
@import my.Entry
@param Entry entry
@param boolean verbose

<h2>${entry.title}</h2>
@if(verbose)
    <h3>${entry.subtitle}</h3>
@endif
```

Templates can be called like regular Java methods.

```xml
@template.my.drawEntry(model.entry1, true)
@template.my.drawEntry(model.entry2, false)
```

Subdirectories in the jte root directory act like packages in java. For instance, if the drawEntry template was located in `my/entry/drawEntry.jte`, you would call it like this:

```xml
@template.my.entry.drawEntry(model.entry1, true)
@template.my.entry.drawEntry(model.entry2, false)
```

### Named parameters

If you don't want to depend on the parameter order, you can explicitly name parameters when calling the template
(this is what the [IntelliJ plugin](https://plugins.jetbrains.com/plugin/14521-jte) suggests by default).

```xml
@template.my.entry.drawEntry(entry = model.entry1, verbose = true)
@template.my.entry.drawEntry(entry = model.entry2, verbose = false)
```

### Default parameters

You can also define default values for all parameters, so that they only need to be passed when needed.

```xml
@import my.Entry
@param Entry entry
@param boolean verbose = false

<h2>${entry.title}</h2>
@if(verbose)
    <h3>${entry.subtitle}</h3>
@endif
```

The second call could then be simplified to this:

```xml
@template.my.entry.drawEntry(entry = model.entry1, verbose = true)
@template.my.entry.drawEntry(entry = model.entry2)
```

### Varargs

The last parameter of a template can be a varargs parameter. For instance, if you created a tag to wrap elements in a list you could create something like `list.jte`:

```xml
@param String title
@param String ... elements
<h2>${title}</h2>
<ul>
@for(var element : elements)
    <li>${element}</li>
@endfor
</ul>
```

And call it like this:

```xml
@template.list(title = "Things to do", "Cook dinner", "Eat", "Netflix and Chill")
```

## Content

`gg.jte.Content` is a special parameter type to pass template code to other templates, much like lambdas in Java. They are particularly useful to share structure between different templates.

Here is an example layout with a content block:

```htm
@import org.example.Page
@import gg.jte.Content

@param Page page
@param Content content
@param Content footer = null

<head>
    @if(page.getDescription() != null)
        <meta name="description" content="${page.getDescription()}">
    @endif
    <title>${page.getTitle()}</title>
</head>
<body>
    <h1>${page.getTitle()}</h1>
    <div class="content">
        ${content}
    </div>
    @if (footer != null)
        <div class="footer">
            ${footer}
        </div>
    @endif
</body>
```

The shorthand to create content blocks within jte templates is an `@` followed by two backticks. Let's call the layout we just created and pass a a page content and footer:

```htm
@import org.example.WelcomePage
@param WelcomePage welcomePage

@template.layout.page(
    page = welcomePage,
    content = @`
        <p>Welcome, ${welcomePage.getUserName()}.</p>
    `,
    footer = @`
        <p>Thanks for visiting, come again soon!</p>
    `
)
```

## Variables

Local variables can be declared like this:

```jte
!{var innerObject = someObject.get().very().deeply().located().internal().object();}

${innerObject.a()}
${innerObject.b()}
```

Local variables translate 1:1 to Java code.

## HTML Rendering

For rendering HTML documents, `ContentType.Html` is highly recommended for [security](#html-escaping) but also for convenience.

### Smart Attributes

Expressions in HTML attributes are evaluated, so that optimal output is generated. This means attributes with a single output that evaluates to an empty string, null, or false, are not rendered. For instance:

```html
<span data-title="${null}">Info</span>
```

Will be rendered as:

```html
<span>Info</span>
```

If an HTML attribute is boolean, jte requires you to provide a boolean expression and it will omit the attribute if that expression evaluates to `false`. For example:

```html
<select id="cars">
  <option value="volvo" selected="${false}">Volvo</option>
  <option value="saab" selected="${true}">Saab</option>
  <option value="opel" selected="${false}">Opel</option>
  <option value="audi" selected="${false}">Audi</option>
</select>
```

Will render this HTML:

```html
<select id="cars">
  <option value="volvo">Volvo</option>
  <option value="saab" selected>Saab</option>
  <option value="opel">Opel</option>
  <option value="audi">Audi</option>
</select>
```

### Natural comments

All HTML, CSS and JavaScript comments are not rendered. You can use the natural comment syntax without worrying to leak too much information/data to the outside.

## HTML Escaping

Output escaping depends on the `ContentType` the engine is created with:

- With `ContentType.Plain` there is no output escaping.
- With `ContentType.Html`, the [OwaspHtmlTemplateOutput](jte-runtime/src/main/java/gg/jte/html/OwaspHtmlTemplateOutput.java) is used for context sensitive output escaping.

In `Html` mode, user content `${}` is automatically escaped, depending what part of the template it is placed into:

- HTML tag bodies
- HMTL attributes
- JavaScript attributes, e.g. `onclick`
- `<script>` blocks

### HTML tag bodies

User output in HTML tag bodies is escaped with `org.owasp.encoder.Encode#forHtmlContent` (`org.owasp.encoder.Encode#forHtml` before jte 1.5.0).

```htm
<div>${userName}</div>
```

With `userName` being `<script>alert('xss');</script>`,

the output would be `<div>&lt;script&gt;alert('xss');&lt;/script&gt;</div>`.

### HTML attributes

User output in HTML attributes is escaped with `org.owasp.encoder.Encode#forHtmlAttribute`. It ensures that all quotes are escaped, so that an attacker cannot escape the attribute.

```htm
<div data-title="Hello ${userName}"></div>
```

With `userName` being `"><script>alert('xss')</script>`,

the output would be `<div data-title="Hello &#34;>&lt;script>alert(&#39;xss&#39;)&lt;/script>"></div>`. The quote `"` is escaped with `&#34;` and the attacker cannot escape the attribute.

### JavaScript attributes

User output in HTML attributes is escaped with `org.owasp.encoder.Encode#forJavaScriptAttribute`. Those are all HTML attributes starting with `on`.

```htm
<span onclick="showName('${userName}')">Click me</span>
```

With `userName` being `'); alert('xss`,
the output would be `<span onclick="showName('\x27); alert(\x27xss')">Click me</span>`.

In case you run a [strict content security policy](https://csp.withgoogle.com/docs/strict-csp.html) without `unsafe-inline`, you could configure jte to run with `gg.jte.html.policy.PreventInlineEventHandlers`. This would cause errors at compile time, if inline event handlers are used. See [this issue](https://github.com/casid/jte/issues/20) for additional context.

```java
public class MyHtmlPolicy extends OwaspHtmlPolicy {
    public MyHtmlPolicy() {
        addPolicy(new PreventInlineEventHandlers());
    }
}
```

Then, you set it with `templateEngine.setHtmlPolicy(new MyHtmlPolicy());`.

For more examples, you may want to check out the [TemplateEngine_HtmlOutputEscapingTest](jte/src/test/java/gg/jte/TemplateEngine_HtmlOutputEscapingTest.java).

### Unsafe

In rare cases you may want to skip output escaping for a certain element. You can do this by using `$unsafe{}` instead of `${}`. For instance, to trust the user name, you would write:

```htm
<div>$unsafe{userName}</div>
```

The syntax `$unsafe{}` was picked on purpose. Whenever you use it, you're risking XSS attacks and you should carefully consider if it really is okay to trust the data you're outputting.

### Custom output escaping

It is possible to provide your own implementation of `HtmlTemplateOutput`. Maybe you want to extend the default [OwaspHtmlTemplateOutput](jte-runtime/src/main/java/gg/jte/html/OwaspHtmlTemplateOutput.java), or use your own implementation.

Before rendering, you'd simply wrap the actual `TemplateOutput` you are using:

```java
TemplateOutput output = new MySecureHtmlOutput(new StringOutput());
```

## Hot Reloading

### For a regular website

When using the `DirectoryCodeResolver`, hot reloading is supported out of the box. Before a template is resolved, the modification timestamp of the template file and all of its dependencies is checked. If there is any modification detected, the template is recompiled and the old one discarded to GC.

> It makes sense to do this on your local development environment only. When running in production, for maximum performance and security [precompiled templates](#precompiling-templates) are recommended instead.

If you clone this repository, you can launch the [SimpleWebServer](jte/src/test/java/gg/jte/benchmark/SimpleWebServer.java) example's main method. It will fire up a tiny webserver with one page to play with at http://localhost:8080.

### For a statically rendered website

In case you're using jte to pre-render static websites as HTML files, you can also listen to template file changes during development and re-render affected static files. Add the jte-watcher module to your project:

```xml
<dependency>
   <groupId>gg.jte</groupId>
   <artifactId>jte-watcher</artifactId>
   <version>${jte.version}</version>
</dependency>
```

`DirectoryWatcher::start()` starts a daemon thread listening to file changes within the jte template directory. Once file changes are detected, a listener is called with a list of changed templates.

```java
if (isDeveloperEnvironment()) {
    DirectoryWatcher watcher = new DirectoryWatcher(templateEngine, codeResolver);
    watcher.start(templates -> {
        for (String template : templates) {
            // Re-render the static HTML file
        }
    });
}
```

### Customizing generated class directory

By default generated sources and classes are outputted into the subdirectory `jte-classes` under the current directory.
It is possible to customize this directory when creating template engine. But in order to have the hot reload feature
working, a custom class directory must not be on the classpath. If it is on the classpath, generated classes
will be visible to the default class loader and once a generated class is loaded, it will not be possible to reload it
after recompiling a template, thus making the hot reload effectively non-functional.

## Precompiling Templates

To speed up startup and rendering your production server, it is possible to precompile all templates during the build. This way, the template engine can load the .class file for each template directly, without first compiling it. For security reasons you may not want to run a JDK on production - with precompiled templates this is not needed. The recommended way to setup jte, is to instantiate the engine differently, depending on when you are developing or running on a server.

```java
if (isDeveloperMachine()) {
   // create template engine with hot reloading (a bit slower)
} else {
   // create template engine with precompiled templates (fastest)
}
```

To do this, you need to create a `TemplateEngine` with the `createPrecompiled` factory method and specify where compiled template classes are located. Currently there are two options available to do this.

### Using a directory on your server (recommended)

When using this method you need to deploy the precompiled templates to your server.

```java
Path targetDirectory = Path.of("jte-classes"); // This is the directoy where compiled templates are located.

TemplateEngine templateEngine = TemplateEngine.createPrecompiled(targetDirectory, ContentType.Html);
```

#### Maven

There is a [Maven plugin](https://github.com/casid/jte-maven-compiler-plugin) you can use to precompile all templates during the Maven build. You would need to put this in build / plugins of your projects' `pom.xml`. Please note that paths specified in Java need to match those specified in Maven.

> It is recommended to create a variable like `${jte.version}` in Maven, to ensure that the jte maven plugin always matches your jte dependency.

```xml
<plugin>
    <groupId>gg.jte</groupId>
    <artifactId>jte-maven-plugin</artifactId>
    <version>${jte.version}</version>
    <configuration>
        <sourceDirectory>src/main/jte</sourceDirectory> <!-- This is the directory where your .jte files are located. -->
        <targetDirectory>jte-classes</targetDirectory> <!-- This is the directoy where compiled templates are located. -->
        <contentType>Html</contentType>
    </configuration>
    <executions>
        <execution>
            <phase>process-classes</phase>
            <goals>
                <goal>precompile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Gradle

Since 1.6.0 there is a [Gradle plugin](https://plugins.gradle.org/plugin/gg.jte.gradle) you can use to precompile all templates during the Gradle build. Please note that paths specified in Java need to match those specified in Gradle.

> Make sure that the jte gradle plugin version always matches the jte dependency version.

<details open>
<summary>Groovy</summary>

```groovy
plugins {
    id 'java'
    id 'gg.jte.gradle' version '${jte.version}'
}

dependencies {
    implementation('gg.jte:jte:${jte.version}')
}

jte {
  precompile()
}
```

</details>

<details>
<summary>Kotlin</summary>

```kotlin
plugins {
    java
    id("gg.jte.gradle") version("${jte.version}")
}

dependencies {
    implementation("gg.jte:jte:${jte.version}")
}

jte {
    precompile()
}
```

</details>

In case you would like to build a self-contained JAR, you can add this to your build.gradle:

<details open>
<summary>Groovy</summary>

```groovy
jar {
    dependsOn precompileJte
    from fileTree("jte-classes") {
        include "**/*.class"
        include "**/*.bin" // Only required if you use binary templates
    }
}
```

</details>
<details>
<summary>Kotlin</summary>

```kotlin
tasks.jar {
    dependsOn(tasks.precompileJte)
    from(fileTree("jte-classes") {
        include("**/*.class")
        include("**/*.bin") // Only required if you use binary templates
    })
}
```

</details>

And init the template engine like this for production builds:

```java
TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
```

This way the templates are loaded from the application class loader. See [this issue](https://github.com/casid/jte/issues/62) for additional information.

### Using the application class loader (since 1.2.0)

When using this method the precompiled templates are bundled within your application jar file. The plugin generates `*.java` files for all jte templates during Maven's `GENERATE_SOURCES` phase. Compilation of the templates is left to the Maven Compiler plugin.

While this provides you with a nice self-containing jar, it has some limitations:

- Once the sources are generated, IntelliJ will put them on the classpath and hot reloading will not work unless the generated sources are deleted.
- Some plugin settings are not supported, like configuring a custom HtmlPolicy class (this is because project classes are not yet compiled at the `GENERATE_SOURCES` phase).

```java
TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
```

#### Maven

There is a [Maven plugin](https://github.com/casid/jte-maven-compiler-plugin) you can use to generate all templates during the Maven build. You would need to put this in build / plugins of your projects' `pom.xml`. Please note that paths specified in Java need to match those specified in Maven.

> It is recommended to create a variable like `${jte.version}` in Maven, to ensure that the jte maven plugin always matches your jte dependency.

```xml
<plugin>
    <groupId>gg.jte</groupId>
    <artifactId>jte-maven-plugin</artifactId>
    <version>${jte.version}</version>
    <configuration>
        <sourceDirectory>${basedir}/src/main/jte</sourceDirectory> <!-- This is the directory where your .jte files are located. -->
        <contentType>Html</contentType>
    </configuration>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Gradle

Since 1.6.0 there is a <a href="https://plugins.gradle.org/plugin/gg.jte.gradle">Gradle plugin</a> you can use to generate all templates during the Gradle build. Please note that paths specified in Java need to match those specified in Gradle. 

> Make sure that the jte gradle plugin version always matches the jte dependency version.

<details open>
<summary>Groovy</summary>

```groovy
plugins {
    id 'java'
    id 'gg.jte.gradle' version '${jte.version}'
}

dependencies {
    implementation('gg.jte:jte:${jte.version}')
}

jte {
    generate()
}
```

</details>

<details>
<summary>Kotlin</summary>

```kotlin
plugins {
    java
    id("gg.jte.gradle") version("${jte.version}")
}

dependencies {
    implementation("gg.jte:jte:${jte.version}")
}

jte {
    generate()
}
```

</details>

### GraalVM native-image support (since 1.10.0)
An application jar with generated classes can be built into a native binary using [GraalVM native-image](https://www.graalvm.org/reference-manual/native-image/). To support this, jte can generate the necessary configuration files to tell native-image about classes loaded by reflection.

To use this feature, set `generateNativeImageResources = true` in your Gradle `jte` block. (Docs for Maven TBD)

There's an example [gradle test project](https://github.com/casid/jte/blob/master/jte-runtime-cp-test-gradle-convention/build.gradle) using native-image compilation.

## Binary rendering for max throughput

Most template parts are static content and only few parts of a template are dynamic. It is wasteful to convert those static parts over and over on every request, if your web-framework sends binary UTF-8 content to the user. Since jte 1.7.0 it is possible to encode those static parts at compile time:

```java
templateEngine.setBinaryStaticContent(true);
```

This generates a binary content resource for each template at compile time. Those pre-encoded UTF-8 byte[] arrays are loaded in memory from the resource file together with the template class. This also implies, that the constant pool is released of holding template strings.

To fully utilize binary templates you need to use a binary template output, like `Utf8ByteOutput`. This output is heavily optimized to consume as little CPU and memory as possible when using binary templates.

> Hint: You will only see a performance increase if you use binaryStaticContent in tandem with a binary output. Other outputs convert the pre-encoded byte[] arrays back to Java Strings and defeat this optimization.

Example usage with `HttpServletResponse`:

```java
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

There are a few pretty cool things going on here:

- We know about the binary content-length directly after rendering, at no additional cost
- All static parts are streamed directly to the output stream, without any copying / encoding overhead
- Dynamic parts are usually small - and written very efficiently to internal chunks during rendering

With binary content you will be able to render millions of pages per second (in case there's no DB or other external service interaction, heh) - with very little CPU, memory and GC usage.

## Localization

jte has no built in keywords for localization. Instead, it provides a flexible interface so that you can easily use the same localization mechanism you're used to in your project! This has several advantages:

- no need to learn yet another way to localize things
- no need to reverse engineer another opinionated localization implementation
- your users receive the same localization through jte as they do from the rest of your application


Let's implement `gg.jte.support.LocalizationSupport`. There's only on method to implement:

```java
public static class JteLocalizer implements gg.jte.support.LocalizationSupport {
    
    private final OtherFrameworkLocalizer frameworkLocalizer;
    private final Locale locale;
    
    public JteLocalizer(OtherFrameworkLocalizer frameworkLocalizer, Locale locale) {
        this.frameworkLocalizer = frameworkLocalizer;
        this.locale = locale;
    }
    
    @Override
    public String lookup(String key) {
        // However this works in your localization framework
        return frameworkLocalizer.get(locale, key);
    }
}
```

Now, you can create a JteLocalizer whenever you render a page and pass it to the page template:

```html
@param JteLocalizer localizer

<h1>${localizer.localize("my.title")}</h1>
<p>${localizer.localize("my.greetings", user.getName())}</p>
```

> Why is the `gg.jte.support.LocalizationSupport` interface even needed? It mainly helps with proper output escaping in HTML mode. Localized texts are considered safe and are not output escaped, but all user provided parameters are! Here are some good examples [in form of unit tests](https://github.com/casid/jte/blob/0daa676174a2ed9f1b303b927f252ce5bc9ef653/jte/src/test/java/gg/jte/TemplateEngine_HtmlOutputEscapingTest.java#L1099).

This works fine, but passing a parameter to every template might feel a little repetitive. In case it does, you could use a `ThreadLocal`, that is filled with the required information before rendering a page and destroyed afterwards.

```java
public class JteContext {
    private static final ThreadLocal<JteLocalizer> context = new ThreadLocal<>();

    public static Content localize(String key) {
        context.get().localize(key);
    }
    
    public static Content localize(String key, Object... params) {
        context.get().localize(key, params);
    }
    
    static void init(JteLocalizer localizer) {
        context.set(localizer);
    }
    
    static void dispose() {
        context.remove();
    }
}
```

```java
public void renderPage(String template, Locale locale) {
    try {
        JteContext.init(new JteLocalizer(this.frameworkLocalizer, locale));
        templateEngine.render(template);
    } finally {
        JteContext.dispose();
    }
}
```

Localization in the template is now possible with a simple static method call:

```html
@import static my.JteContext.*

<h1>${localize("my.title")}</h1>
<p>${localize("my.greetings", user.getName())}</p>
```

It really is a matter of taste, if you prefer a parameter or a static method call. The nice thing of both ways is, that everything is under your control and if you want to know what happens under the hood, that is just a click away in your IDE.

Further reading:
- Javalin example app [with localization support](https://github.com/casid/jte-javalin-tutorial)
- The localization part of the [Javalin jte tutorial](https://javalin.io/tutorials/jte)
