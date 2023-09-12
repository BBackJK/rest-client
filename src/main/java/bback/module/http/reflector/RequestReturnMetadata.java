package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientCommonException;
import bback.module.http.wrapper.RestResponse;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class RequestReturnMetadata {
    private final Class<?> returnRawType;       // return raw type ex) String methodA() -> String.class, List<String> methodB() -> List.class
    private final Class<?> actualType;          // real generic type ex) String methodA() -> String.class, List<String> methodB() -> String.class
    @Nullable
    private final Class<?> actualWrapperType;   // generic 2 depth wrapper type ex) String methodA() -> null, List<String> methodB() -> null, RestResponse<Optional<String>> methodC() -> Optional.class

    public RequestReturnMetadata(Method method) {
        this.returnRawType = method.getReturnType();
        ParameterizedType parameterizedType = this.getParameterType(method.getGenericReturnType());
        if ( parameterizedType != null ) {
            Type actualTypeClass = parameterizedType.getActualTypeArguments()[0];
            ParameterizedType overParameterType = this.getParameterType(actualTypeClass);
            if ( overParameterType != null ) {
                Type overActualType = overParameterType.getActualTypeArguments()[0];
                if ( overActualType instanceof ParameterizedType ) {
                    throw new RestClientCommonException(" Return 타입으로는 3번 이상 Wrapping 할 수 없습니다. (최대 2개 지원). ");
                }
                this.actualType = (Class<?>) overActualType;
                this.actualWrapperType = (Class<?>) overParameterType.getRawType();
            } else {
                this.actualType = (Class<?>) actualTypeClass;
                this.actualWrapperType = this.returnRawType;
            }
        } else {
            this.actualType = this.returnRawType;
            this.actualWrapperType = null;
        }
    }

    /**
     * 결과를 Wrapping 하는 Return 타입인지
     * @return 결과를 Wrapping 하는 Return 여부
     */
    public boolean isResultWrap() {
        return isWrapRestResponse() || isWrapOptional() || isWrapCompletableFuture();
    }

    /**
     * Return type 이 Optional 인지 여부를 판단.
     * @return Return type 이 Optional 인지 여부
     */
    public boolean isWrapOptional() {
        return Optional.class.equals(this.returnRawType);
    }

    /**
     * Return type 이 RestResponse 인지 여부를 판단.
     * @return Return type 이 RestResponse 인지 여부
     */
    public boolean isWrapRestResponse() {
        return RestResponse.class.equals(this.returnRawType);
    }

    /**
     * Return type 이 CompletableFuture 인지 여부를 판단.
     * @return Return type 이 CompletableFuture 인지 여부
     */
    public boolean isWrapCompletableFuture() {
        return CompletableFuture.class.equals(this.returnRawType);
    }

    /**
     * 2번이상 Wrapping 되어있는지 여부를 판단.
     * @return 2번이상 Wrapping 되어있는지 여부
     */
    public boolean isOverWrap() {
        return this.actualWrapperType != null && !this.actualWrapperType.equals(this.returnRawType);
    }

    /**
     * 실제 Return Generic Type 을 반환
     * @return Return Generic Type
     */
    public Class<?> getActualType() {
        return this.actualType;
    }

    /**
     * 실제 Return Generic Type 을 감싸고 있는 Wrapping Type 반환
     * @return Return Generic Type 을 감싸고 있는 Wrapping Type
     */
    @Nullable
    public Class<?> getActualWrapperType() {
        return this.actualWrapperType;
    }

    private ParameterizedType getParameterType(Type returnType) {
        return returnType instanceof ParameterizedType ? (ParameterizedType) returnType : null;
    }
}
