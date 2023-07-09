package gg.jte;

import java.util.List;

/**
 * Responsible for resolving template code.
 * Used by the {@link TemplateEngine} to transfer templates into native Java/Kotlin code.
 * When running in production with precompiled templates, no {@link CodeResolver} is required.
 */
public interface CodeResolver {

    /**
     * Resolves the code of a template.
     * @param name The name of the template, e.g. <code>"tag/util/card.jte"</code>.
     * @return The code of the resolved template, or <code>null</code> if no template with this name exists.
     */
    String resolve(String name);

    /**
     * Resolves the code of a template, which is required to exist.
     * @param name The name of the template, e.g. <code>"tag/util/card.jte"</code>.
     * @return The code of the resolved template, this is never <code>null</code>.
     * @throws TemplateNotFoundException if no template with this name exists.
     *
     * Implementations that have better knowledge why the loading failed, are expected to
     * override this method and provide information about the problem in the thrown exception message.
     */
    default String resolveRequired(String name) throws TemplateNotFoundException {
        String code = resolve(name);
        if (code == null) {
            throw new TemplateNotFoundException(name + " not found");
        }

        return code;
    }

    /**
     * Resolves the last modification time of a template.
     * @param name The name of the template, e.g. <code>"tag/util/card.jte"</code>.
     * @return The last modification time of this template in milliseconds, or <code>0L</code> if no template with this name exists.
     * In case this {@link CodeResolver} does not support modification times <code>0L</code> should be returned.
     */
    long getLastModified(String name);

    /**
     * Resolves all template names this {@link CodeResolver} can resolve.
     * @return A list of all existing templates.
     * @throws UnsupportedOperationException in case this operation is not supported by this code resolver
     */
    default List<String> resolveAllTemplateNames() {
        throw new UnsupportedOperationException("This code resolver does not support finding all template names!");
    }

    /**
     * Checks if a template with this name exists.
     * @param name The name of the template, e.g. <code>"tag/util/card.jte"</code>.
     * @return <code>true</code> if a template with this name exists, otherwise false.
     */
    default boolean exists(String name) {
        return resolve(name) != null;
    }

}
