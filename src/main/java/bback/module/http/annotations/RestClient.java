package bback.module.http.annotations;

import bback.module.http.bean.agent.RestTemplateAgent;
import bback.module.http.bean.mapper.DefaultResponseMapper;
import bback.module.http.interfaces.HttpAgent;
import bback.module.http.interfaces.ResponseMapper;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestClient {

    @AliasFor("context")
    String value() default "";
    @AliasFor("value")
    String context() default "";
    String url() default "";

    Class<? extends HttpAgent> agent() default RestTemplateAgent.class;
    Class<? extends ResponseMapper> mapper() default DefaultResponseMapper.class;
}
