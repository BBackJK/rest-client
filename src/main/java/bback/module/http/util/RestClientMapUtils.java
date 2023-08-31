package bback.module.http.util;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public final class RestClientMapUtils {

    private RestClientMapUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key, Function<K, V> invokeFunction) {
        V value = map.get(key);
        if ( value != null ) {
            return value;
        }
        return map.computeIfAbsent(key, invokeFunction);
    }

    public static <K, V> Map<K, V> toReadonly(Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }
}
