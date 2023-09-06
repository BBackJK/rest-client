package bback.module.http.bean.agent;

import bback.module.http.configuration.RestClientConnectProperties;
import bback.module.http.exceptions.RestClientCallException;
import bback.module.http.helper.LogHelper;
import bback.module.http.interfaces.HttpAgent;
import bback.module.http.util.RestClientUtils;
import bback.module.http.wrapper.RequestMetadata;
import bback.module.http.wrapper.ResponseMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UnirestAgent implements HttpAgent {

    private static final String LOGGING_DELIMITER = "========================================================================================";
    private final UnirestInstance unirest;
    private final ObjectMapper om;

    public UnirestAgent(RestClientConnectProperties connectProperties) {

        Config config = new Config();
        config.socketTimeout(connectProperties.getSocketTimeout() * 1000)
                .connectTimeout(connectProperties.getConnectKeepAlive() * 1000)
                .concurrency(
                        connectProperties.getConnectPoolSize()
                        , connectProperties.getConnectPoolPerRoute()
                )
                .followRedirects(false)
                .enableCookieManagement(false)
                .cacheResponses(
                        Cache.builder().depth(10)
                        .maxAge(10, TimeUnit.SECONDS)
                )
        ;

        this.unirest = new UnirestInstance(config);
        this.om = new ObjectMapper();
    }

    @Override
    public ResponseMetadata doGet(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.GET);
    }

    @Override
    public ResponseMetadata doPost(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.POST);
    }

    @Override
    public ResponseMetadata doPatch(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.PATCH);
    }

    @Override
    public ResponseMetadata doPut(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.PUT);
    }

    @Override
    public ResponseMetadata doDelete(RequestMetadata requestMetadata) throws RestClientCallException {
        return this.doCall(requestMetadata, HttpMethod.DELETE);
    }

    private ResponseMetadata doCall(RequestMetadata requestMetadata, HttpMethod httpMethod) {
        LogHelper logger = requestMetadata.getRestClientLogger();
        HttpRequest<?> request = this.handleRequestBuilder(requestMetadata, httpMethod, logger);
        this.requestLogging(request, logger);
        long requestAt = System.currentTimeMillis();
        try {
            HttpResponse<String> response = request.asString();
            long responseAt = System.currentTimeMillis();
            this.responseLogging(response, logger, responseAt - requestAt);
            List<String> contentTypeList = response.getHeaders().get(RestClientUtils.HEADER_CONTENT_TYPE_KEY);
            return new ResponseMetadata(
                    response.getStatus()
                    , response.getBody()
                    , contentTypeList.isEmpty() ? RestClientUtils.HEADER_CONTENT_TYPE_DEFAULT : contentTypeList.get(0)
            );
        } catch (UnirestException e) {
            throw new RestClientCallException(e);
        }
    }


    private HttpRequest<?> handleRequestBuilder(RequestMetadata requestMetadata, HttpMethod httpMethod, LogHelper logger) {
        HttpRequestWithBody requestBuilder = this.unirest.request(httpMethod.name(), requestMetadata.getUrl());
        this.handleHeader(requestBuilder, requestMetadata.getHeaderValuesMap(), requestMetadata.getContentType());
        this.handlePathValue(requestBuilder, requestMetadata.getPathValuesMap());
        this.handleQueryParameter(requestBuilder, requestMetadata.getQueryValuesMap());
        return this.handleBody(requestBuilder, requestMetadata.getBodyData(), requestMetadata.isFormContent(), logger);
    }

    private void handlePathValue(HttpRequestWithBody requestBuilder, Map<String, String> pathValues) {
        if (pathValues.isEmpty()) return;

        for (Map.Entry<String, String> x : pathValues.entrySet()) {
            String key = x.getKey();
            String value = x.getValue();
            if (key != null && value != null) {
                requestBuilder.routeParam(key, value);
            }
        }
    }

    private void handleQueryParameter(HttpRequestWithBody requestBuilder, Map<String, String> queryValues) {
        if (queryValues.isEmpty()) return;

        for (Map.Entry<String, String> x : queryValues.entrySet()) {
            String key = x.getKey();
            String value = x.getValue();
            if (key != null && value != null) {
                requestBuilder.queryString(key, value);
            }
        }
    }

    private void handleHeader(HttpRequestWithBody requestBuilder, Map<String, String> headerValues, MediaType contentType) {
        if (!headerValues.isEmpty()) {
            for (Map.Entry<String, String> kv : headerValues.entrySet()) {
                String key = kv.getKey();
                String val = kv.getValue();
                if ( key != null && val != null ) {
                    requestBuilder.header(key, val);
                }
            }
        }

        requestBuilder.header(HttpHeaders.CONTENT_TYPE, contentType.toString());
    }

    private HttpRequest<?> handleBody(HttpRequestWithBody requestBuilder, Object body, boolean isUrlEncodedForm, LogHelper logger) {
        if (body == null) return requestBuilder;

        if (isUrlEncodedForm) {
            try {
                Map<String, Object> map = this.om.convertValue(body, Map.class);
                return (map == null || map.isEmpty())
                        ? requestBuilder
                        : requestBuilder.fields(map);
            } catch (IllegalArgumentException e) {
                logger.warn("x-www-form-urlencoded 의 form data 가 Map 으로 변환되는데에 에러가 발생하였습니다. body :: " + body);
                logger.warn(e.getMessage());
                return requestBuilder.body(body);
            }
        } else {
            return requestBuilder.body(body);
        }
    }

    private void requestLogging(HttpRequest<?> request, LogHelper logger) {
        Headers headers = request.getHeaders();
        int headerSize = headers.size();
        HttpMethod httpMethod = request.getHttpMethod();

        logger.log("Request\t\t" + LOGGING_DELIMITER);
        logger.log("Request\t\t| Agent\t\t\t\t: " + this.getClass().getSimpleName());
        logger.log("Request\t\t| Url\t\t\t\t: " + (httpMethod == null ? "N/A" : httpMethod.name()) + " " + request.getUrl());

        if (headerSize < 1) {
            logger.log("Request\t\t| Header\t\t\t: EMPTY");
        } else {
            headers.all().forEach(h -> {
                String headerKey = h.getName();
                String headerValue = h.getValue();
                if (RestClientUtils.HTTP_HEADER_AUTH_KEY.equals(headerKey)) {
                    headerValue = "◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼◼";
                }
                logger.log("Request\t\t| Header\t\t\t: " + headerKey + " - " + headerValue);
            });
        }

        request.getBody().ifPresent(b -> {
            if (b.isMultiPart()) {
                List<BodyPart> bodyParts = new ArrayList<>(b.multiParts());
                logger.log("Request\t\t| Body\t\t\t\t: " + bodyParts);
            } else if (b.isEntityBody()) {
                logger.log("Request\t\t| Body\t\t\t\t: " + b.uniPart());
            }
        });
        logger.log("Request\t\t" + LOGGING_DELIMITER);
    }

    private void responseLogging(HttpResponse<String> response, LogHelper logger, long callTimeDiff) {
        Headers headers = response.getHeaders();
        int headerSize = headers.size();

        logger.log("Response\t\t" + LOGGING_DELIMITER);
        logger.log("Response\t\t| Agent\t\t\t\t: " + this.getClass().getSimpleName());
        logger.log("Response\t\t| Total Call Millis\t: " + callTimeDiff + " ms");
        logger.log("Response\t\t| Data(String)\t\t: " + response.getBody());
        if (headerSize < 1) {
            logger.log("Response\t\t| Header\t\t\t: EMPTY");
        } else {
            headers.all().forEach(h -> logger.log("Response\t\t| Header\t\t\t: " + h.getName() + " - " + h.getValue()));
        }
        logger.log("Response\t\t" + LOGGING_DELIMITER);
    }
}
