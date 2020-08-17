package gg.jte;

public final class TemplateException extends RuntimeException {
    public TemplateException(String message) {
        super(message);
    }

    public TemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}
