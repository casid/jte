package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

import java.nio.file.Path;
import java.util.List;

public abstract class JteTaskBase extends DefaultTask {
    protected final ObjectFactory objectFactory;
    protected final Property<Path> sourceDirectory;
    protected boolean configured;
    protected boolean wiring;
    protected boolean projectEvaluated;

    protected JteTaskBase(JteStage taskStage, ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        this.sourceDirectory = objectFactory.property(Path.class);
        try {
            getProject().afterEvaluate(p -> projectEvaluated = true);
        } catch (Exception e) {
            // swallow expected exception
        }
        getLogger().info("This is a {}", getClass().getName());
        onlyIf(t -> {
            getLogger().info("{} onlyIf configured {}, taskStage {}, configuredStage {}", getClass().getName(), configured, taskStage, getConfiguredStage().getOrNull());
            return (configured && (!getConfiguredStage().isPresent() || getConfiguredStage().get() == JteStage.NONE)) || taskStage == getConfiguredStage().get();
        });
    }

    @Internal
    protected abstract Property<JteStage> getConfiguredStage();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public Property<Path> getSourceDirectory()
    {
        if (!wiring && !projectEvaluated) {
            configured = true;
        }
        return sourceDirectory;
    }

    @OutputDirectory
    public abstract Property<Path> getTargetDirectory();

    @Input
    public abstract Property<ContentType> getContentType();

    @Input
    @Optional
    public abstract Property<Boolean> getTrimControlStructures();

    @Input
    @Optional
    public abstract Property<String[]> getHtmlTags();

    @Input
    @Optional
    public abstract Property<Boolean> getHtmlCommentsPreserved();

    @Input
    @Optional
    public abstract Property<Boolean> getBinaryStaticContent();

    @Input
    public abstract Property<String> getPackageName();

    @OutputDirectory
    @Optional
    public abstract Property<Path> getTargetResourceDirectory();

    @Input
    public abstract ListProperty<JteExtensionSettings> getJteExtensions();

    protected Provider<List<String>> toListProvider(Provider<String[]> arrayProperty) {
        return arrayProperty.flatMap(arr -> objectFactory.listProperty(String.class).value(List.of(arr)));
    }

    protected void wireExtension(JteExtension extension, Provider<Path> defaultTargetDirectory) {
        wiring = true;
        getLogger().info("{} wireExtension", getClass().getName());
        getBinaryStaticContent().set(extension.getBinaryStaticContent());
        getConfiguredStage().set(extension.getStage());
        getContentType().set(extension.getContentType());
        getHtmlCommentsPreserved().set(extension.getHtmlCommentsPreserved());
        getHtmlTags().set(extension.getHtmlTags());
        getJteExtensions().set(extension.getJteExtensions());
        getPackageName().set(extension.getPackageName());
        getSourceDirectory().set(extension.getSourceDirectory());
        getTargetDirectory().set(extension.getTargetDirectory().orElse(defaultTargetDirectory));
        getTargetResourceDirectory().set(extension.getTargetResourceDirectory());
        getTrimControlStructures().set(extension.getTrimControlStructures());
    }
}
