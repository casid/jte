package example;

import gg.jte.Content;

import java.util.Collection;
import java.util.Map;

/**
 * Dummy jte context, how you might use it in your project.
 * Contains helper methods for a JSP migration
 */
public class JteContext {
    /**
     * Taken from org.apache.el.parser.AstEmpty implementation
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        } else if (obj instanceof String) {
            return ((String) obj).length() == 0;
        } else if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else {
            return obj instanceof Map && ((Map<?, ?>) obj).isEmpty();
        }
    }

    /**
     * Dummy localization method
     */
    public static Content localize(String key) {
        return null;
    }

    /**
     * Dummy localization method
     */
    public static Content localize(String key, Object ... params) {
        return null;
    }
}
