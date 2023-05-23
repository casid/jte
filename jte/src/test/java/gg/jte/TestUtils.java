package gg.jte;

public class TestUtils {

    private static final double JAVA_VERSION = Double.parseDouble(System.getProperty("java.specification.version", "0"));

    public static boolean isInstanceOfPatternMatchingJavaVersion() {
        return JAVA_VERSION >= 14; // Not really needed since we compile with jdk 17, but leave as pattern for the next Java version features.
    }
}
