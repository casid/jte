package gg.jte.gradle;

import gg.jte.ContentType;

import java.nio.file.Path;

public interface JteParameters {

    Path getSourceDirectory();
    void setSourceDirectory(Path value) ;

    Path getTargetDirectory();
    void setTargetDirectory(Path value);

    ContentType getContentType();
    void setContentType(ContentType value);

    boolean getTrimControlStructures();
    void setTrimControlStructures(boolean value);

    String[] getHtmlTags();
    void setHtmlTags(String[] value);

    String[] getHtmlAttributes();
    void setHtmlAttributes(String[] value);

    boolean getHtmlCommentPreserved();
    void setHtmlCommentPreserved(boolean value);
}
