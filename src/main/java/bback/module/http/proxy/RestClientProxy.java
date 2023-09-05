package bback.module.http.proxy;

import bback.module.http.annotations.RestClient;
import bback.module.http.exceptions.RestClientCallException;
import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.helper.LogHelper;
import bback.module.http.interfaces.HttpAgent;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.interfaces.RestCallback;
import bback.module.http.util.RestClientMapUtils;
import bback.module.http.wrapper.ResponseMetadata;
import bback.module.http.wrapper.RestClientInvoker;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

class RestClientProxy<T> implements InvocationHandler {
    private static final LogHelper LOGGER = LogHelper.of(RestClientProxy.class);
    private final RestClient restClient;
    private final HttpAgent httpAgent;
    private final ResponseMapper dataMapper;
    private final Map<Method, RestClientInvoker> cachedMethod;
    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, RestClientInvoker> cachedMethod
            , HttpAgent httpAgent
            , ResponseMapper dataMapper
    ) {
        this.restClient = restClientInterface.getAnnotation(RestClient.class);
        this.httpAgent = httpAgent;
        this.dataMapper = dataMapper;
        this.cachedMethod = cachedMethod;
        this.initCachedMethodHandlerByMethod(restClientInterface.getMethods(), httpAgent, dataMapper);
    }

    @Override
    @Async
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        LOGGER.warn("thread name :: " + Thread.currentThread().getName());
        LOGGER.warn("Thread id :: " + Thread.currentThread().getId());
        RestClientInvoker invoker = RestClientMapUtils.computeIfAbsent(this.cachedMethod, method, m -> new RestClientInvoker(m, this.httpAgent, this.dataMapper));
        ResponseMetadata response;
//        if (invoker.isAsyncMethod()) {
//            response = CompletableFuture.<ResponseMetadata>supplyAsync(() -> {
//                        LOGGER.warn("thread async name :: " + Thread.currentThread().getName());
//                        LOGGER.warn("Thread async id :: " + Thread.currentThread().getId());
//                        return invoker.invoke(args, this.restClient.url());
//                    })
//                    .handle(((responseMetadata, throwable) -> {
//                        if ( throwable != null ) {
//                            invoker.getRestClientLogger().err(throwable.getMessage());
//                            throw new RestClientCallException(throwable);
//                        } else {
//                            return responseMetadata;
//                        }
//                    })).join();
//        } else {
//            try {
//                response = invoker.invoke(args, this.restClient.url());
//            } catch (RestClientCallException e) {
//                invoker.getRestClientLogger().err(e.getMessage());
//                throw new RestClientCallException();
//            }
//        }

        try {
            response = invoker.invoke(args, this.restClient.url());
        } catch (RestClientCallException e) {
            invoker.getRestClientLogger().err(e.getMessage());
            throw new RestClientCallException();
        }

        if ( !response.isSuccess() && !invoker.hasFailHandler() ) {
            throw new RestClientCallException(response.getFailMessage());
        }

        Object result = null;

        try {
            result = invoker.getRestReturnResolver().resolve(response);
        } catch (RestClientDataMappingException e) {
            invoker.getRestClientLogger().err("response :: \n" + response.getStringResponse());
            invoker.getRestClientLogger().err(e.getMessage());
            throw new RestClientCallException();
        }

        Object finalResult = result;
        Optional.ofNullable(this.getRestCallbackByArg(args)).ifPresent(callback -> callback.run(response.isSuccess(), response.getHttpCode(), finalResult, response.getFailMessage()));

        return result;
    }

    private void initCachedMethodHandlerByMethod(Method[] methods, HttpAgent httpAgent, ResponseMapper dataMapper) {
        if ( this.cachedMethod.isEmpty() && methods != null ) {
            int methodCount = methods.length;
            for (int i=0; i<methodCount; i++) {
                Method m = methods[i];
                this.cachedMethod.put(m, new RestClientInvoker(m, httpAgent, dataMapper));
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
