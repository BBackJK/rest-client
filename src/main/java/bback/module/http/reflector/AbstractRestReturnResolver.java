package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.util.RestClientClassUtils;
import bback.module.http.util.RestClientObjectUtils;
import bback.module.http.wrapper.ResponseMetadata;
import org.springframework.lang.Nullable;

abstract class AbstractRestReturnResolver implements RestReturnResolver {

    protected final ResponseMapper responseMapper;
    protected final Class<?> actualType;
    @Nullable
    protected final Class<?> actualWrapperType;

    protected AbstractRestReturnResolver(ResponseMapper responseMapper, Class<?> actualType, Class<?> actualWrapperType) {
        this.responseMapper = responseMapper;
        this.actualType = actualType;
        this.actualWrapperType = actualWrapperType;
    }

    protected abstract Object doWrapping(Object result, ResponseMetadata responseMetadata) throws RestClientDataMappingException;

    @Override
    public Object resolve(ResponseMetadata response) throws RestClientDataMappingException {
        Object result = null;
        String responseValue = response.getStringResponse();
        if ( RestClientObjectUtils.isEmpty(responseValue) ) {
            result = RestClientClassUtils.getTypeInitValue(this.actualType);
        } else if (response.isXml()) {
            result = this.responseMapper.toXml(responseValue, this.actualType);
        } else {
            if ( this.actualWrapperType != null ) {
                result = this.responseMapper.convert(responseValue, this.actualWrapperType, this.actualType);
            } else {
                if ( this.isReturnString() ) {
                    result = responseValue;
                } else if ( !this.isReturnVoid() ) {
                    result = this.responseMapper.convert(responseValue, this.actualType);
                }
            }
        }

        return doWrapping(result, response);
    }

    protected boolean isReturnString() {
        return String.class.equals(this.actualType);
    }

    protected boolean isReturnVoid() {
        return void.class.equals(this.actualType) || Void.class.equals(this.actualType);
    }
}
