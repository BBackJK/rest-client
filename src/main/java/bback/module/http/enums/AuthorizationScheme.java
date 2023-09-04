package bback.module.http.enums;


public enum AuthorizationScheme {

    BASIC("Basic"),     // RFC 7617
    BEARER("Bearer")   // RFC 6750
    ;
    private final String scheme;

    AuthorizationScheme(String scheme) {
        this.scheme = scheme;
    }

    public String get() {
        return this.scheme;
    }
}
