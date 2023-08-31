package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientCommonException;
import bback.module.http.helper.LogHelper;
import bback.module.http.wrapper.RestResponse;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class RequestReturnMetadata {
    private static final LogHelper LOGGER = LogHelper.of(RequestReturnMetadata.class);
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

    public boolean isWrap() {
        return isWrapList() || isWrapRestResponse() || isDoubleWrap() || isWrapMap() || isWrapOptional();
    }

    public boolean isWrapList() {
        return this.isWrapList(this.returnClass);
    }

    public boolean isWrapList(Class<?> clazz) {
        if ( clazz == null ) return false;
        return Arrays.asList(clazz.getInterfaces()).contains(List.class) || clazz.isAssignableFrom(List.class);
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

    private ParameterizedType getParameterType(Type returnType) {
        return returnType instanceof ParameterizedType ? (ParameterizedType) returnType : null;
    }
}
