package bback.module.http.spring;

import bback.module.http.annotations.RestClient;
import bback.module.http.bean.agent.RestTemplateAgent;
import bback.module.http.bean.mapper.DefaultResponseMapper;
import bback.module.http.exceptions.RestClientCommonException;
import bback.module.http.helper.LogHelper;
import bback.module.http.interfaces.HttpAgent;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.util.RestClientClassUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Set;

public class RestClientPostBeanDefinitionProcessor extends AbstractPostBeanDefinitionProcessor {

    private static final LogHelper LOGGER = LogHelper.of(RestClientPostBeanDefinitionProcessor.class);
    private static final ClassLoader[] CLASS_LOADERS = RestClientClassUtils.getClassLoaders();

    private static final String DEFAULT_AGENT_CLASS_NAME = "RestTemplateAgent";
    private static final String DEFAULT_MAPPER_CLASS_NAME = "DefaultResponseMapper";
    private static final String DEFAULT_AGENT_CLASS_PATH = "bback.module.http.bean.agent";
    private static final String DEFAULT_MAPPER_CLASS_PATH = "bback.module.http.bean.mapper";

    private final Set<BeanDefinition> httpAgentBeanDefinitions;
    private final Set<BeanDefinition> responseMapperBeanDefinitions;
    private final BeanDefinition defaultResponseMapperBeanDefinition;
    private final BeanDefinition defaultHttpAgentBeanDefinition;
    private final Class<? extends Annotation> annotationClass;

    public RestClientPostBeanDefinitionProcessor(
            Class<? extends Annotation> annotationClass
            , Set<BeanDefinition> httpAgentBeanDefinitions
            , Set<BeanDefinition> responseMapperBeanDefinitions
    ) {
        this.annotationClass = annotationClass;
        this.httpAgentBeanDefinitions = httpAgentBeanDefinitions;
        this.responseMapperBeanDefinitions = responseMapperBeanDefinitions;
        this.defaultHttpAgentBeanDefinition = this.getDefaultHttpAgentBeanDefinition(httpAgentBeanDefinitions);
        this.defaultResponseMapperBeanDefinition = this.getDefaultResponseMapperBeanDefinition(responseMapperBeanDefinitions);
        if ( this.defaultHttpAgentBeanDefinition == null || this.defaultResponseMapperBeanDefinition == null ) {
            throw new RestClientCommonException(String.format("%s, %s 를 찾을 수 없습니다.", DEFAULT_AGENT_CLASS_NAME, DEFAULT_MAPPER_CLASS_NAME));
        }
    }

    @Override
    protected void postProcess(AbstractBeanDefinition definition) {
        String beanClassName = definition.getBeanClassName();
        if ( beanClassName != null ) {
            // RestClientProxyFactoryBean 생성자에 Interface Class 주입
            definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
            // RestClientProxyFactoryBean 생성자에 HttpAgent Bean 주입
            definition.getConstructorArgumentValues().addGenericArgumentValue(this.findHttpAgentBeanDefinition(beanClassName));
            // RestClientProxyFactoryBean 생성자에 ResponseMapper Bean 주입
            definition.getConstructorArgumentValues().addGenericArgumentValue(this.findResponseMapperBeanDefinition(beanClassName));
        }
    }

    @Nullable
    private BeanDefinition getDefaultHttpAgentBeanDefinition(Set<BeanDefinition> httpAgentBeanDefinitions) {
        for (BeanDefinition bd : httpAgentBeanDefinitions) {
            String beanClassName = bd.getBeanClassName();
            if ( beanClassName != null && beanClassName.equals(DEFAULT_AGENT_CLASS_PATH + "." + DEFAULT_AGENT_CLASS_NAME) ) {
                return bd;
            }
        }
        return null;
    }

    @Nullable
    private BeanDefinition getDefaultResponseMapperBeanDefinition(Set<BeanDefinition> responseMapperBeanDefinitions) {
        for (BeanDefinition bd : responseMapperBeanDefinitions) {
            String beanClassName = bd.getBeanClassName();
            if ( beanClassName != null && beanClassName.equals(DEFAULT_MAPPER_CLASS_PATH + "." + DEFAULT_MAPPER_CLASS_NAME) ) {
                return bd;
            }
        }
        return null;
    }

    /**
     * HttpAgent 로 이루어진 BeanDefinition 중 해당 RestClient 의 설정으로 설정한 class 와 일치하는 httpAgent 를 가져온다. 없으면 defaultHttpAgent
     */
    private BeanDefinition findHttpAgentBeanDefinition(String restClientBeanClassName) {
        Class<? extends HttpAgent> defaultHttpAgentClass = RestTemplateAgent.class;
        try {
            RestClient restClient = this.getRestClientAnnotation(restClientBeanClassName);
            if ( restClient != null ) {
                defaultHttpAgentClass = restClient.agent();
            }
        } catch (ClassNotFoundException e) {
            LOGGER.err(e.getMessage());
        }

        BeanDefinition result = this.defaultHttpAgentBeanDefinition;
        for (BeanDefinition def : this.httpAgentBeanDefinitions) {
            if (def != null && defaultHttpAgentClass.getName().equals(def.getBeanClassName())) {
                result = def;
                break;
            }
        }
        return result;
    }

    /**
     * ResponseMapper 로 이루어진 BeanDefinition 중 해당 RestClient 의 설정으로 설정한 class 와 일치하는 ResponseMapper 를 가져온다. 없으면 defaultResponseMapper
     */
    private BeanDefinition findResponseMapperBeanDefinition(String restClientBeanClassName) {
        Class<? extends ResponseMapper> defaultResponseMapperClass = DefaultResponseMapper.class;
        try {
            RestClient restClient = this.getRestClientAnnotation(restClientBeanClassName);
            if ( restClient != null ) {
                defaultResponseMapperClass = restClient.mapper();
            }
        } catch (ClassNotFoundException e) {
            LOGGER.err(e.getMessage());
            // ignore..
        }

        BeanDefinition result = this.defaultResponseMapperBeanDefinition;
        for (BeanDefinition def : this.responseMapperBeanDefinitions) {
            if (def != null && defaultResponseMapperClass.getName().equals(def.getBeanClassName())) {
                result = def;
                break;
            }
        }
        return result;
    }

    /*
     * RestClient Bean Class Name 으로부터 Class 를 가져와서 RestClient Annotation 을 가져온다.
     */
    private RestClient getRestClientAnnotation(String restClientBeanClassName) throws ClassNotFoundException {
        if ( restClientBeanClassName == null || restClientBeanClassName.isEmpty() ) {
            return null;
        }

        Class<?> restClientClass = RestClientClassUtils.classForName(restClientBeanClassName, CLASS_LOADERS);
        Annotation restClientAnnotation = restClientClass.getAnnotation(this.annotationClass);

        if ( restClientAnnotation == null || restClientAnnotation.annotationType() != RestClient.class) {
            LOGGER.err("restClientAnnotation 이 null 이거나 Annotation 이 RestClient 가 아닙니다.");
            throw new RestClientCommonException("RestClient Bean 을 생성하는데 문제가 발생하였습니다.");
        }

        return (RestClient) restClientAnnotation;
    }
}
