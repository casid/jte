package gg.jte.gradle;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

import java.util.Map;

public interface JteCompilerWorkerParameters extends WorkParameters {
    RegularFileProperty getSourceDirectory();
    RegularFileProperty getTargetDirectory();
    Property<String> getContentType();
    Property<String> getPackageName();
    Property<Boolean> getTrimControlStructures();
    Property<String> getHtmlTags();
    Property<Boolean> getHtmlCommentsPreserved();
    Property<Boolean> getBinaryStaticContent();
    RegularFileProperty getTargetResourceDirectory();
    Property<String> getProjectNamespace();
    MapProperty<String, Map<String, String>> getJteExtensions();
}
