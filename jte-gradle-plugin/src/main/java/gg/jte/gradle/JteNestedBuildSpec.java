package gg.jte.gradle;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.nio.file.Path;


/**
 * configuration extension for the plugin
 * interface because it uses managed properties
 *
 * @author edward3h
 * @since 2021-05-03
 */
public abstract class JteNestedBuildSpec extends JteBuildSpec
{
    private final String name;

    @Inject
    public JteNestedBuildSpec(String name, ObjectFactory objectFactory) {
        super(objectFactory);
        this.name = name;
        getRelativePackageName().convention(name);
        getRelativeSourceDirectory().convention(Path.of(name));
    }

    public abstract Property<Path> getRelativeSourceDirectory();
    public abstract Property<String> getRelativePackageName();

    public JteNestedBuildSpec convention(JteBuildSpec parent) {
        getSourceDirectory().convention(parent.getSourceDirectory().map(parentSourceDirectory -> parentSourceDirectory.resolve(getRelativeSourceDirectory().get())));
        getRelativeSourceDirectory().convention(Path.of(getName()));

        getPackageName().convention(parent.getPackageName().map(this::_appendRelativePackageName));
        getRelativePackageName().convention(getName());

        getStage().convention(parent.getStage());
        getTargetDirectory().convention(parent.getTargetDirectory());
        getContentType().convention(parent.getContentType());
        getTrimControlStructures().convention(parent.getTrimControlStructures());
        getHtmlTags().convention(parent.getHtmlTags());
        getHtmlCommentsPreserved().convention(parent.getHtmlCommentsPreserved());
        getBinaryStaticContent().convention(parent.getBinaryStaticContent());
        getTargetResourceDirectory().convention(parent.getTargetResourceDirectory());
        getCompilePath().convention(parent.getCompilePath());
        getHtmlPolicyClass().convention(parent.getHtmlPolicyClass());
        getCompileArgs().convention(parent.getCompileArgs());
        getKotlinCompileArgs().convention(parent.getKotlinCompileArgs());
        getProjectNamespace().convention(parent.getProjectNamespace());
        getJteExtensions().convention(parent.getJteExtensions());
        return this;
    }

    private String _appendRelativePackageName(String parentPackageName) {
        var relativePackageName = getRelativePackageName().get();
        if (!relativePackageName.isBlank()) {
            return parentPackageName + "." + relativePackageName;
        }
        return parentPackageName;
    }

    public String getName() {
        return name;
    }
}
