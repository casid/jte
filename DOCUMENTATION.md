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
- [Layouts](#layouts)
- [Hot Reloading](#hot-reloading)
- [Precompiling Templates](#precompiling-templates)
- [Output Escaping](#output-escaping)

## Rendering a template

To render any template, an instance of `TemplateEngine` is required. Typically you create it once per application (it is safe to share the engine between threads):

```java
CodeResolver codeResolver = new DirectoryCodeResolver(Path.of("jte")); // This is the directory where your .jte files are located.
TemplateEngine templateEngine = new TemplateEngine(codeResolver);
```

With the TemplateEngine ready, templates are rendered like this:

```java
TemplateOutput output = new StringOutput();
templateEngine.render("example.jte", model, output);
```

Where `output` specifies where the template is rendered to and `model` is the data passed to this template, which can be an instance of any class. Root templates must have exactly one data parameter passed to them.

> Besides `StringOutput`, there are several other `TemplateOutput` implementations you can use, or create your own if required. Currently the following implementations are available:
> - `StringOutput` - writes to a string
> - `FileOutput` - writes to the given file
> - `PrintWriterOutput` - writes to a `PrintWriter`, for instance the writer provided by `HttpServletRequest`.

A minimal template would look like this.

```xml
@import my.Model
@param Model model

Hello world!
```

Behind the scenes, this will be translated to the following Java class:

```java
package org.jusecase.jte.generated.test;
import my.Model;
public final class JteexampleGenerated implements org.jusecase.jte.internal.Template<Model> {
    public void render(Model model, org.jusecase.jte.TemplateOutput output) {
        output.writeStaticContent("Hello world!");
    }
}
```

As you can see, all the heavy lifting is eventually done by Java, in a very transparent way.


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

The output of the above template would be `Hello jte!`. Behind the scenes, the following Java would be generated from this template:

```java
package org.jusecase.jte.generated.test;
import my.Model;
public final class JtetemplateGenerated implements org.jusecase.jte.internal.Template<Model> {
    public void render(Model model, org.jusecase.jte.TemplateOutput output) {
        output.writeStaticContent("Hello ");
        output.writeUserContent(model.name);
        output.writeStaticContent("!");
    }
}
```

Note the difference of safe und unsafe content. All static parts of a template are treated as safe. All dynamic parts are treated as unsafe to avoid cross-site scripting (XSS) attacks.

> **Caution!** All core `TemplateOutput` implementations make no difference between handling safe and unsafe content. Output escaping comes in many flavours and jte doesn't want to force an opinion on you. See the section [Output Escaping](#output-escaping) for more details.

## Control structures

jte provides convenient shortcuts for common Java control structures, such as conditional statements and loops. These shortcuts provide a very clean, terse way of working with Java control structures, while also remaining familiar to their Java counterparts.

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
@import org.jusecase.jte.support.ForSupport
<%-- ... --%>
@for(var entryLoop : ForSupport.of(model.entries))
    <tr class="${(entryLoop.getIndex() + 1) % 2 == 0 ? "even" : "odd"}">
        ${entryLoop.get().title}
    </tr>
@endfor
```

## Comments

jte allows you to define comments in your templates. Unlike HTML comments, jte comments are not included in the output of your template:

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

If you don't want to depend on the parameter order, you can explicitly name parameters when calling the template (this is what the <a href="https://plugins.jetbrains.com/plugin/14521-jte">IntelliJ plugin</a> suggests by default).

```xml
@tag.entry.drawEntry(entry = model.entry1, verbose = true)
@tag.entry.drawEntry(entry = model.entry2, verbose = false)
```

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

## Layouts

Layouts provide all features of tags, plus render sections. They are particularly useful to share structure between different templates. All layouts must be located within the `layout` directory in the jte root directory.

Here is an example layout, located in `layout/page.jte`

```htm
@import org.example.Page

@param Page page

<head>
    @if(page.getDescription() != null)
        <meta name="description" content="${page.getDescription()}">
    @endif
    <title>${page.getTitle()}</title>
</head>
<body>
    <h1>${page.getTitle()}</h1>
    <div class="content">
        @render(content)
    </div>
    <div class="footer">
        @render(footer)
    </div>
</body>
```

Layouts are called like tags, but you can define what content should be put in the `@render` slots declared in the layout. Here is an example using the above page layout:

```htm
@import org.example.WelcomePage
@param WelcomePage welcomePage

@layout.page(welcomePage)
    @define(content)
        <p>Welcome, ${welcomePage.getUserName()}.</p>
    @enddefine
    @define(footer)
        <p>Thanks for visiting, come again soon!</p>
    @enddefine
@endlayout
```

## Hot Reloading

When using the `DirectoryCodeResolver`, hot reloading can be activated. It makes sense to do this on dev environments only. `enableHotReload` starts a daemon thread listening to file changes within the jte template directory. Once file changes are detected, all related templates are be invalidated and a listener is called with a list of invalidated templates.

```java
if (isDevEnvironment()) {
    codeResolver.enableHotReload(templateEngine, templates -> {
        for (String template : templates) {
            try {
                templateEngine.prepareForRendering(template);
            } catch (Exception e) {
                logger.error("Failed to recompile " + template, e);
            }
        }
    });
}
```

In the above example `prepareForRendering` is called for every invalidated template. This will trigger recompilation even before you refresh the page you're working on in the browser.

> It is up to you how to handle hot reloading for your particular use case. For instance, if you have a static site that pre-renders everything to .html files, you may want to trigger a re-render of static files that use the just invalidated templates.

## Precompiling Templates

To speed up startup of your production server, it is possible to precompile all templates during the build. This way, the template engine can load the .class file for each template directly, without first compiling it.

To do this, you need to pass a directory to the `TemplateEngine`, to specify where compiled template classes are located. Without this directory, `TemplateEngine` will compile templates in memory and precompilation cannot be used.

```java
Path sourceDirectory = Path.of("src/main/jte"); // This is the directory where your .jte files are located.
Path targetDirectory = Path.of("jte"); // This is the directoy where compiled templates are located.

CodeResolver codeResolver = new DirectoryCodeResolver(sourceDirectory);
TemplateEngine templateEngine = new TemplateEngine(codeResolver, targetDirectory);
```

To precompile all templates, you'd simply invoke `templateEngine.precompileAll();`. However, this would still let your Java process compile all templates on startup.

There is a <a href="https://github.com/casid/jte-maven-compiler-plugin">Maven plugin</a> you can use to precompile all templates during the Maven build. You would need to put this in build / plugins of your projects' `pom.xml`. Please note that paths specified in Java need to match those specified in Maven. 

> It is recommended to create a variable like `${jte.version}` in Maven, to ensure that the jte compiler plugin always matches your jte dependency.

```xml
<plugin>
    <groupId>org.jusecase</groupId>
    <artifactId>jte-maven-compiler-plugin</artifactId>
    <version>${jte.version}</version>
    <configuration>
        <sourceDirectory>src/main/jte</sourceDirectory> <!-- This is the directory where your .jte files are located. -->
        <targetDirectory>jte</targetDirectory> <!-- This is the directoy where compiled templates are located. -->
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

## Output Escaping

It is recommended to wrap `TemplateOutput` when output escaping is needed. For instance, if you already have <a href="https://jsoup.org/cookbook/cleaning-html/whitelist-sanitizer">jsoup</a> in your project you could do this:

```java
public class SecureOutput implements TemplateOutput {

    private final TemplateOutput output;

    public SecureOutput(TemplateOutput output) {
        this.output = output;
    }

    @Override
    public void writeContent(String value) {
        output.writeContent(value);
    }

    @Override
    public void writeUserContent(String value) {
        output.writeContent(Jsoup.clean(value, Whitelist.basic()));
    }
}
```

Before rendering, you'd simply wrap the actual `TemplateOutput` you are using:

```java
TemplateOutput output = new SecureOutput(new StringOutput());
```

In rare cases you may want to skip output escaping for a certain element. You can do this by using `$unsafe{}` instead of `${}`. For instance, to trust the model name, we would write:

```
$unsafe{model.name}
```
