---
title: jte syntax
description: jte syntax reference.
---

# Templates syntax

A minimal template would look like this.

```html linenums="1"
Hello world!
```

Rendering it with `templateEngine.render("example.jte", null, output);` will return `Hello world!`.

## Displaying data

To display data in a template, wrap it in `${}`:

=== "Java"

    ```html linenums="1"
    @import my.Model
    @param Model model

    Hello ${model.name}!
    ```
=== "Kotlin"

    ```html linenums="1"
    @import my.Model
    @param model: Model

    Hello ${model.name}!
    ```

If your model class would look like this:

=== "Java"

    ```java linenums="1"
    package my;
    public class Model {
        public String name = "jte";
    }
    ```

=== "Kotlin"

    ```kotlin linenums="1"
    package my
    data class Model(val name: String = "jte")
    ```

The output of the above template would be `Hello jte!`.

## Control structures

jte provides convenient shortcuts for common Java control structures, such as conditional statements and loops. These shortcuts provide a clean, terse way of working with control structures while remaining familiar to their Java counterparts.

### If Statements

You may construct if statements using the keywords `@if`, `@elseif`, `@else` and `@endif`. These translate directly to their Java counterparts:

```html linenums="1"
@if(model.entries.isEmpty())
    I have no entries!
@elseif(model.entries.size() == 1)
    I have one entry!
@else
    I have ${model.entries.size()} entries!
@endif
```

!!! tip

    Since Java 14+, you can also use [Pattern Matching for `instanceof`](https://openjdk.java.net/jeps/394):

    ```html linenums="1"
    @if (model instanceof SubModel subModel)
        ${subModel.getSpecial()}
    @endif
    ```

### Loops

In addition to if statements, jte provides the `@for` and `@endfor` keywords to loop over iterable data. Again, `@for` translates directly to its Java or Kotlin counterpart:

=== "Java"

    ```html linenums="1"
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

=== "Kotlin"

    ```html linenums="1"
    @for(entry in model.entries)
    <li>${entry.title}</li>
    @endfor

    @for(i in 0..10)
    <li>i is ${i}</li>
    @endfor
    ```

When looping, you may use the `gg.jte.support.ForSupport` class to gain information about the loop, such as whether you are in the first or last iteration through the loop.

=== "Java"

    ```html linenums="1"
    @import gg.jte.support.ForSupport
    <%-- ... --%>
    @for(var entryLoop : ForSupport.of(model.entries))
        <tr class="${(entryLoop.getIndex() + 1) % 2 == 0 ? "even" : "odd"}">
            ${entryLoop.get().title}
        </tr>
    @endfor
    ```

=== "Kotlin"

    ```html linenums="1"
    @import gg.jte.support.ForSupport
    <%-- ... --%>
    @for(entryLoop in ForSupport.of(model.entries))
    <tr class="${if((entryLoop.index + 1) % 2) == 0) "even" else "odd"}">
        ${entryLoop.get().title}
    </tr>
    @endfor
    ```

Since jte 3.0, it is possible to use `@else` before `@endfor`. The `@else` content renders in case no elements were iterated over in the loop. This is useful for displaying an empty list state without an additional `@if`. For example:

=== "Java"

    ```html linenums="1"
    @for(var item : groceryList)
      <tr>
        <td>${item.getName()}</td>
        <td>${item.getQuantity()}</td>
      </tr>
    @else
      <tr>
        <td colspan="2">You have forgotten your groceries list at home!</td>
      </tr>
    @endfor
    ```

=== "Kotlin"

    ```html linenums="1"
    @for(item in groceryList)
    <tr>
        <td>${item.name}</td>
        <td>${item.quantity}</td>
    </tr>
    @else
    <tr>
        <td colspan="2">You have forgotten your groceries list at home!</td>
    </tr>
    @endfor
    ```

## Comments

jte allows you to define comments in your templates.

```xml linenums="1"
<%-- This comment will not be present in the rendered output --%>
```

!!! info

    jte comments are not included in the output of your template.

## Template Calls

To share common functionality between templates, you can call other templates. All templates must be located within the jte root directory.

Here is an example template, located in `my/drawEntry.jte`

```html linenums="1"
@import my.Entry
@param Entry entry
@param boolean verbose

<h2>${entry.title}</h2>
@if(verbose)
    <h3>${entry.subtitle}</h3>
@endif
```

Template calls are similar to regular Java methods.

```html linenums="1"
@template.my.drawEntry(model.entry1, true)
@template.my.drawEntry(model.entry2, false)
```

Subdirectories in the jte root directory act like packages in Java. For instance, if the `drawEntry` template was located in `my/entry/drawEntry.jte`, you would call it like this:

```html linenums="1"
@template.my.entry.drawEntry(model.entry1, true)
@template.my.entry.drawEntry(model.entry2, false)
```

### Named parameters

If you don't want to depend on the parameter order, you can explicitly name parameters when calling the template.

```html linenums="1"
@template.my.entry.drawEntry(entry = model.entry1, verbose = true)
@template.my.entry.drawEntry(entry = model.entry2, verbose = false)
```

!!! note

    Named parameters is what the [IntelliJ plugin](https://plugins.jetbrains.com/plugin/14521-jte) suggests by default.

### Default parameters

You can also define default values for all parameters so they only need to be passed when needed.

```html linenums="1"
@import my.Entry
@param Entry entry
@param boolean verbose = false

<h2>${entry.title}</h2>
@if(verbose)
    <h3>${entry.subtitle}</h3>
@endif
```

The second call could then be simplified to this:

```html linenums="1"
@template.my.entry.drawEntry(entry = model.entry1, verbose = true)
@template.my.entry.drawEntry(entry = model.entry2)
```

### Varargs

The last parameter of a template can be a varargs parameter. For instance, if you created a tag to wrap elements in a list, you could create a template such as `list.jte`:

```html linenums="1"
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

```html linenums="1"
@template.list(title = "Things to do", "Cook dinner", "Eat", "Netflix and Chill")
```

## Content

`gg.jte.Content` is a special parameter type to pass template code to other templates, much like lambdas in Java. They are handy for sharing structures between different templates.

Here is an example layout with a content block:

```html linenums="1"
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

The shorthand to create content blocks within jte templates is an `@` followed by two backticks. Let's call the layout we just created and pass a page content and footer:

```html linenums="1"
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

Declare local variables like this:

```html linenums="1"
!{var innerObject = someObject.get().very().deeply().located().internal().object();}

${innerObject.a()}
${innerObject.b()}
```

Local variables translate 1:1 to Java code.
