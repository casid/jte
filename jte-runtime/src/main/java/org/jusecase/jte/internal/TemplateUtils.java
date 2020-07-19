package org.jusecase.jte.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused") // by template code
public class TemplateUtils {
    public static Map<String, Object> toMap(Object ... pairs) {
        if (pairs.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = new HashMap<>(pairs.length / 2);
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String)pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
