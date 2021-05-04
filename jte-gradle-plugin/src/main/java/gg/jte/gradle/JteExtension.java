package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.provider.Property;

import java.nio.file.Path;


/**
 * configuration extension for the plugin
 * interface because it uses managed properties
 *
 * @author edward3h
 * @since 2021-05-03
 */
public interface JteExtension
{
    Property<JteStage> getStage();
    Property<Path> getSourceDirectory();
    Property<Path> getTargetDirectory();
    Property<ContentType> getContentType();
    Property<Boolean> getTrimControlStructures();
    Property<String[]> getHtmlTags();
    Property<String[]> getHtmlAttributes();
    Property<Boolean> getHtmlCommentsPreserved();
    Property<Boolean> getBinaryStaticContent();
    Property<String> getPackageName();
    Property<Path> getTargetResourceDirectory();
}
