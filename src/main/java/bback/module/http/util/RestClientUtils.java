package bback.module.http.util;

public final class RestClientUtils {

    private RestClientUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String HTTP_HEADER_AUTH_KEY = "Authorization";
    public static final String HEADER_CONTENT_TYPE_KEY = "Content-Type";
    public static final String HEADER_CONTENT_TYPE_DEFAULT = "text/plain";

    public static boolean isSuccess(int httpCode) {
        return httpCode / 100 == 2;
    }
}
