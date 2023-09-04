package bback.module.http.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class RestClientObjectUtils {

    private RestClientObjectUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static  boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).isEmpty();
        }
        if (value instanceof Optional) {
            return !((Optional<?>) value).isPresent();
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0;
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        // else
        return false;
    }

    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

    public static String toCamel(String value) {
        if ( value == null || value.isEmpty() ) {
            return "";
        }
        String firstVal = value.substring(0, 1);
        return value.replaceFirst(firstVal, firstVal.toLowerCase());
    }

    public static String toPascal(String value) {
        if ( value == null || value.isEmpty() ) {
            return "";
        }
        String firstVal = value.substring(0, 1);
        return value.replaceFirst(firstVal, firstVal.toUpperCase());
    }
}
