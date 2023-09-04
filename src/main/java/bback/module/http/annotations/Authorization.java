package bback.module.http.annotations;


import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorization {

    String type() default "Bearer";
    boolean onPrefix() default true;
}
