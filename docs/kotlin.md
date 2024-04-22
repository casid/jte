---
title: jte Kotlin tips
description: Details specific to Kotlin usage
---

# Kotlin Details

Kotlin has entails a few differences. The `jte-kotlin` dependency is needed to enable `.kte` templates that contain 
Kotlin code. Keep in mind that Kotlin application code can use either Java (`.jte`) or Kotlin (`.ket`) templates. `.kte` 
files are only needed for Kotlin based templates. 

## Add required dependencies

This dependency is a compile time dependency to avoid including the Kotlin compiler in the resulting build.

=== "Maven"

    ```xml linenums="1"
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte-kotlin</artifactId>
        <version>{{ latest-git-tag }}</version>
        <scope>compile</scope>
    </dependency>
    ```

=== "Gradle"

    ```groovy linenums="1"
    compileOnly "gg.jte:jte-kotlin:{{ latest-git-tag }}"
    ```

## Common errors

### Non-null parameters

=== "Non-null (`.kte file`)"

    ```html linenums="1"
    @param name: String
    ```

=== "Nullable (`.kte file`)"

    ```html linenums="1"
    @param name: String?
    ```

If a non-null parameter exists in the template, the template will fail to load with an error in the line where the 
parameter is being included. 

```
Error rendering template:

Failed to render admin/index.kte, error at admin/index.kte:1
(many many lines of stack trace)
Caused by: java.lang.NullPointerException: null cannot be cast to non-null type kotlin.String
```
### Writing a non-supported type to a template

```html linenums="1"
@param myList: List

${myList}
```

This will generate an error like so:

```
gg.jte.TemplateException: Failed to compile template, error at myPage/index.kte:3
			jteOutput.writeUserContent(things)
/Users/aUser/app/jte-classes/gg/jte/generated/ondemand/index/JteindexGenerated.kt:13:14
Reason: None of the following functions can be called with the arguments supplied: 
public open fun writeUserContent(p0: Content!): Unit defined in gg.jte.html.HtmlTemplateOutput
public open fun writeUserContent(p0: Boolean): Unit defined in gg.jte.html.HtmlTemplateOutput
(etc.)
```

This will be improved in the future.

### Nulls in `@for` expressions

`@for` loops use an iterator. If a variable is null then the following error will appear:

```
Failed to compile template, error at myApp/index.kte:5
		for (s in things) {
/Users/aUser/code/cds/jte-classes/gg/jte/generated/ondemand/index/JteindexGenerated.kt:10:13
Reason: Not nullable value required to call an 'iterator()' method on for-loop range
```

A quick fix is to make the parameter have an empty list by default.

```html linenums="1"
@for(things in (things ?: listOf())) 
```