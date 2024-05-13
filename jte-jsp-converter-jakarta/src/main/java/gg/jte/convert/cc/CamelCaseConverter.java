package gg.jte.convert.cc;

public class CamelCaseConverter {
    public static void convertTo(StringBuilder sb) {
        boolean nextUpperCase = false;

        for (int i = 0; i < sb.length(); ++i) {
            char c = sb.charAt(i);

            if (c == '-' || c == '_') {
                nextUpperCase = i > 0;
                sb.deleteCharAt(i--);
            } else if (nextUpperCase) {
                sb.setCharAt(i, Character.toUpperCase(c));
                nextUpperCase = false;
            }
        }
    }
}
