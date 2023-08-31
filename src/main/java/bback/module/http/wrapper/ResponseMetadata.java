package bback.module.http.wrapper;

import bback.module.http.util.RestClientObjectUtils;
import bback.module.http.util.RestClientUtils;

public class ResponseMetadata {
    private static final String DEFAULT_ERROR_MESSAGE = "요청한 서버에서 에러가 발생하였습니다.";
    private final int httpCode;
    private final boolean success;
    private final String stringResponse;
    private final String contentType;

    private ResponseMetadata(int httpCode, boolean success, String stringResponse, String contentType) {
        this.httpCode = httpCode;
        this.success = success;
        this.stringResponse = stringResponse;
        this.contentType = contentType;
    }

    public ResponseMetadata(int httpCode, String stringResponse, String contentType) {
        this(httpCode, RestClientUtils.isSuccess(httpCode), stringResponse, contentType);
    }

    public int getHttpCode() {
        return httpCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStringResponse() {
        return stringResponse;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return "ResponseMetadata{" +
                "httpCode=" + httpCode +
                ", success=" + success +
                ", stringResponse='" + stringResponse + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }

    public String getFailMessage() {
        if ( this.success ) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        return sb.append(RestClientObjectUtils.isEmpty(stringResponse) ? DEFAULT_ERROR_MESSAGE : stringResponse).toString();
    }

    public boolean isXml() {
        // application/xml;charset=UTF-8
        if ( RestClientObjectUtils.isEmpty(contentType) ) {
            return false;
        }
        String[] contentTypeSplits = contentType.split(";");
        if ( contentTypeSplits.length < 1 ) {
            return false;
        }
        return contentTypeSplits[0].endsWith("xml");
    }
}
