package gg.jte;

public class TestUtils {

    public static String repeat(String s, int amount) {
        StringBuilder result = new StringBuilder(s.length() * amount);
        for (int i = 0; i < amount; i++) {
            result.append(s);
        }
        return result.toString();
    }
}
