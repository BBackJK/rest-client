package bback.module.http.annotations;


import bback.module.spring.AutoConfiguredRestClientScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AutoConfiguredRestClientScannerRegistrar.class)
public @interface EnableRestClient {
}
