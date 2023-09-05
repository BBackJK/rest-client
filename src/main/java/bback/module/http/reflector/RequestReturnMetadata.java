package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientCommonException;
import bback.module.http.helper.LogHelper;
import bback.module.http.wrapper.RestResponse;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

class RequestReturnMetadata {
//    private static final LogHelper LOGGER = LogHelper.of(RequestReturnMetadata.class);
    private final Class<?> returnClass;
    private final Class<?> rawType;

    @Nullable
    private final Class<?> secondRawType;

    public RequestReturnMetadata(Method method) {
        this.returnClass = method.getReturnType();
        ParameterizedType parameterizedType = this.getParameterType(method.getGenericReturnType());
        if ( parameterizedType != null ) {
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            ParameterizedType overParameterType = this.getParameterType(actualType);
            if ( overParameterType != null ) {
                Type overActualType = overParameterType.getActualTypeArguments()[0];
                if ( overActualType instanceof ParameterizedType ) {
                    throw new RestClientCommonException(" Return 타입으로는 3번 이상 Wrapping 할 수 없습니다. (최대 2개 지원). ");
                }
                this.rawType = (Class<?>) overActualType;
                this.secondRawType = (Class<?>) overParameterType.getRawType();
            } else {
                this.rawType = (Class<?>) actualType;
                this.secondRawType = this.returnClass;
            }
        } else {
            this.rawType = this.returnClass;
            this.secondRawType = null;
        }
    }

    public boolean isObjectWrap() {
        return isWrapList() || isWrapMap() || isDoubleWrap();
    }

    public boolean isResultWrap() {
        return isWrapRestResponse() || isWrapOptional() || isWrapCompletableFuture();
    }

    public boolean isWrapList() {
        return Arrays.asList(this.returnClass.getInterfaces()).contains(List.class) || this.returnClass.isAssignableFrom(List.class);
    }

    public boolean isWrapMap() {
        return Arrays.asList(this.returnClass.getInterfaces()).contains(Map.class) || this.returnClass.isAssignableFrom(Map.class);
    }

    public boolean isVoid() {
        return void.class.equals(this.returnClass) || Void.class.equals(this.returnClass);
    }

    public boolean isWrapOptional() {
        return Optional.class.equals(this.returnClass);
    }

    public boolean isString() {
        return String.class.equals(this.returnClass);
    }

    public boolean isWrapRestResponse() {
        return RestResponse.class.equals(this.returnClass);
    }

    public boolean isWrapCompletableFuture() {
        return CompletableFuture.class.equals(this.returnClass);
    }

    public boolean isDoubleWrap() {
        return this.secondRawType != null && !this.secondRawType.equals(this.returnClass);
    }

    public Class<?> getRawType() {
        return this.rawType;
    }

    @Nullable
    public Class<?> getSecondRawType() {
        return this.secondRawType;
    }

    public boolean isSecondWrapList() {
        if ( this.getSecondRawType() == null ) return false;
        return Arrays.asList(this.secondRawType.getInterfaces()).contains(List.class) || this.secondRawType.isAssignableFrom(List.class);
    }

    public boolean isSecondWrapMap() {
        if ( this.getSecondRawType() == null ) return false;
        return Arrays.asList(this.secondRawType.getInterfaces()).contains(Map.class) || this.secondRawType.isAssignableFrom(Map.class);
    }

    private ParameterizedType getParameterType(Type returnType) {
        return returnType instanceof ParameterizedType ? (ParameterizedType) returnType : null;
    }
}
