package bback.module.http.spring;

import bback.module.http.interfaces.HttpAgent;
import bback.module.http.interfaces.ResponseMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Objects;

class RestClientScannerConfigurer implements InitializingBean, ApplicationContextAware, BeanNameAware, BeanDefinitionRegistryPostProcessor {
    private String basePackage;
    private Class<? extends Annotation> annotationClass;
    private ApplicationContext applicationContext;
    private String beanName;

    @Override // BeanDefinitionRegistryPostProcessor 의 implements
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        ClassPathRestClientScanner scanner = new ClassPathRestClientScanner(registry);
        scanner.setBasePackage(this.basePackage);
        scanner.setAnnotationClass(this.annotationClass);
        scanner.setHttpAgentBeanList(this.applicationContext.getBeanNamesForType(HttpAgent.class));
        scanner.setResponseMapperBeanDefinitionSet(this.applicationContext.getBeanNamesForType(ResponseMapper.class));
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // NOP
    }

    @Override // InitializingBean 의 implements
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(this.basePackage, "basePackage 값이 비어 있습니다.");
        Objects.requireNonNull(this.annotationClass, "annotationClass 값이 비어 있습니다.");
        Objects.requireNonNull(this.applicationContext, "applicationContext 값이 비어 있습니다.");
        Objects.requireNonNull(this.beanName, "beanName 값이 비어 있습니다.");
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
