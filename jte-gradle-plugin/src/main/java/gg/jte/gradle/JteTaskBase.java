package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

import java.io.File;
import java.nio.file.Path;

public abstract class JteTaskBase extends DefaultTask {

    public JteTaskBase(JteExtension extension, JteStage stage)
    {
        this.extension = extension;
        this.stage = stage;

        onlyIf(t -> extension.getStage().getOrNull() == stage);
    }

    private final JteStage stage;
    protected final JteExtension extension;

    // for backwards compatibility, set the stage if a setter on the task is called directly
    protected void setterCalled() {
        extension.getStage().set(stage);
    }

    @InputDirectory
    public Path getSourceDirectory() {
        return extension.getSourceDirectory().get();
    }

    public void setSourceDirectory(Path value) {
        extension.getSourceDirectory().set(value);
        setterCalled();
    }

    @OutputDirectory
    @Optional
    public Path getTargetDirectory() {
        return extension.getTargetDirectory().get();
    }

    public void setTargetDirectory(Path value) {
        extension.getTargetDirectory().set(value);
        setterCalled();
    }

    @Input
    public ContentType getContentType() {
        return extension.getContentType().get();
    }

    public void setContentType(ContentType value) {
        extension.getContentType().set(value);
        setterCalled();
    }

    @Input
    @Optional
    public Boolean getTrimControlStructures() {
        return extension.getTrimControlStructures().getOrNull();
    }

    public void setTrimControlStructures(Boolean value) {
        extension.getTrimControlStructures().set(value);
        setterCalled();
    }

    @Input
    @Optional
    public String[] getHtmlTags() {
        return extension.getHtmlTags().getOrNull();
    }

    public void setHtmlTags(String[] value) {
        extension.getHtmlTags().set(value);
        setterCalled();
    }

    @Input
    @Optional
    public Boolean getHtmlCommentsPreserved() {
        return extension.getHtmlCommentsPreserved().getOrNull();
    }

    public void setHtmlCommentsPreserved(Boolean value) {
        extension.getHtmlCommentsPreserved().set(value);
        setterCalled();
    }

    public void setBinaryStaticContent(Boolean binaryStaticContent) {
        extension.getBinaryStaticContent().set(binaryStaticContent);
        setterCalled();
    }

    @Input
    @Optional
    public Boolean getBinaryStaticContent() {
        return extension.getBinaryStaticContent().getOrNull();
    }

    @Input
    @Optional
    public String getPackageName() {
        return extension.getPackageName().getOrNull();
    }

    public void setPackageName(String packageName) {
        extension.getPackageName().set(packageName);
        setterCalled();
    }

    @OutputDirectory
    @Optional
    public Path getTargetResourceDirectory() {
        return extension.getTargetResourceDirectory().getOrNull();
    }

    public void setTargetResourceDirectory(Path targetResourceDirectory) {
        extension.getTargetResourceDirectory().set(targetResourceDirectory);
        setterCalled();
    }
}
