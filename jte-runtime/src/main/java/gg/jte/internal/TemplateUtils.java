package gg.jte.internal;

import gg.jte.Content;
import gg.jte.TemplateOutput;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused") // by template code
public final class TemplateUtils {
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

    public static Content toContent(Object param) {
        if (param == null) {
            return null;
        }

        if (param instanceof Content) {
            return (Content)param;
        }

        return new RawContent(param);
    }

    private static final class RawContent implements Content {

        private final Object param;

        public RawContent(Object param) {
            this.param = param;
        }

        @Override
        public void writeTo(TemplateOutput output) {
            output.writeContentPart(param.toString());
        }
    }
}
