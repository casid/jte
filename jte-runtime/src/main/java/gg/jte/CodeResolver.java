package gg.jte;

import java.util.List;

public interface CodeResolver {
    String resolve(String name);

    long getLastModified(String name);

    default List<String> resolveAllTemplateNames() {
        throw new UnsupportedOperationException("This code resolver does not support finding all template names!");
    }

    default boolean exists(String name) {
        return resolve(name) != null;
    }

}
