package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;


/**
 * configuration extension for the plugin
 * interface because it uses managed properties
 *
 * @author edward3h
 * @since 2021-05-03
 */
public abstract class JteBuildSpec
{
    protected final ObjectFactory objectFactory;

    public JteBuildSpec(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public abstract Property<JteStage> getStage();
    public abstract Property<Path> getSourceDirectory();
    public abstract Property<Path> getTargetDirectory();
    public abstract Property<ContentType> getContentType();
    public abstract  Property<Boolean> getTrimControlStructures();
    public abstract Property<String[]> getHtmlTags();
    public abstract Property<Boolean> getHtmlCommentsPreserved();
    public abstract Property<Boolean> getBinaryStaticContent();
    public abstract Property<String> getPackageName();
    public abstract Property<Path> getTargetResourceDirectory();
    public abstract ConfigurableFileCollection getCompilePath();
    public abstract Property<String> getHtmlPolicyClass();
    public abstract Property<String[]> getCompileArgs();
    public abstract Property<String[]> getKotlinCompileArgs();
    public abstract Property<String> getProjectNamespace();
    public abstract ListProperty<JteExtensionSettings> getJteExtensions();

    public void jteExtension(String className) {
        JteExtensionSettings extensionSettings = objectFactory.newInstance(JteExtensionSettings.class);
        extensionSettings.getClassName().set(className);
        getJteExtensions().add(extensionSettings);
    }

    public void jteExtension(String className, Action<JteExtensionSettings> action) {
        JteExtensionSettings extensionSettings = objectFactory.newInstance(JteExtensionSettings.class);
        extensionSettings.getClassName().set(className);
        getJteExtensions().add(extensionSettings);
        action.execute(extensionSettings);
    }

    public void precompile() {
        getStage().set(JteStage.PRECOMPILE);
    }

    public void generate() {
        getStage().set(JteStage.GENERATE);
    }

    void toParams(GenerateJteParams params) {
        params.getBinaryStaticContent().set(getBinaryStaticContent());
        params.getCompilerClasspath().from(getCompilePath());
        params.getContentType().set(getContentType());
        params.getHtmlTags().set(getHtmlTags());
        params.getHtmlCommentsPreserved().set(getHtmlCommentsPreserved());
        getJteExtensions().get().forEach(e -> params.getJteExtensions().put(e.getClassName().get(), e.getProperties().get()));
        params.getPackageName().set(getPackageName());
        params.getProjectNamespace().set(getProjectNamespace());
        params.getSourceDirectory().set(getSourceDirectory().get().toFile());
        params.getTargetDirectory().set(getTargetDirectory().get().toFile());
        params.getTargetResourceDirectory().set(getTargetResourceDirectory().get().toFile());
        params.getTrimControlStructures().set(getTrimControlStructures());
    }

    void toParams(PrecompileJteWorker.Parameters params) {
        params.getBinaryStaticContent().set(getBinaryStaticContent());
        params.getContentType().set(getContentType());
        params.getHtmlTags().set(getHtmlTags());
        params.getHtmlCommentsPreserved().set(getHtmlCommentsPreserved());
        params.getPackageName().set(getPackageName());
        params.getSourceDirectory().set(getSourceDirectory().get().toFile());
        params.getTargetDirectory().set(getTargetDirectory().get().toFile());
        params.getTargetResourceDirectory().set(getTargetResourceDirectory().get().toFile());
        params.getTrimControlStructures().set(getTrimControlStructures());
        params.getCompileArgs().set(getCompileArgs());
        params.getKotlinCompileArgs().set(getKotlinCompileArgs());
    }
}
