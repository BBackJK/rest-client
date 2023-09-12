package bback.module.http.reflector;

import bback.module.http.annotations.Authorization;
import bback.module.http.helper.GetFieldInvoker;
import bback.module.http.interfaces.RestCallback;
import bback.module.http.util.RestClientClassUtils;
import bback.module.http.util.RestClientReflectorUtils;
import bback.module.http.util.RestClientUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class RequestParamMetadata {

    private static final List<Class<? extends Annotation>> ALLOWED_PARAMETER_SPRING_ANNOTATIONS = Collections.unmodifiableList(Stream.of(RequestParam.class, RequestHeader.class, RequestBody.class, PathVariable.class).collect(Collectors.toList()));
    private static final List<Class<? extends Annotation>> ALLOWED_PARAMETER_CUSTOM_ANNOTATIONS = Collections.unmodifiableList(Stream.of(Authorization.class).collect(Collectors.toList()));

    /**
     * Parameter Type 정보
     */
    @NonNull
    private final Class<?> parameterType;

    /**
     * Parameter Type 이 ReferenceType 일 경우, field 에 대한 정보 목록
     */
    private final List<GetFieldInvoker> fieldInvokerList;

    /**
     * parameter 가 가지고 있는 annotation 정보 @Nullable
     */
    private final Annotation annotation;

    /**
     * parameter 선언명
     */
    private final String paramName;

    public RequestParamMetadata(@NonNull Parameter parameter) {
        this.parameterType = parameter.getType();
        this.fieldInvokerList = this.parseFieldInvoker(parameter);
        this.annotation = this.parseAnnotation(parameter);
        this.paramName = this.parseParamName(parameter);
    }

    /**
     * 파라미터가 @RequestParam 어노테이션을 가지고 있는지 여부를 판단
     * @return 파라미터가 @RequestParam 어노테이션을 가지고 있는지 여부
     */
    public boolean isAnnotationRequestParam() {
        return this.annotation != null && RequestParam.class.equals(this.annotation.annotationType());
    }

    /**
     * 파라미터가 @RequestHeader 어노테이션을 가지고 있는지 여부를 판단
     * @return 파라미터가 @RequestHeader 어노테이션을 가지고 있는지 여부
     */
    public boolean isAnnotationRequestHeader() {
        return this.annotation != null && RequestHeader.class.equals(this.annotation.annotationType());
    }

    /**
     * 파라미터가 @PathVariable 어노테이션을 가지고 있는지 여부를 판단
     * @return 파라미터가 @PathVariable 어노테이션을 가지고 있는지 여부
     */
    public boolean isAnnotationPathVariable() {
        return this.annotation != null && PathVariable.class.equals(this.annotation.annotationType());
    }

    /**
     * 파라미터가 @RequestBody 어노테이션을 가지고 있는지 여부를 판단
     * @return 파라미터가 @RequestBody 어노테이션을 가지고 있는지 여부
     */
    public boolean isAnnotationRequestBody() {
        return this.annotation != null && RequestBody.class.equals(this.annotation.annotationType());
    }

    /**
     * 파라미터가 @Authorization 어노테이션을 가지고 있는지 여부를 판단
     * @return 파라미터가 @Authorization 어노테이션을 가지고 있는지 여부
     */
    public boolean isAnnotationAuthorization() {
        return this.annotation != null && Authorization.class.equals(this.annotation.annotationType());
    }

    /**
     * 파라미터가 RestCallback 인지 판단
     * @return 파라미터 타입이 RestCallback 타입인지 여부
     */
    public boolean isRestCallback() {
        return RestCallback.class.equals(this.parameterType);
    }

    /**
     * 파라미터 타입이 List 타입인지 판단
     * @return 파라미터 타입이 List 타입인지 여부
     */
    public boolean isListType() {
        return Arrays.asList(this.parameterType.getInterfaces()).contains(List.class)
                || this.parameterType.isAssignableFrom(List.class);
    }

    /**
     * 파라미터 타입이 Reference Type 인지 판단
     * @return 파라미터 타입이 Reference Type 인지 여부
     */
    public boolean isReferenceType() {
        return !this.isListType() && !this.isMapType() && !RestClientClassUtils.isPrimitiveOrString(this.parameterType) && !this.isRestCallback();
    }

    /**
     * 파라미터가 RequestParameter 가 될 수 있는지 판단하는 함수
     * @param isOnlyRequestParam 파라미터의 메소드가 가진 파라미터들의 어노테이션이 @RequestParam 밖에 없는지 여부
     * @param isEmptyAllAnnotation 파라미터의 메소드가 가진 파라미터들의 어노테이션이 전부 비어있는지 여부
     * @param pathValueNames 파라미터의 메소드가 가진 pathname 으로 이루어진 목록
     * @return 파라미터가 RequestParameter 가 될 수 있는지 여부
     */
    public boolean canRequestParam(boolean isOnlyRequestParam, boolean isEmptyAllAnnotation, List<String> pathValueNames) {
        if ( isOnlyRequestParam ) {
            return isAnnotationRequestParam();
        }

        if ( isEmptyAllAnnotation ) {
            return !pathValueNames.contains(this.paramName);
        }

        return this.hasNotAnnotation() && !pathValueNames.contains(this.paramName) && !this.isRestCallback();
    }

    /**
     * 파라미터가 가지고있는 어노테이션 반환
     * @return 파라미터가 가지고있는 어노테이션
     */
    @Nullable
    public Annotation getAnnotation() {
        return this.annotation;
    }

    /**
     * 파라미터가 가지고 있는 어노테이션이 없는지 여부 판단
     * @return 파라미터가 가지고 있는 어노테이션이 없는지 여부
     */
    public boolean hasNotAnnotation() {
        return this.annotation == null;
    }

    /**
     * 파라미터로 선언된 인자명 반환
     * @return 파라미터로 선언된 인자명
     */
    public String getParamName() {
        return this.paramName;
    }

    /**
     * Parameter Type 이 ReferenceType 일 경우, field 에 대한 정보 목록 반환
     * @return Parameter Type 이 ReferenceType 일 경우, field 에 대한 정보 목록
     */
    public List<GetFieldInvoker> getFieldInvokerList() {
        return this.fieldInvokerList;
    }

    /**
     * 파라미터 타입이 Map 타입인지 판단
     * @return 파라미터 타입이 Map 타입인지 여부
     */
    private boolean isMapType() {
        return Arrays.asList(this.parameterType.getInterfaces()).contains(Map.class)
                || this.parameterType.isAssignableFrom(Map.class);
    }

    /**
     * 파라미터에서 annotation 추출 (Spring 에서 제공하는 어노테이션 + 자체적으로 만든 어노테이션)
     * @param parameter 파라미터
     * @return 파라미터가 가진 어노테이션 (spring 에서 제공하는 어노테이션 우선)
     */
    @Nullable
    private Annotation parseAnnotation(Parameter parameter) {
        for (Class<? extends Annotation> s : ALLOWED_PARAMETER_SPRING_ANNOTATIONS) {
            Annotation springAnnotation = parameter.getAnnotation(s);
            if ( springAnnotation != null ) {
                return springAnnotation;
            }
        }
        for (Class<? extends Annotation> c : ALLOWED_PARAMETER_CUSTOM_ANNOTATIONS) {
            Annotation customAnnotation = parameter.getAnnotation(c);
            if ( customAnnotation != null ) {
                return customAnnotation;
            }
        }
        return null;
    }

    /**
     * 파라미터의 선언된 인자명 반환
     * @param parameter 파라미터
     * @return 파라미터의 선언된 인자명
     */
    @NonNull
    private String parseParamName(Parameter parameter) {
        if ( this.hasNotAnnotation() || this.isAnnotationRequestBody() ) {
            return parameter.getName();
        }

        if ( this.isAnnotationAuthorization() ) {
            return RestClientUtils.HTTP_HEADER_AUTH_KEY;
        }

        String result = null;
        String value = (String) RestClientReflectorUtils.annotationMethodInvoke(this.annotation, "value");
        String name = (String) RestClientReflectorUtils.annotationMethodInvoke(this.annotation, "name");
        if ( name != null && !name.isEmpty() ) {
            result = name;
        } else if ( value != null && !value.isEmpty() ) {
            result = value;
        }
        return result == null ? parameter.getName() : result;
    }

    /**
     * 파라미터 타입이 레퍼런스 타입일 경우, 빈 목록 반환
     * 아닐 경우, 파라미터 타입이 가지고 있는 field 들에 대해서 정보를 수집하여 목록 반환
     * @param parameter 파라미터
     * @return 빈 목록 / field 들에 대해서 정보 목록
     */
    private List<GetFieldInvoker> parseFieldInvoker(Parameter parameter) {
        if ( !this.isReferenceType() ) return Collections.emptyList();
        List<GetFieldInvoker> result = new ArrayList<>();
        List<Field> fields = RestClientReflectorUtils.filterLocalFields(parameter.getType());
        for (Field field : fields) {
            result.add(
                    new GetFieldInvoker(field)
            );
        }
        return result;
    }
}
