package bback.module.http.wrapper;


import bback.module.http.helper.LogHelper;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class RequestMetadata {
    private final MediaType contentType;
    private final Map<String, String> headerValuesMap;
    private final Map<String, String> pathValuesMap;
    private final Map<String, String> queryValuesMap;
    private final String url;
    @Nullable
    private final Object bodyData;
    @Nullable
    private final Object[] args;
    private final LogHelper restClientLogger;

    public RequestMetadata(
            String url
            , MediaType contentType
            , Map<String, String> headerValuesMap
            , Map<String, String> pathValuesMap
            , Map<String, String> queryValuesMap
            , @Nullable Object bodyData
            , @Nullable Object[] args
            , LogHelper restClientLogger
    ) {
        this.url = url;
        this.contentType = contentType;
        this.headerValuesMap = headerValuesMap;
        this.pathValuesMap = pathValuesMap;
        this.queryValuesMap = queryValuesMap;
        this.bodyData = bodyData;
        this.args = args;
        this.restClientLogger = restClientLogger;
    }

    public RequestMetadata(String url, MediaType mediaType, LogHelper restClientLogger) {
        this(url, mediaType, new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>(), null, null, restClientLogger);
    }

    public static RequestMetadata of(String requestUrl, MediaType mediaType, LogHelper restClientLogger) {
        return new RequestMetadata(requestUrl, mediaType, restClientLogger);
    }

    public static RequestMetadata of(
            String requestUrl
            , MediaType contentType
            , Map<String, String> headerValuesMap
            , Map<String, String> pathValuesMap
            , Map<String, String> queryValuesMap
            , Object bodyData
            , Object[] args
            , LogHelper restClientLogger
    ) {
        return new RequestMetadata(requestUrl, contentType, headerValuesMap, pathValuesMap, queryValuesMap, bodyData, args, restClientLogger);
    }

    public MediaType getContentType() {
        return contentType;
    }

    public Map<String, String> getHeaderValuesMap() {
        return headerValuesMap;
    }

    public Map<String, String> getPathValuesMap() {
        return pathValuesMap;
    }

    public Map<String, String> getQueryValuesMap() {
        return queryValuesMap;
    }

    public String getUrl() {
        return url;
    }

    @Nullable
    public Object getBodyData() {
        return bodyData;
    }

    @Nullable
    public Object[] getArgs() {
        return args;
    }

    public LogHelper getRestClientLogger() {
        return restClientLogger;
    }

    public boolean isFormContent() {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(this.contentType);
    }
}
