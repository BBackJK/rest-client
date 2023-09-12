package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientCallException;
import bback.module.http.helper.LogHelper;
import bback.module.http.util.RestClientMapUtils;
import bback.module.http.util.RestClientReflectorUtils;
import bback.module.http.wrapper.RequestMetadata;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestMethodMetadata {
    private static final List<Class<? extends Annotation>> ALLOWED_REQUEST_MAPPING_ANNOTATIONS = Collections.unmodifiableList(Stream.of(RequestMapping.class, GetMapping.class, PostMapping.class, PatchMapping.class, PutMapping.class, DeleteMapping.class).collect(Collectors.toList()));
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{[a-z|0-9]+}");
    private static final LogHelper LOGGER = LogHelper.of(RequestMethodMetadata.class);

    /**
     * Method 의 Return 정보
     */
    private final RequestReturnMetadata requestReturnMetadata;

    /**
     * Method 의 Parameter 정보를 순서대로 Mapping
     * @Key argument 순서
     * @Value request parameter metadata
     */
    private final Map<Integer, RequestParamMetadata> sortedParameterMetadata;

    /**
     * Method 의 Parameter 정보에 대한 Argument Handler 를 순서대로 Mapping
     * @Key argument 순서
     * @Value request parameter argument handler
     */
    private final Map<Integer, ParameterArgumentHandler> sortedParameterArgumentHandlers;

    /**
     * Method 의 Parameter 들이 어노테이션들이 하나도 없는지.
     */
    private final boolean emptyAllParameterAnnotation;

    /**
     * Method 의 Parameter 들 중 @RequestParam 어노테이션을 가지고 있는지 여부
     */
    private final boolean hasRequestParamAnnotation;

    /**
     * RestCallback 인 Argument 목록
     */
    private final List<RequestParamMetadata> restCallbackParameters;

    /**
     * Method 가 가진 @xxMapping 의 Request Method 정보.
     */
    private final RequestMethod requestMethod;

    /**
     * Method 가 가진 @xxMapping 의 PathName 정보 (sub url)
     */
    private final String pathname;

    /**
     * Method 가 가진 @xxMapping 의 consumes 정보를 파싱한 content-type
     */
    private final MediaType contentType;

    /**
     * PATH_VARIABLE_PATTERN 으로 찾은 argument name 목록
     */
    private final List<String> pathValueNames;

    /**
     * Map 형태의 header value 정보 preset.
     */
    private final ArgumentPresetMetadata<Map<String, String>> headerValuePreset = new MapArgumentPreset();

    /**
     * Map 형태의 path value 정보 preset.
     */
    private final ArgumentPresetMetadata<Map<String, String>> pathValuePreset = new MapArgumentPreset();

    /**
     * Map 형태의 query value 정보 preset.
     */
    private final ArgumentPresetMetadata<Map<String, String>> queryValuePreset = new MapArgumentPreset();

    /**
     * Object 형태의 body data 정보 preset.
     */
    private final ArgumentPresetMetadata<Object> bodyValuePreset = new ObjectPreset();

    public RequestMethodMetadata(Method method) {
        Annotation mappingAnnotation = this.parseAnnotation(method);
        this.requestReturnMetadata = new RequestReturnMetadata(method);
        this.sortedParameterMetadata = RestClientMapUtils.toReadonly(this.getParamMetadataList(method.getParameters()));
        this.emptyAllParameterAnnotation = method.getParameterAnnotations().length == 0;
        this.hasRequestParamAnnotation = this.sortedParameterMetadata.values().stream().anyMatch(RequestParamMetadata::isAnnotationRequestParam);
        this.restCallbackParameters = Collections.unmodifiableList(this.sortedParameterMetadata.values().stream().filter(RequestParamMetadata::isRestCallback).collect(Collectors.toList()));
        this.requestMethod = this.parseRequestMethodByAnnotation(mappingAnnotation);
        this.pathname = this.parsePathNameByAnnotation(mappingAnnotation);
        this.contentType = this.parseContentTypeByAnnotation(mappingAnnotation);
        this.pathValueNames = this.getPathVariableNames(this.pathname);
        this.sortedParameterArgumentHandlers = this.sortedParameterMetadata
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey
                        , entry -> ParameterArgumentHandlerFactory.getHandler(entry.getValue(), this.isOnlyRequestParam(), emptyAllParameterAnnotation, pathValueNames)
                ));
    }

    /**
     * Argument 를 기반으로 Parameter Metadata 를 이용하여 RequestMetadata 생성.
     * @param args Method Argument 배열
     * @param origin @RestClient 어노테이션의 url (origin)
     * @param restClientLogger http 를 호출하거나 응답이 올 때 로깅을 위한 logger
     * @return ParameterArgumentHandler 를 기반으로 Preset 데이터를 채워서 생성된 RequestMetadata
     */
    public RequestMetadata applyArgs(Object[] args, String origin, LogHelper restClientLogger) {
        if ( args == null || args.length == 0 ) {
            return RequestMetadata.of(this.makeRequestUrl(origin, this.pathname), this.contentType, restClientLogger);
        }

        int argCount = args.length;
        if ( argCount != this.sortedParameterArgumentHandlers.size() ) {
            throw new RestClientCallException();
        }

        int bodyCount = 0;
        for (int i=0; i<argCount; i++) {
            Optional<Object> arg = Optional.ofNullable(args[i]);
            ParameterArgumentHandler handler = this.sortedParameterArgumentHandlers.get(i);
            if ( handler != null ) {
                Class<? extends ParameterArgumentHandler> handlerType = handler.getClass();
                if (handlerType.equals(HeaderValueArgumentHandler.class) || handlerType.equals(HeaderAuthorizationArgumentHandler.class)) {
                    handler.handle(headerValuePreset, arg);
                } else if (handlerType.equals(PathValueArgumentHandler.class)) {
                    handler.handle(pathValuePreset, arg);
                } else if (handlerType.equals(QueryValueArgumentHandler.class)) {
                    handler.handle(queryValuePreset, arg);
                } else if (handlerType.equals(BodyDataArgumentHandler.class)) {
                    bodyCount++;
                    handler.handle(bodyValuePreset, arg);
                }
            }
        }

        if ( bodyCount > 1 ) {
            LOGGER.warn("Request Body 로 인식되는 파라미터가 1개 이상입니다.");
        }

        return RequestMetadata.of(
                this.makeRequestUrl(origin, this.pathname)
                , this.contentType
                , this.headerValuePreset.get()
                , this.pathValuePreset.get()
                , this.queryValuePreset.get()
                , this.bodyValuePreset.get()
                , args
                , restClientLogger);
    }

    /**
     * RequestMethod 가 Post / Put / Patch 인지 판단.
     * @return RequestBody 를 가질 수 있는 RequestMethod 어노테이션인지 여부
     */
    public boolean isCanHasRequestBodyAnnotation() {
        return this.requestMethod == RequestMethod.POST ||
                this.requestMethod == RequestMethod.PUT ||
                this.requestMethod == RequestMethod.PATCH;
    }

    /**
     * Parameter 중 RestCallback 이 있는지 여부 판단.
     * @return Parameter 중 RestCallback 이 있는지 여부
     */
    public boolean hasRestCallback() {
        return !this.restCallbackParameters.isEmpty();
    }

    /**
     * pathname 의 path value 가 존재하는지 판단.
     * @return pathname 의 path value 로 판단 될만한 수식이 있는지 여부
     */
    public boolean isHasPathValue() {
        return !pathValueNames.isEmpty();
    }

    /**
     * RequestReturnMetadata 의 결과를 Wrapping 하는 여부를 그대로 반환.
     * RequestReturnMetadata 를 public class 로 만들고 싶지 않아서
     * @return 결과를 Wrapping 하는 Return 여부
     */
    public boolean isReturnResultWrap() {
        return this.requestReturnMetadata.isResultWrap();
    }

    /**
     * RequestReturnMetadata 의 Return 타입이 Optional 여부를 그대로 반환.
     * RequestReturnMetadata 를 public class 로 만들고 싶지 않아서
     * @return Return type 이 Optional 인지 여부
     */
    public boolean isReturnOptional() {
        return this.requestReturnMetadata.isWrapOptional();
    }

    /**
     * RequestReturnMetadata 의 Return 타입이 RestResponse 여부를 그대로 반환.
     * RequestReturnMetadata 를 public class 로 만들고 싶지 않아서
     * @return Return type 이 RestResponse 인지 여부
     */
    public boolean isReturnRestResponse() {
        return this.requestReturnMetadata.isWrapRestResponse();
    }

    /**
     * RequestReturnMetadata 의 Return 타입이 2번이상 Wrapping 되었는지 여부 판단.
     * RequestReturnMetadata 를 public class 로 만들고 싶지 않아서
     * @return 2번이상 Wrapping 되어있는지 여부
     */
    public boolean isOverWrap() {
        return this.requestReturnMetadata.isOverWrap();
    }

    /**
     * RequestReturnMetadata 의 Return Actual Type 을 Wrapping 한 raw type 을 반환
     * RequestReturnMetadata 를 public class 로 만들고 싶지 않아서
     * @return Return Generic Type 을 감싸고 있는 Wrapping Type
     */
    @Nullable
    public Class<?> getActualWrapperType() {
        return this.requestReturnMetadata.getActualWrapperType();
    }

    /**
     * RequestReturnMetadata 의 Return Actual Type 을 반환
     * RequestReturnMetadata 를 public class 로 만들고 싶지 않아서
     * @return Return Generic Type
     */
    public Class<?> getActualType() {
        return this.requestReturnMetadata.getActualType();
    }

    /**
     * sortParameterMetadata 의 Getter 메소드
     * @return sortParameterMetadata
     */
    public Map<Integer, RequestParamMetadata> getSortParameterMetadata() {
        return this.sortedParameterMetadata;
    }

    /**
     * emptyAllParameterAnnotation 의 Getter 메소드
     * @return emptyAllParameterAnnotation
     */
    public boolean isEmptyAllParameterAnnotation() {
        return this.emptyAllParameterAnnotation;
    }

    /**
     * emptyAllParameterAnnotation 의 Getter 메소드
     * @return emptyAllParameterAnnotation
     */
    public List<String> getPathValueNames() {
        return this.pathValueNames;
    }

    /**
     * hasRequestParamAnnotation 의 Getter 메소드
     * @return hasRequestParamAnnotation
     */
    public boolean hasRequestParamAnnotation() {
        return this.hasRequestParamAnnotation;
    }

    /**
     * requestMethod 의 Getter 메소드
     * @return requestMethod
     */
    public RequestMethod getRequestMethod() {
        return this.requestMethod;
    }

    /**
     * xxMapping 어노테이션에서 파싱한 pathname 에 대해서 PATH_VARIABLE_PATTERN 에 매칭되는 argument name 을 List 로 만들어서 반환.
     * @param pathname xxMapping 어노테이션에서 파싱한 pathname
     * @return pathname 중 PATH_VARIABLE_PATTERN 에 매칭되는 argument name 목록
     */
    @NonNull
    private List<String> getPathVariableNames(String pathname) {
        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pathname);
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            String values = matcher.group(); // "{values}"
            result.add(values.substring(1, values.length()-1)); // "values"
        }
        return result;
    }

    /**
     * Method 의 Parameter 목록을 기반으로 Parameter 에 대한 정보를 가지고 있는 RequestParamMetadata 를 순서와 함께 Mapping 하여 반환
     * @param parameters Rest Client Method 의 Parameters
     * @return RequestParamMetadata 를 순서와 Parameter 에 대한 정보 Map
     */
    @NonNull
    private Map<Integer, RequestParamMetadata> getParamMetadataList(Parameter[] parameters) {
        if ( parameters == null ) {
            return Collections.emptyMap();
        }
        Map<Integer, RequestParamMetadata> result = new LinkedHashMap<>();
        int paramCount = parameters.length;
        for (int i=0; i<paramCount;i++) {
            result.put(i, new RequestParamMetadata(parameters[i]));
        }
        return result;
    }

    /**
     * ALLOWED_REQUEST_MAPPING_ANNOTATIONS 어노테이션 중 매핑되는 어노테이션 반환
     * @param method Rest Client Method
     * @return ALLOWED_REQUEST_MAPPING_ANNOTATIONS 어노테이션 중 매핑되는 어노테이션
     */
    @Nullable
    private Annotation parseAnnotation(Method method) {
        for (Class<? extends Annotation> a :  ALLOWED_REQUEST_MAPPING_ANNOTATIONS) {
            Annotation anno = method.getAnnotation(a);
            if ( anno != null ) {
                return anno;
            }
        }
        return null;
    }

    /**
     * Annotation 에서 Request Method 파싱
     * default value :: GET
     * @param annotation ALLOWED_REQUEST_MAPPING_ANNOTATIONS 어노테이션 중 매핑되는 어노테이션
     * @return RequestMethod (GET, POST, PATCH, PUT, DELETE)
     */
    @NonNull
    private RequestMethod parseRequestMethodByAnnotation(@Nullable Annotation annotation) {
        RequestMethod rm = RequestMethod.GET;
        if ( annotation == null ) {
            return rm;
        }

        Class<? extends Annotation> mappingAnnotationClazz = annotation.annotationType();

        if ( RequestMapping.class.equals(mappingAnnotationClazz)) {
            RequestMethod[] requestMethods = (RequestMethod[]) RestClientReflectorUtils.annotationMethodInvoke(annotation, "method");
            if ( requestMethods != null && requestMethods.length > 0) {
                rm = requestMethods[0];
            }
        } else if ( PostMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.POST;
        } else if ( PatchMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.PATCH;
        } else if ( PutMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.PUT;
        } else if ( DeleteMapping.class.equals(mappingAnnotationClazz) ) {
            rm = RequestMethod.DELETE;
        }

        return rm;
    }

    /**
     * Annotation 에서 pathname 파싱
     * default value :: ""
     * @param annotation ALLOWED_REQUEST_MAPPING_ANNOTATIONS 어노테이션 중 매핑되는 어노테이션
     * @return 호출할 pathname
     */
    @NonNull
    private String parsePathNameByAnnotation(@Nullable Annotation annotation) {
        String url = "";
        if ( annotation == null ) {
            return url;
        }
        String[] urlValues = (String[]) RestClientReflectorUtils.annotationMethodInvoke(annotation, "value");
        return urlValues != null && urlValues.length > 0 ? urlValues[0] : url;
    }

    /**
     * Annotation 에서 consumes 를 파싱하여 MediaType 반환
     * default Value :: application/json
     * @param annotation ALLOWED_REQUEST_MAPPING_ANNOTATIONS 어노테이션 중 매핑되는 어노테이션
     * @return MediaType
     */
    @NonNull
    private MediaType parseContentTypeByAnnotation(@Nullable Annotation annotation) {
        MediaType defaultContentType = MediaType.APPLICATION_JSON;
        if ( annotation == null ) {
            return defaultContentType;
        }
        String[] contentTypeValues = (String[]) RestClientReflectorUtils.annotationMethodInvoke(annotation, "consumes");
        if ( contentTypeValues == null || contentTypeValues.length < 1 ) {
            return defaultContentType;
        }

        String firstContentType = contentTypeValues[0];
        String[] contentTypeSplit = firstContentType.split("/");
        try {
            return new MediaType(contentTypeSplit[0], contentTypeSplit[1]);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LOGGER.warn("Annotation 으로부터 contentType 을 파싱하다 실패하였습니다. 원인 : \n" + e.getMessage());
            return defaultContentType;
        }
    }

    /**
     * RestClient 어노테이션의 url (origin) 과 Annotation 에서 파싱한 pathname 으로 full url 을 만든다.
     * @param origin RestClient 어노테이션의 url
     * @param pathname Annotation 에서 파싱한 pathname
     * @return 호출할 Full Url
     */
    @NonNull
    private String makeRequestUrl(String origin, String pathname) {
        String copyOrigin = origin == null ? "" : origin;
        String copyPathname = pathname == null ? "" : pathname;

        if (copyOrigin.isEmpty() && copyPathname.isEmpty()) {
            throw new RestClientCallException("호출할 URL 이 존재하지 않습니다");
        }

        if (!copyOrigin.isEmpty()) {
            if (copyOrigin.endsWith("/")) {
                copyOrigin = copyOrigin.substring(copyOrigin.length() - 1);
            }

            if (!copyPathname.startsWith("/")) {
                copyPathname = String.format("/%s", copyPathname);
            }
        }

        return copyOrigin + copyPathname;
    }

    /**
     * Method 의 Parameter 들이 모두 RequestParam 어노테이션을 가지고 있는지 판단
     * @return Method 의 Parameter 들이 모두 RequestParam 어노테이션을 가지고 있는지 여부
     */
    public boolean isOnlyRequestParam() {
        boolean isFormContent = MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(this.contentType);
        return (this.isCanHasRequestBodyAnnotation() && isFormContent) || this.hasRequestParamAnnotation;
    }
}
