package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.nio.file.Path;


/**
 * configuration extension for the plugin
 * interface because it uses managed properties
 *
 * @author edward3h
 * @since 2021-05-03
 */
public abstract class JteExtension extends JteBuildSpec {
    private final NamedDomainObjectContainer<JteNestedBuildSpec> builds;
    public JteExtension(ObjectFactory objectFactory) {
        super(objectFactory);
        builds = objectFactory.domainObjectContainer(JteNestedBuildSpec.class, this::createNested);
    }

    private JteNestedBuildSpec createNested(String name) {
        return objectFactory.newInstance(JteNestedBuildSpec.class, name).convention(this);
    }

    public NamedDomainObjectContainer<JteNestedBuildSpec> getBuilds() {
        return builds;
    }

}