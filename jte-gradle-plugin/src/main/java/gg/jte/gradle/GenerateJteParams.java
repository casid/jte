package gg.jte.gradle;

import gg.jte.ContentType;
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
    RegularFileProperty getSourceDirectory();
    RegularFileProperty getTargetDirectory();
    Property<ContentType> getContentType();

    Property<String> getPackageName();

    Property<Boolean> getTrimControlStructures();

    Property<String[]> getHtmlTags();

    Property<Boolean> getHtmlCommentsPreserved();

    Property<Boolean> getBinaryStaticContent();

    RegularFileProperty getTargetResourceDirectory();

    Property<Boolean> getGenerateNativeImageResources();

    Property<String> getProjectNamespace();

    MapProperty<String, Map<String, String>> getJteExtensions();
}

