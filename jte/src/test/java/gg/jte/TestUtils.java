package gg.jte;

public class TestUtils {

    private static final double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));

    public static String repeat(String s, int amount) {
        StringBuilder result = new StringBuilder(s.length() * amount);
        for (int i = 0; i < amount; i++) {
            result.append(s);
        }
        return result.toString();
    }

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
