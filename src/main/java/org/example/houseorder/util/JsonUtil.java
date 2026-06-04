package org.example.houseorder.util;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private JsonUtil() {}

    public static String toJson(Object o) {
        if (o == null) return "null";
        if (o instanceof String) return "\"" + esc((String) o) + "\"";
        if (o instanceof Number || o instanceof Boolean || o instanceof BigDecimal) return String.valueOf(o);
        if (o instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Iterator<? extends Map.Entry<?, ?>> it = ((Map<?, ?>) o).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<?, ?> e = it.next();
                sb.append(toJson(String.valueOf(e.getKey()))).append(":").append(toJson(e.getValue()));
                if (it.hasNext()) sb.append(",");
            }
            return sb.append("}").toString();
        }
        if (o instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            Iterator<?> it = ((List<?>) o).iterator();
            while (it.hasNext()) {
                sb.append(toJson(it.next()));
                if (it.hasNext()) sb.append(",");
            }
            return sb.append("]").toString();
        }
        return "\"" + esc(String.valueOf(o)) + "\"";
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t")
                .replace("\b", "\\b").replace("\f", "\\f");
    }
}
