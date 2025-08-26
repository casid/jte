package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

interface PrecompileJteParams extends WorkParameters {
    DirectoryProperty getSourceDirectory();

    DirectoryProperty getTargetDirectory();

    Property<ContentType> getContentType();

    Property<String> getPackageName();

    Property<Boolean> getTrimControlStructures();

    ListProperty<String> getHtmlTags();

    Property<String> getHtmlPolicyClass();

    Property<Boolean> getHtmlCommentsPreserved();

    Property<Boolean> getBinaryStaticContent();

    ListProperty<String> getCompileArgs();

    ListProperty<String> getKotlinCompileArgs();

    DirectoryProperty getTargetResourceDirectory();

    ConfigurableFileCollection getCompilePath();
}
