package bback.module.http.reflector;

import bback.module.http.interfaces.RestCallback;
import bback.module.http.util.RestClientClassUtils;
import bback.module.http.util.RestClientReflectorUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RequestParamMetadata {

    private static final List<Class<? extends Annotation>> ALLOWED_PARAMETER_ANNOTATIONS = Collections.unmodifiableList(Stream.of(RequestParam.class, RequestHeader.class, RequestBody.class, PathVariable.class).collect(Collectors.toList()));

    @NonNull
    private final Class<?> paramClass;
    private final List<String> getterMethodNames;
    private final Annotation annotation;
    private final String paramName;

    public RequestParamMetadata(@NonNull Parameter parameter) {
        this.paramClass = parameter.getType();
        this.getterMethodNames = RestClientClassUtils.getGetterFieldNameByClass(this.paramClass);
        this.annotation = this.parseAnnotation(parameter);
        this.paramName = this.parseParamName(parameter, this.annotation);
    }

    public boolean isRestCallback() {
        return RestCallback.class.equals(this.paramClass);
    }

    public boolean isAnnotationRequestParam() {
        return this.annotation != null && RequestParam.class.equals(this.annotation.annotationType());
    }

    public boolean isAnnotationRequestHeader() {
        return this.annotation != null && RequestHeader.class.equals(this.annotation.annotationType());
    }

    public boolean isAnnotationPathVariable() {
        return this.annotation != null && PathVariable.class.equals(this.annotation.annotationType());
    }

    public boolean isAnnotationRequestBody() {
        return this.annotation != null && RequestBody.class.equals(this.annotation.annotationType());
    }

    public boolean isListType() {
        return Arrays.asList(this.paramClass.getInterfaces()).contains(List.class)
                || this.paramClass.isAssignableFrom(List.class);
    }

    public boolean isMapType() {
        return Arrays.asList(this.paramClass.getInterfaces()).contains(Map.class)
                || this.paramClass.isAssignableFrom(Map.class);
    }

    public boolean isReferenceType() {
        return !this.isListType() && !this.isMapType() && !RestClientClassUtils.isPrimitiveOrString(this.paramClass) && !this.isRestCallback();
    }

    public boolean canRequestParam(boolean isOnlyRequestParam, boolean isEmptyAllAnnotation, List<String> pathValueNames) {
        if ( isOnlyRequestParam ) {
            return isAnnotationRequestParam();
        }

        if ( isEmptyAllAnnotation ) {
            return !pathValueNames.contains(this.paramName);
        }

        return !this.hasAnnotation() && !pathValueNames.contains(this.paramName) && !this.isRestCallback();
    }

    @Nullable
    public Annotation getAnnotation() {
        return this.annotation;
    }

    public boolean hasAnnotation() {
        return this.annotation != null;
    }

    public List<String> getGetterMethodNames() {
        return getterMethodNames;
    }

    public String getParamName() {
        return this.paramName;
    }

    @Nullable
    private Annotation parseAnnotation(Parameter parameter) {
        for (Class<? extends Annotation> a : ALLOWED_PARAMETER_ANNOTATIONS) {
            Annotation anno = parameter.getAnnotation(a);
            if ( anno != null ) {
                return anno;
            }
        }
        return null;
    }

    @NonNull
    private String parseParamName(Parameter parameter, Annotation annotation) {
        if ( annotation != null && !RequestBody.class.equals(annotation.annotationType()) ) {
            String result = null;
            String value = (String) RestClientReflectorUtils.annotationMethodInvoke(annotation, "value");
            String name = (String) RestClientReflectorUtils.annotationMethodInvoke(annotation, "name");
            if ( name != null && !name.isEmpty() ) {
                result = name;
            } else if ( value != null && !value.isEmpty() ) {
                result = value;
            }
            return result == null ? parameter.getName() : result;
        }
        return parameter.getName();
    }
}
