# jte Templates

## Introduction

jte is a simple, yet powerful templating engine for Java. All jte templates are compiled to Java class files, meaning jte adds essentially zero overhead to your application. All template files use the .jte file extension.

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
    output.writeSafeContent("Hello world!");
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
    output.writeSafeContent("Hello ");
    output.writeUnsafe(model.jte);
    output.writeSafeContent("!");
  }
}
```

Note the difference of safe und unsafe content. All static parts of a template are treated as safe. All dynamic parts are treated as unsafe to avoid cross-site scripting (XSS) attacks.

> **Caution!** All core `TemplateOutput` implementations make no difference between handling safe and unsafe content. Output escaping comes in many flavours and jte doesn't want to force an opinion on you. See the section [Output Escaping](#output-escaping) for more details.

## Output Escaping

It is recommended to wrap `TemplateOutput` when output escaping is needed. For instance, if you already have jsoup dependency in your project you could do this:

```java
public class SecureOutput implements TemplateOutput {

    private final TemplateOutput output;

    public SecureOutput(TemplateOutput output) {
        this.output = output;
    }

    @Override
    public void writeSafeContent(String value) {
        output.writeSafeContent(value);
    }

    @Override
    public void writeUnsafeContent(String value) {
        output.writeSafeContent(Jsoup.clean(value, Whitelist.basic()));
    }
}
```

Before rendering, you'd simply wrap the actual `TemplateOutput` you are using:

```java
TemplateOutput output = new SecureOutput(new StringOutput());
```

In rare cases you may want to skip output escaping for a certain element. You can do this by using `$safe{}` instead of `${}`. For instance, to trust our model name, we would write:

```
$safe{model.name}
```
