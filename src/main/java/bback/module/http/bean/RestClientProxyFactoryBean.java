package bback.module.http.bean;

import bback.module.http.helper.LogHelper;
import bback.module.http.interfaces.HttpAgent;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.proxy.RestClientProxyFactory;
import org.springframework.beans.factory.FactoryBean;

public class RestClientProxyFactoryBean<T> implements FactoryBean<T> {
    private static final LogHelper LOGGER = LogHelper.of(RestClientProxyFactoryBean.class);
    private final RestClientProxyFactory<T> proxyFactory;
    private final Class<T> restClientInterface;

    public RestClientProxyFactoryBean(Class<T> restClientInterface, HttpAgent httpAgent, ResponseMapper dataMapper) {
        this.restClientInterface = restClientInterface;
        this.proxyFactory = new RestClientProxyFactory<>(this.restClientInterface, httpAgent, dataMapper);
    }

    @Override
    public T getObject() throws Exception {
        T object = this.proxyFactory.newInstance();
        LOGGER.log("RestClient Bean 이 정상적으로 등록되었습니다. 등록된 Class : {}", this.restClientInterface.getName());
        return object;
    }

    @Override
    public Class<?> getObjectType() {
        return this.restClientInterface;
    }
}
