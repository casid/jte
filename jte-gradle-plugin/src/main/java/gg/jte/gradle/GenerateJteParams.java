package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

import java.util.Map;

/**
 * Worker API requires a Parameters type that is a 'managed object', to pass data to the Worker.
 * All property types must be Serializable.
 */
public interface GenerateJteParams extends WorkParameters {

    /**
     * The directory where template files are located.
     */
    RegularFileProperty getSourceDirectory();

    /**
     * Destination directory to store generated templates.
     */
    RegularFileProperty getTargetDirectory();

    /**
     * The content type of all templates. Either Plain or Html.
     */
    Property<ContentType> getContentType();

    /**
     * The package name, where template classes are generated to
     */
    Property<String> getPackageName();

    /**
     * Trims control structures, resulting in prettier output.
     */
    Property<Boolean> getTrimControlStructures();

    /**
     * Intercepts the given html tags during template compilation
     * and calls the configured htmlInterceptor during template rendering.
     */
    Property<String[]> getHtmlTags();

    /**
     * By default, jte omits all HTML/CSS/JS comments, when compiling with {@link ContentType#Html}.
     * If you don't want this behavior, you can disable it here.
     */
    Property<Boolean> getHtmlCommentsPreserved();

    /**
     * Setting, that UTF-8 encodes all static template parts at compile time.
     * Only makes sense if you use a binary output, like {@link gg.jte.output.Utf8ByteOutput}.
     */
    Property<Boolean> getBinaryStaticContent();

    /**
     * Directory in which to generate non-java files (resources). Typically, set by plugin rather than end user.
     * Optional - if null, resources will not be generated
     */
    RegularFileProperty getTargetResourceDirectory();

    /**
     * "group/artifact" of the project using jte. Typically, set by plugin rather than end user.
     * Optional - usually done by Gradle.
     */
    Property<String> getProjectNamespace();

    /**
     * Optional - Extensions this template engine should load. Currently, the following extensions exist:
     *
     * <ul>
     *     <li>gg.jte.models.generator.ModelExtension</li>
     *     <li>gg.jte.nativeimage.NativeResourcesExtension</li>
     * </ul>
     */
    MapProperty<String, Map<String, String>> getJteExtensions();

    /**
     * The classpath to use for compilation, including any compiler dependencies
     */
    ConfigurableFileCollection getCompilerClasspath();

    /**
     * The HTML policy class name to use for compilation
     */
    Property<String> getHtmlPolicyClass();

    /**
     * Additional compilation arguments for Java
     */
    Property<String[]> getCompileArgs();

    /**
     * Additional compilation arguments for Kotlin
     */
    Property<String[]> getKotlinCompileArgs();
}

