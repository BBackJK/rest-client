package bback.module.http.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public final class RestClientReflectorUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientReflectorUtils.class);

    private RestClientReflectorUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Nullable
    public static Object annotationMethodInvoke(Annotation target, String invokeMethodName) {
        return annotationMethodInvoke(target, invokeMethodName, null);
    }

    @Nullable
    public static Object annotationMethodInvoke(Annotation target, String invokeMethodName, Object[] args) {
        Class<? extends Annotation> annotationClazz = target.annotationType();
        try {
            return args == null || args.length == 0
                    ? annotationClazz.getMethod(invokeMethodName).invoke(target)
                    : annotationClazz.getMethod(invokeMethodName).invoke(target, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }
}
