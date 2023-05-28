package gg.jte.models.runtime;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JteView will be included on the methods of a generated Templates interface. This is to make it easier to adapt with
 * frameworks that use annotations.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JteView {
    /**
     * value is the name of the template
     * @return
     */
    String value();
}
