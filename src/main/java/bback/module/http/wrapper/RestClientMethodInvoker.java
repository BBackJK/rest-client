package bback.module.http.wrapper;

import bback.module.http.annotations.RestClient;
import bback.module.http.exceptions.RestClientCallException;
import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.helper.LogHelper;
import bback.module.http.interfaces.HttpAgent;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.interfaces.RestCallback;
import bback.module.http.reflector.RequestMethodMetadata;
import bback.module.http.reflector.RestClientMethodValidator;
import bback.module.http.reflector.RestReturnResolver;
import bback.module.http.reflector.RestReturnResolverFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

public class RestClientMethodInvoker {

    @NonNull
    private final HttpAgent httpAgent;
    @NonNull
    private final RequestMethodMetadata requestMethodMetadata;
    @NonNull
    private final RestReturnResolver restReturnResolver;

    @NonNull
    private final LogHelper restClientLogger;

    public RestClientMethodInvoker(Method method, HttpAgent httpAgent, ResponseMapper dataMapper) {
        Class<?> restClientInterface = method.getDeclaringClass();
        RestClient restClient = restClientInterface.getAnnotation(RestClient.class);
        this.httpAgent = httpAgent;
        this.requestMethodMetadata = new RequestMethodMetadata(method);
        this.restReturnResolver = RestReturnResolverFactory.getResolver(this.requestMethodMetadata, dataMapper);
        this.restClientLogger = LogHelper.of(this.getRestClientLogContext(restClientInterface, restClient, method));
        (new RestClientMethodValidator(this.requestMethodMetadata, method)).valid();
    }

    public Object invoke(Object[] args, String origin) throws RestClientCallException {
        RequestMetadata request = this.requestMethodMetadata.applyArgs(args, origin, this.restClientLogger);
        ResponseMetadata response = this.doHttp(request);
        this.checkResponseValid(response);
        return this.handleResponseResult(response, this.getRestCallbackByArg(args));
    }

    private ResponseMetadata doHttp(RequestMetadata request) throws RestClientCallException {
        ResponseMetadata result;

        switch (this.requestMethodMetadata.getRequestMethod()) {
            case GET:
                result = this.httpAgent.doGet(request);
                break;
            case POST:
                result = this.httpAgent.doPost(request);
                break;
            case PATCH:
                result = this.httpAgent.doPatch(request);
                break;
            case PUT:
                result = this.httpAgent.doPut(request);
                break;
            case DELETE:
                result = this.httpAgent.doDelete(request);
                break;
            default:
                throw new RestClientCallException(String.format(" 지원하지 않은 Request Method 입니다. RequestMethod :: %s", this.requestMethodMetadata.getRequestMethod()));
        }

        return result;
    }

    private void checkResponseValid(ResponseMetadata responseMetadata) throws RestClientCallException {
        if ( responseMetadata == null ) {
            throw new RestClientCallException("ResponseMetadata is null. checking HttpAgent.");
        }

        if ( !responseMetadata.isSuccess() && !hasFailHandler() ) {
            throw new RestClientCallException(responseMetadata.getFailMessage());
        }
    }

    private Object handleResponseResult(ResponseMetadata response, @Nullable RestCallback<Object> restCallback) throws RestClientCallException {
        Object result = null;
        try {
            result = this.restReturnResolver.resolve(response);
        } catch (RestClientDataMappingException e) {
            this.restClientLogger.err("response :: \n" + response.getStringResponse());
            this.restClientLogger.err(e.getMessage());
            throw new RestClientCallException();
        }

        Object finalResult = result;
        Optional.ofNullable(restCallback)
                .ifPresent(callback -> callback.run(response.isSuccess(), response.getHttpCode(), finalResult, response.getFailMessage()));
        return result;
    }

    private boolean hasFailHandler() {
        return this.requestMethodMetadata.hasRestCallback()
                || this.requestMethodMetadata.isReturnResultWrap();
    }

    @NonNull
    private String getRestClientLogContext(Class<?> restClientInterface, RestClient restClient, Method m) {
        String value = restClient.value();
        String context = restClient.context();

        if (value.isEmpty() && context.isEmpty()) {
            return String.format("%s::%s", restClientInterface.getSimpleName(), m.getName());
        } else {
            if (!value.isEmpty()) {
                return String.format("%s#%s::%s", restClientInterface.getSimpleName(), value, m.getName());
            } else {
                return String.format("%s#%s::%s", restClientInterface.getSimpleName(), context, m.getName());
            }
        }
    }

    @Nullable
    private RestCallback<Object> getRestCallbackByArg(Object[] args) {
        if ( args == null ) {
            return null;
        }
        int paramCount = args.length;
        for (int i=0; i<paramCount; i++) {
            Object arg = args[i];
            if (arg instanceof RestCallback) {
                return (RestCallback<Object>) arg;
            }
        }
        return null;
    }
}
