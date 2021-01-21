# jte Templates

jte is a simple, yet powerful templating engine for Java. All jte templates are compiled to Java class files, meaning jte adds essentially zero overhead to your application. All template files use the .jte file extension.

## Table of Contents
- [Rendering a template](#rendering-a-template)
- [Displaying data](#displaying-data)
- [Control structures](#control-structures)
  - [If Statements](#if-statements)
  - [Loops](#loops)
- [Comments](#comments)
- [Tags](#tags)
- [Content](#content)
- [Variables](#variables)
- [Hot Reloading](#hot-reloading)
- [Precompiling Templates](#precompiling-templates)
- [HTML escaping](#html-escaping)
  - [HTML tag bodies](#html-tag-bodies)
  - [HTML attributes](#html-attributes)
  - [JavaScript attributes](#javascript-attributes)  

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

> Besides `StringOutput`, there are several other `TemplateOutput` implementations you can use, or create your own if required. Currently the following implementations are available:
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

## Tags

To share common functionality between templates, you can extract it into tags. All tags must be located within the `tag` directory in the jte root directory.

Here is an example tag, located in `tag/drawEntry.jte`

```xml
@import my.Entry
@param Entry entry
@param boolean verbose

<h2>${entry.title}</h2>
@if(verbose)
    <h3>${entry.subtitle}</h3>
@endif
```

Tags can be called like regular Java methods.

```xml
@tag.drawEntry(model.entry1, true)
@tag.drawEntry(model.entry2, false)
```

Subdirectories in the `tag` directory act like packages in java. For instance, if the drawEntry tag was located in `tag/entry/drawEntry.jte`, you would call it like this:

```xml
@tag.entry.drawEntry(model.entry1, true)
@tag.entry.drawEntry(model.entry2, false)
```

### Named parameters

If you don't want to depend on the parameter order, you can explicitly name parameters when calling the template (this is what the <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> suggests by default).

```xml
@tag.entry.drawEntry(entry = model.entry1, verbose = true)
@tag.entry.drawEntry(entry = model.entry2, verbose = false)
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
@tag.entry.drawEntry(entry = model.entry1, verbose = true)
@tag.entry.drawEntry(entry = model.entry2)
```

### Varargs

The last parameter of a tag can be a varargs parameter. For instance, if you created a tag to wrap elements in a list you could create something like `tag/list.jte`:

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
@tag.list(title = "Things to do", "Cook dinner", "Eat", "Netflix and Chill")
```

## Content

`gg.jte.Content` is a special parameter type to pass template code to tags, much like lambdas in Java. They are particularly useful to share structure between different templates.

Here is an example tag with a content block:

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

The shorthand to create content blocks within jte templates is an `@`followed by two backticks. Let's call the tag we just created and pass a a page content and footer:

```htm
@import org.example.WelcomePage
@param WelcomePage welcomePage

@tag.page(
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

```
!{var innerObject = someObject.get().very().deeply().located().internal().object();}

${innerObject.a()}
${innerObject.b()}
```

Local variables translate 1:1 to Java code.

## Hot Reloading

### For a regular website

When using the `DirectoryCodeResolver`, hot reloading is supported out of the box. Before a template is resolved, the modification timestamp of the template file and all of its dependencies is checked. If there is any modification detected, the template is recompiled and the old one discarded to GC. 

> It makes sense to do this on your local development environment only. When running in production, for maximum performance and security [precompiled templates](#precompiling-templates) are recommended instead.

If you clone this repository, you can launch the [SimpleWebServer](jte/src/test/java/gg/jte/benchmark/SimpleWebServer.java) example's main method. It will fire up a tiny webserver with one page to play with at http://localhost:8080.

### For a statically rendered website

In case you're using jte to pre-render static websites as HTML files, you can also listen to template file changes during development and re-render affected static files:
 
`DirectoryCodeResolver::startTemplateFilesListener` starts a daemon thread listening to file changes within the jte template directory. Once file changes are detected, a listener is called with a list of changed templates.

```java
if (isDeveloperEnvironment()) {
    codeResolver.startTemplateFilesListener(templateEngine, templates -> {
        for (String template : templates) {
            // Re-render the static HTML file
        }
    });
}
```

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

There is a <a href="https://github.com/casid/jte-maven-compiler-plugin">Maven plugin</a> you can use to precompile all templates during the Maven build. You would need to put this in build / plugins of your projects' `pom.xml`. Please note that paths specified in Java need to match those specified in Maven. 

> It is recommended to create a variable like `${jte.version}` in Maven, to ensure that the jte compiler plugin always matches your jte dependency.

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

### Using the application class loader (since 1.2.0)

When using this method the precompiled templates are bundled within your application jar file.

```java
TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
```

There is a <a href="https://github.com/casid/jte-maven-compiler-plugin">Maven plugin</a> you can use to precompile all templates during the Maven build. You would need to put this in build / plugins of your projects' `pom.xml`. Please note that paths specified in Java need to match those specified in Maven. 

> It is recommended to create a variable like `${jte.version}` in Maven, to ensure that the jte compiler plugin always matches your jte dependency.

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

## HTML escaping

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

### 

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
