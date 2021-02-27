package gg.jte.gradle;

import gg.jte.ContentType;
import gg.jte.runtime.Constants;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

import java.nio.file.Path;

public abstract class JteTaskBase extends DefaultTask {

    protected Path sourceDirectory;
    protected Path targetDirectory;
    protected ContentType contentType;
    protected Boolean trimControlStructures;
    protected String[] htmlTags;
    protected String[] htmlAttributes;
    protected Boolean htmlCommentsPreserved;
    protected Boolean binaryStaticContent;
    protected String packageName = Constants.PACKAGE_NAME_PRECOMPILED;

    @InputDirectory
    public Path getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(Path value) {
        sourceDirectory = value;
    }

    @OutputDirectory
    @Optional
    public Path getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(Path value) {
        targetDirectory = value;
    }

    @Input
    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType value) {
        contentType = value;
    }

    @Input
    @Optional
    public Boolean getTrimControlStructures() {
        return trimControlStructures;
    }

    public void setTrimControlStructures(Boolean value) {
        trimControlStructures = value;
    }

    @Input
    @Optional
    public String[] getHtmlTags() {
        return htmlTags;
    }

    public void setHtmlTags(String[] value) {
        htmlTags = value;
    }

    @Input
    @Optional
    public String[] getHtmlAttributes() {
        return htmlAttributes;
    }

    public void setHtmlAttributes(String[] value) {
        htmlAttributes = value;
    }

    @Input
    @Optional
    public Boolean getHtmlCommentsPreserved() {
        return htmlCommentsPreserved;
    }

    public void setHtmlCommentsPreserved(Boolean value) {
        htmlCommentsPreserved = value;
    }

    public void setBinaryStaticContent(Boolean binaryStaticContent) {
        this.binaryStaticContent = binaryStaticContent;
    }

    @Input
    @Optional
    public Boolean getBinaryStaticContent() {
        return binaryStaticContent;
    }

    @Input
    @Optional
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
