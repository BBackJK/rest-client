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

    public boolean isResultWrap() {
        return isWrapRestResponse() || isWrapOptional() || isWrapCompletableFuture();
    }

    public boolean isVoid() {
        return void.class.equals(this.returnRawType) || Void.class.equals(this.returnRawType);
    }

    public boolean isWrapOptional() {
        return Optional.class.equals(this.returnRawType);
    }

    public boolean isString() {
        return String.class.equals(this.returnRawType);
    }

    public boolean isWrapRestResponse() {
        return RestResponse.class.equals(this.returnRawType);
    }

    public boolean isWrapCompletableFuture() {
        return CompletableFuture.class.equals(this.returnRawType);
    }

    public boolean isOverWrap() {
        return this.actualWrapperType != null && !this.actualWrapperType.equals(this.returnRawType);
    }

    public Class<?> getActualType() {
        return this.actualType;
    }

    @Nullable
    public Class<?> getActualWrapperType() {
        return this.actualWrapperType;
    }

    private ParameterizedType getParameterType(Type returnType) {
        return returnType instanceof ParameterizedType ? (ParameterizedType) returnType : null;
    }
}
