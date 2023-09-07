package bback.module.http.spring;

import bback.module.http.helper.LogHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(RestClientBeanScannerRegistrar.class)
@ConditionalOnMissingBean(AnnotatedRestClientAutoConfigurer.class)
public class BootRestClientAutoConfigurer implements InitializingBean {
    private static final LogHelper LOGGER = LogHelper.of(BootRestClientAutoConfigurer.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.debug("BootRestClientAutoConfigurer bean is registered...");
    }
}
