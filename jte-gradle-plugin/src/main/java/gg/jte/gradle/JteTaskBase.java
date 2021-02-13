package gg.jte.gradle;

import gg.jte.ContentType;
import org.gradle.api.DefaultTask;

import java.nio.file.Path;

public class JteTaskBase extends DefaultTask implements JteParameters {

    protected Path sourceDirectory;
    protected Path targetDirectory;
    protected ContentType contentType;
    protected boolean trimControlStructures;
    protected String[] htmlTags;
    protected String[] htmlAttributes;
    protected boolean htmlCommentsPreserved;

    @Override
    public Path getSourceDirectory() {
        return sourceDirectory;
    }

    @Override
    public void setSourceDirectory(Path value) {
        sourceDirectory = value;
    }

    @Override
    public Path getTargetDirectory() {
        return targetDirectory;
    }

    @Override
    public void setTargetDirectory(Path value) {
        targetDirectory = value;
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(ContentType value) {
        contentType = value;
    }

    @Override
    public boolean getTrimControlStructures() {
        return trimControlStructures;
    }

    @Override
    public void setTrimControlStructures(boolean value) {
        trimControlStructures = value;
    }

    @Override
    public String[] getHtmlTags() {
        return htmlTags;
    }

    @Override
    public void setHtmlTags(String[] value) {
        htmlTags = value;
    }

    @Override
    public String[] getHtmlAttributes() {
        return htmlAttributes;
    }

    @Override
    public void setHtmlAttributes(String[] value) {
        htmlAttributes = value;
    }

    @Override
    public boolean getHtmlCommentPreserved() {
        return htmlCommentsPreserved;
    }

    @Override
    public void setHtmlCommentPreserved(boolean value) {
        htmlCommentsPreserved = value;
    }
}
