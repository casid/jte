package gg.jte.watcher;

public class TestUtils {

    private static final double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));

    public static boolean isLegacyJavaVersion() {
        return JAVA_VERSION < 11;
    }

    public static void sleepIfLegacyJavaVersion(long millis) {
        if (isLegacyJavaVersion()) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
