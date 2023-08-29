package gg.jte.springframework.boot.autoconfigure;

public class JteConfigurationException extends RuntimeException {
    public JteConfigurationException(String message) {
        super(message);
    }

    public JteConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
