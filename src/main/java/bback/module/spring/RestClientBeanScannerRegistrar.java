package bback.module.spring;

import bback.module.http.annotations.EnableRestClient;
import bback.module.http.annotations.RestClient;
import bback.module.http.bean.agent.RestTemplateAgent;
import bback.module.http.bean.agent.UnirestAgent;
import bback.module.http.bean.mapper.DefaultResponseMapper;
import bback.module.http.configuration.RestClientConnectProperties;
import bback.module.http.exceptions.RestClientCommonException;
import bback.module.http.helper.LogHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

public class RestClientBeanScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
    private static final Class<RestClientScannerConfigurer> SCANNER_CONFIGURER_CLASS = RestClientScannerConfigurer.class;
    private static final LogHelper LOGGER = LogHelper.of(RestClientBeanScannerRegistrar.class);
    private DefaultListableBeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // RestClientConnectProperties Bean Definition register
        BeanDefinitionBuilder restClientConnectPropertiesBuilder = BeanDefinitionBuilder.genericBeanDefinition(RestClientConnectProperties.class);
        registry.registerBeanDefinition(RestClientConnectProperties.class.getName(), restClientConnectPropertiesBuilder.getBeanDefinition());

        // RestTemplateAgent Bean Definition register
        BeanDefinitionBuilder restTemplateBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RestTemplateAgent.class);
        restTemplateBeanDefinitionBuilder.addConstructorArgValue(restClientConnectPropertiesBuilder.getBeanDefinition());
        registry.registerBeanDefinition(RestTemplateAgent.class.getName(), restTemplateBeanDefinitionBuilder.getBeanDefinition());

        // UnirestAgent Bean Definition register
        BeanDefinitionBuilder unirestBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(UnirestAgent.class);
        unirestBeanDefinitionBuilder.addConstructorArgValue(restClientConnectPropertiesBuilder.getBeanDefinition());
        registry.registerBeanDefinition(UnirestAgent.class.getName(), unirestBeanDefinitionBuilder.getBeanDefinition());

        // DefaultResponseMapper Bean Definition register
        BeanDefinitionBuilder responseMapperBuilder = BeanDefinitionBuilder.genericBeanDefinition(DefaultResponseMapper.class);
        registry.registerBeanDefinition(DefaultResponseMapper.class.getName(), responseMapperBuilder.getBeanDefinition());

        // RestClient Bean Definition register
        BeanDefinitionBuilder restClientScannerBuilder = BeanDefinitionBuilder.genericBeanDefinition(SCANNER_CONFIGURER_CLASS);
        restClientScannerBuilder.addPropertyValue("basePackages", this.getBasePackageNames());
        restClientScannerBuilder.addPropertyValue("annotationClass", RestClient.class);
        registry.registerBeanDefinition(SCANNER_CONFIGURER_CLASS.getName(), restClientScannerBuilder.getBeanDefinition());
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    private String[] getBasePackageNames() {
        try {
            List<String> basePackageList = new ArrayList<>();
            String[] enableRestClientBeanNames = this.beanFactory.getBeanNamesForAnnotation(EnableRestClient.class);
            if ( enableRestClientBeanNames.length < 1) {
                LOGGER.debug("@EnableRestClient Bean is Empty.");
            } else {
                for (String enableRestClientBeanName : enableRestClientBeanNames) {
                    BeanDefinition enableRestClientBeanDefinition = this.beanFactory.getBeanDefinition(enableRestClientBeanName);
                    basePackageList.add(ClassUtils.getPackageName(enableRestClientBeanDefinition.getResolvableType().toString()));
                }
            }

            String[] componentScanBeanNames = this.beanFactory.getBeanNamesForAnnotation(ComponentScan.class);
            if ( componentScanBeanNames.length < 1) {
                LOGGER.debug("@ComponentScan Bean is Empty.");
            } else {
                for (String componentScanBeanName : componentScanBeanNames) {
                    BeanDefinition componentScanBeanDefinition = this.beanFactory.getBeanDefinition(componentScanBeanName);
                    String componentScanBeanClassName = componentScanBeanDefinition.getBeanClassName();
                    if ( componentScanBeanClassName != null ) {
                        Class<?> componentScanClassType = ClassUtils.forName(componentScanBeanClassName, ClassUtils.getDefaultClassLoader());
                        ComponentScan componentScan = AnnotationUtils.findAnnotation(componentScanClassType, ComponentScan.class);
                        if (componentScan != null) {
                            basePackageList.addAll(Arrays.asList(componentScan.basePackages()));
                            basePackageList.addAll(Arrays.asList(componentScan.value()));
                            basePackageList.addAll(Arrays.stream(componentScan.basePackageClasses()).map(pck -> pck.getPackage().getName()).collect(Collectors.toList()));
                        }
                    }
                    basePackageList.add(ClassUtils.getPackageName(componentScanBeanDefinition.getResolvableType().toString()));
                }
            }

            Set<String> basePackageSet = new HashSet<>(basePackageList);
            return basePackageSet.toArray(new String[0]);
        } catch (Exception e) {
            LOGGER.err(e.getMessage());
            throw new RestClientCommonException("EnableRestClient 어노테이션클래스를 찾는데 실패하였습니다.");
        }
    }
}
