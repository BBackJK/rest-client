package bback.module.http.spring;

import bback.module.http.helper.LogHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(RestClientBeanScannerRegistrar.class)
public class AnnotatedRestClientAutoConfigurer implements InitializingBean {
    private static final LogHelper LOGGER = LogHelper.of(AnnotatedRestClientAutoConfigurer.class);
    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.debug("AnnotatedRestClientAutoConfigurer bean is registered...");
    }
}
