package bback.module.http.util;

import bback.module.logger.Log;
import bback.module.logger.LogFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public final class RestClientReflectorUtils {

    private static final Log LOGGER = LogFactory.getLog(RestClientReflectorUtils.class);

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

    public static List<Field> filterLocalFields(@NonNull Class<?> clazz) {
        Objects.requireNonNull(clazz);
        return Arrays.stream(clazz.getDeclaredFields()).filter(f -> {
            int mod = f.getModifiers();
            return !Modifier.isFinal(mod) && !Modifier.isStatic(mod);
        }).collect(Collectors.toList());
    }

    @Nullable
    public static String getGetterMethodByField(Field field) {
        if ( field == null ) return null;
        return "get"+RestClientObjectUtils.toPascal(field.getName());
    }

    public static List<String> getGetterFieldNameByClass(Class<?> clazz) {
        if ( clazz == null || RestClientClassUtils.isPrimitiveOrString(clazz) || clazz.isInterface() ) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        int fieldCount = fields.length;
        for (int i=0; i<fieldCount; i++) {
            try {
                Field f = fields[i];
                if ( f != null ) {
                    String fieldGetterName = getGetterMethodByField(f);
                    clazz.getMethod(fieldGetterName);
                    result.add(f.getName());
                }
            } catch (NoSuchMethodException e) {
                // ignore
            }
        }
        return result;
    }
}
