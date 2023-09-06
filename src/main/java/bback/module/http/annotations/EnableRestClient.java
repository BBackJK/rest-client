package bback.module.http.annotations;


import bback.module.spring.AnnotatedRestClientAutoConfigurer;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AnnotatedRestClientAutoConfigurer.class)
public @interface EnableRestClient {
}
