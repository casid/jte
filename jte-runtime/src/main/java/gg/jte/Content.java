package gg.jte;

/**
 * This interface can be used to create reusable content blocks for templates through Java code.
 *
 * When using the content block shorthand <code>@`...`</code> in templates, behind the scenes an anonymous class of
 * {@link Content} is created as well. For example:
 * <pre>
 *     !{var name = "world";}
 *     !{var myContent = @`
 *         Hello ${name}!
 *         This is a reusable content block.
 *     `;}
 *
 *     ${myContent}
 *     ${myContent}
 * </pre>
 *
 * Would be the same as:
 * <pre>
 *     public class MyContent implements Content {
 *         private final String name;
 *
 *         public MyContent(String name) {
 *             this.name = name;
 *         }
 *
 *         &#64;Override
 *         public void writeTo(TemplateOutput output) {
 *             output.writeContent("Hello ");
 *
 *             // User provided content, must be output escaped!
 *             output.writeUserContent(name);
 *
 *             output.writeContent("!\n This is a reusable content block.");
 *         }
 *     }
 * </pre>
 *
 * While the above example doesn't make a lot of sense, {@link Content} can be a very powerful
 * abstraction for complex tasks.
 */
public interface Content {
    void writeTo(TemplateOutput output);

    default boolean isEmptyContent() {
        return false;
    }
}
