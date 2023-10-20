# JSP Converter

This converter converts JSP templates to jte templates. If you plan to migrate a huge JSP project to jte, this converter will take care of most of the boring, error-prone parts.

## Features

- No duplicate code during conversion, with a jte bridging tag you convert your application in small steps
- Register custom converters for individual JSP tags
- Can be used from the [IntelliJ plugin](https://plugins.jetbrains.com/plugin/14521-jte), to convert a JSP file with a single click

## Getting started

First you need to add `jte-jsp-converter` as test dependency to your project:

=== "Maven"

    ```xml linenums="1"
    <dependency>
        <groupId>gg.jte</groupId>
        <artifactId>jte-jsp-converter</artifactId>
        <version>{{ latest-git-tag }}</version>
        <scope>test</test>
    </dependency>
    ```

=== "Gradle"

    ```groovy linenums="1"
    testImplementation("gg.jte:jte-jsp-converter:{{ latest-git-tag }}")
    ```

Then, add a new class in `src/test/java` to the project that you plan to migrate to jte:

```java linenums="1"
import gg.jte.convert.jsp.Converter;
import gg.jte.convert.jsp.JspElementType;
import gg.jte.convert.jsp.JspToJteConverter;

import java.nio.file.Path;
import java.util.EnumSet;

public class JspConverter extends JspToJteConverter {

    // The IntelliJ plugin will look for the main method of a class extending JspToJteConverter, when you click Code/Convert JSP file to jte file
    public static void main( String[] args ) {
        convertFromIntelliJPlugin(args, new JspConverter(), JspConverter::init);
    }

    public static void init(Converter converter ) {
        // Configure, how your jte files should be formatted
        converter.setIndentationCount(3);
        converter.setIndentationChar(' ');
        converter.setLineSeparator("\n");

        // Configure default imports for your jte files
        converter.setPrefix("@import static my.JteContext.*\n");

        // Register converters for your custom JSP tags
        converter.register("my:tag1", new MyTag1Converter());
        converter.register("my:tag2", new MyTag2Converter());

        // If you have a global import file that you include in every JSP, you need to define it here
        String imports = "/WEB-INF/jsp/includes/import.jsp";
        converter.addInlinedInclude(imports);

        // Configure all JSP elements that the converter should ignore.
        converter.addSuppressions(
            imports,
            EnumSet.of(JspElementType.UseBean, JspElementType.Scriptlet, JspElementType.Comment, JspElementType.CustomTag, JspElementType.Declaration)
        );
    }

    public JspConverter() {
        super(
            Path.of("webapp/WEB-INF"), // Your WEB-INF directory, containing JSPs
            Path.of("webapp/jte"),     // Your jte root directory
            "my:jte"                   // The name of your jte bridging tag (more details below)
        );
    }
}
```

## Add a bridging tag

The converter fails if it finds an unknown element during conversion. If you try to convert a JSP tag that uses another JSP tag, you will see an error message like this:

> The tag <my:example/> is used by this tag and not converted to jte yet. You should convert <my:example/> first. If this is a tag that should be always converted by hand, implement getNotConvertedTags() and add it there.

Let's assume `#!jsp <my:example/>` uses no other tags. In this case the converter will run and create `tag/my/example.jte`. It will also delete `#!jsp <my:example/>` and replace all usages in your JSP directory with this code:

```jsp linenums="1"
<my:jte jte="tag/my/example.jte" param1="foo" param2="bar"/>
```

This means your application will be fully functional after each conversion. This is why we need to define a bridging tag, so that jte can be embedded in JSP tags. This is highly application specific, so it's not part of the converter project. Here's a working example that takes care of tag bodies and enums passed as Strings (sigh):

```java linenums="1"
import gg.jte.Content;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.util.HashMap;
import java.util.Map;

public class JteTag extends BodyTagSupport implements DynamicAttributes {

   private static final Logger log = LoggerFactory.getLogger(TemplateRenderer.class);

   private TemplateEngine templateEngine; // Injection in JSP tags is messy, you somehow need to obtain a reference to the jte template engine your application uses

   private String jte;
   private final Map<String, Object> params = new HashMap<>();

   @Override
   public int doAfterBody() throws JspException {
      BodyContent localBodyContent = getBodyContent();

      // In case there is a tag body, it is passed as jte content parameter
      if ( localBodyContent != null ) {
         params.put("bodyContent", new JteBodyContent(localBodyContent));
      }

      return super.doAfterBody();
   }

   @Override
   public int doEndTag() throws JspException {
      try {
         convertParamsIfRequired();

         templateEngine.render(jte, params, new JspWriterOutput(pageContext.getOut()));

         return super.doEndTag();
      }
      finally {
         resetFields();
      }
   }

   @Override
   public void setDynamicAttribute( String uri, String localName, Object value ) {
      params.put(localName, value);
   }

   public void setJte( String name ) {
      jte = name;
   }

   @NonNull
   private Content convertParamToContent( @NonNull Object param ) {
      if ( param instanceof String ) {
         return new JteStringContent((String)param);
      }

      log.error(param.getClass() + " cannot be converted to jte content, probably this isn't working as intended! Will use empty content in jte tag " + jte);
      return templateOutput -> {};
   }

   @NonNull
   private <T extends Enum<T>> T convertParamToEnum( @NonNull String param, Class<T> definedClass ) {
      return Enum.valueOf(definedClass, param);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void convertParamsIfRequired() {
      Map<String, Class<?>> paramInfo = templateEngine.getParamInfo(jte);

      for ( Map.Entry<String, Class<?>> definedParam : paramInfo.entrySet() ) {
         String name = definedParam.getKey();
         Object actualParam = params.get(name);
         if ( actualParam == null ) {
            continue;
         }

         Class<?> definedClass = definedParam.getValue();

         if ( definedClass == Content.class && !(actualParam instanceof Content) ) {
            params.put(name, convertParamToContent(actualParam));
         } else if ( Enum.class.isAssignableFrom(definedClass) && actualParam instanceof String ) {
            params.put(name, convertParamToEnum((String)actualParam, (Class<? extends Enum>)definedClass));
         }
      }
   }

   private void resetFields() {
      jte = null;
      params.clear();
   }

   private static class JteStringContent implements Content {

       private final String string;

       public JteStringContent( String string ) {
          this.string = string;
       }

       @Override
       public boolean isEmptyContent() {
          return string == null || string.isEmpty();
       }

       @Override
       public void writeTo( TemplateOutput output ) {
          output.writeUserContent(string);
       }
    }

   private static class JteBodyContent implements Content {

      private final String content;

      public JteBodyContent( @NonNull BodyContent bodyContent ) {
         content = bodyContent.getString();
      }

      @Override
      public void writeTo( TemplateOutput templateOutput ) {
         templateOutput.writeContent(content);
      }
   }
}
```

Register the bridging tag in your taglib like this:

```xml linenums="1"
<tag>
   <name>jte</name>
   <tag-class>my.JteTag</tag-class>
   <body-content>scriptless</body-content>
   <attribute>
       <name>jte</name>
       <required>true</required>
       <rtexprvalue>true</rtexprvalue>
   </attribute>
   <dynamic-attributes>true</dynamic-attributes>
</tag>
```

The above tag writes to the underlying `JspWriter`, so that the jte template contributes to the output of the JSP page it is embedded into. Thus, we need a `gg.jte.TemplateOutput` implementation to support this:

```java linenums="1"
import gg.jte.TemplateOutput;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.UncheckedIOException;

public class JspWriterOutput implements TemplateOutput {

    private final JspWriter jspWriter;

    public JspWriterOutput(JspWriter jspWriter) {
        this.jspWriter = jspWriter;
    }

    @Override
    public void writeContent(String value) {
        try {
            jspWriter.write(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeContent(String value, int beginIndex, int endIndex) {
        try {
            jspWriter.write(value, beginIndex, endIndex - beginIndex);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
```

That's it! You should be able to convert your first JSP tag to jte!
