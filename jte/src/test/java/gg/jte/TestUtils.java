package gg.jte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TestUtils {

    private static final double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));

    public static boolean isInstanceOfPatternMatchingJavaVersion() {
        return JAVA_VERSION >= 14; // Not really needed since we compile with jdk 17, but leave as pattern for the next Java version features.
    }

    public enum TypeSelection {
        A, B, C;
    }

    @SuppressWarnings("unused") // see e.g. gg.jte.TemplateEngineTest.variadic_annotation
    @Target({ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TypeUseAnnotation {
    }

    @SuppressWarnings("unused") // see e.g. gg.jte.TemplateEngineTest.variadic_annotation
    @Target({ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TypeUseAnnotationParam {
        String value();
        int count() default 0;
        TypeSelection typeSelection() default TypeSelection.A;
    }

    @Target({ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Nested {
        String value() default "something";
    }

    @Target({ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Nest1 {
        Nested nested();
    }
}
