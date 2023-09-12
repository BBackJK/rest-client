package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientCommonException;
import bback.module.http.util.RestClientClassUtils;

import java.lang.reflect.Method;
import java.util.Map;

public class RestClientMethodValidator {

    private final RequestMethodMetadata requestMethodMetadata;
    private final String errorContext;

    public RestClientMethodValidator(
            RequestMethodMetadata requestMethodMetadata
            , Method method
    ) {
        this.requestMethodMetadata = requestMethodMetadata;
        this.errorContext = this.getErrorContext(method);
    }

    public void valid() throws RestClientCommonException {
        Map<Integer, RequestParamMetadata> sortRequestParameterMetadata = this.requestMethodMetadata.getSortParameterMetadata();
        // List Parameter 가 있는지 확인
        this.validListArgument(sortRequestParameterMetadata);
        // Request Body 수 확인
        this.validRequestBodyCount(sortRequestParameterMetadata);
        // 리턴 타입 기본 생성자 존재 유무 확인
        this.validHasReturnTypeNoArgsConstructor();
        // RestResponse 와 RestCallback 은 같이 사용 X
        this.validHasRestResponseWithRestCallback();
    }

    private void validListArgument(Map<Integer, RequestParamMetadata> sortRequestParameterMetadata) {
        boolean hasListParameter = sortRequestParameterMetadata.values().stream().anyMatch(RequestParamMetadata::isListType);
        if ( hasListParameter ) {
            throw new RestClientCommonException(String.format("[%s] RestClient 는 List 타입의 파라미터를 지원하지 않습니다.", this.errorContext));
        }
    }

    private void validRequestBodyCount(Map<Integer, RequestParamMetadata> sortRequestParameterMetadata) {
        long requestBodyCount = sortRequestParameterMetadata.values().stream().filter(p -> {
            boolean isRequestHeader = p.isAnnotationRequestHeader();
            boolean isHeaderAuthorization = p.isAnnotationAuthorization();
            boolean isPathVariable = p.isAnnotationPathVariable();
            boolean canRequestParam = p.canRequestParam(
                    this.requestMethodMetadata.isOnlyRequestParam()
                    , this.requestMethodMetadata.isEmptyAllParameterAnnotation()
                    , this.requestMethodMetadata.getPathValueNames()
            );
            boolean isRestCallback = p.isRestCallback();
            return !isRequestHeader && !isPathVariable && !canRequestParam && !isRestCallback && !isHeaderAuthorization;
        }).count();

        // GET, DELETE 인데 requestBody 로 판단되는 파라미터가 1개 이상이라면..
        if ( !this.requestMethodMetadata.isCanHasRequestBodyAnnotation() && requestBodyCount > 0 ) {
            StringBuilder errMessage = new StringBuilder(String.format("[%s] RequestBody 를 가질 수 없는 Method 입니다.", errorContext));
            if ( this.requestMethodMetadata.isHasPathValue() ) {
                errMessage.append("\n");
                errMessage.append(" - PathVariable 을 Url 에 선언한 경우, @PathVariable 어노테이션은 필수 입니다.");
            }
            if ( this.requestMethodMetadata.hasRequestParamAnnotation() ) {
                errMessage.append("\n");
                errMessage.append(" - RequestParam 어노테이션을 선언한 경우, Get Method 혹은 Delete 메소드는 선언되어 있지 않은 파라미터에 대해서 RequestBody 로 취급합니다.");
            }
            throw new RestClientCommonException(errMessage.toString());
        }

        // POST, PUT, DELETE 인데 RequestBody 로 판단되는 파라미터가 1개가 아니라면..
        if ( this.requestMethodMetadata.isCanHasRequestBodyAnnotation() && ( requestBodyCount != 1 )) {
            throw new RestClientCommonException(String.format("[%s] POST, PUT, PATCH RequestMethod 는 MediaType 이 application/json 일 경우, RequestBody 1개는 필수 이여야 합니다. RequestBody 수 %d", errorContext, requestBodyCount));
        }
    }

    private void validHasReturnTypeNoArgsConstructor() {
        Class<?> returnActualType = this.requestMethodMetadata.getActualType();
        if ( !returnActualType.isInterface() && !RestClientClassUtils.isVoid(returnActualType) && !RestClientClassUtils.isPrimitiveOrString(returnActualType) ) {
            try {
                returnActualType.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new RestClientCommonException(String.format("[%s] 리턴 타입 %s 는 기본 생성자가 있어야 ResponseMapper 로 변환 가능 합니다.", errorContext, returnActualType.getSimpleName()));
            }
        }
    }

    private void validHasRestResponseWithRestCallback() {
        boolean isReturnRestResponse = this.requestMethodMetadata.isReturnRestResponse();
        boolean hasRestCallback = this.requestMethodMetadata.hasRestCallback();
        if ( isReturnRestResponse && hasRestCallback ) {
            throw new RestClientCommonException(String.format("[%s] RestClient 는 Return RestResponse 와 Argument RestCallback 의 중복 사용을 지원하지 않습니다.", errorContext));
        }
    }

    private String getErrorContext(Method method) {
        Class<?> restClientInterface = method.getDeclaringClass();
        return String.format("%s#%s", restClientInterface.getSimpleName(), method.getName());
    }
}
