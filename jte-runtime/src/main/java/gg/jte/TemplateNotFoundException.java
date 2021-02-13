package gg.jte;

public class TemplateNotFoundException extends TemplateException {
    public TemplateNotFoundException(String message) {
        super(message);
    }

    public TemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
