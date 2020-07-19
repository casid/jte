package org.jusecase.jte.internal;

public class StringUtils {
    public static boolean containsIgnoreCase(String str, String searchStr) {
        final int len = searchStr.length();
        final int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (str.regionMatches(true, i, searchStr, 0, len)) {
                return true;
            }
        }
        return false;
    }
}
