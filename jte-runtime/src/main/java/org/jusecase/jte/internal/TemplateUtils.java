package org.jusecase.jte.internal;

import java.util.HashMap;
import java.util.Map;

public class TemplateUtils {
    @SuppressWarnings("unused") // by template code
    public static Map<String, Object> toMap(Object ... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String)pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
