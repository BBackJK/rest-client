package bback.module.http.proxy;

import bback.module.http.annotations.RestClient;
import bback.module.http.interfaces.HttpAgent;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.util.RestClientMapUtils;
import bback.module.http.wrapper.RestClientMethodInvoker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

class RestClientProxy<T> implements InvocationHandler {
    private final RestClient restClient;
    private final HttpAgent httpAgent;
    private final ResponseMapper dataMapper;
    private final Map<Method, RestClientMethodInvoker> cachedMethod;

    public RestClientProxy(
            Class<T> restClientInterface
            , Map<Method, RestClientMethodInvoker> cachedMethod
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
    public Object invoke(Object o, Method method, Object[] args) {
        RestClientMethodInvoker invoker = RestClientMapUtils.computeIfAbsent(this.cachedMethod, method, m -> new RestClientMethodInvoker(m, this.httpAgent, this.dataMapper));
        return invoker.invoke(args, this.restClient.url());
    }

    private void initCachedMethodHandlerByMethod(Method[] methods, HttpAgent httpAgent, ResponseMapper dataMapper) {
        if ( this.cachedMethod.isEmpty() && methods != null ) {
            int methodCount = methods.length;
            for (int i=0; i<methodCount; i++) {
                Method m = methods[i];
                this.cachedMethod.put(m, new RestClientMethodInvoker(m, httpAgent, dataMapper));
            }
        }
    }
}
