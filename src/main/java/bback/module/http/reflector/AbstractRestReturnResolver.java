package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.util.RestClientClassUtils;
import bback.module.http.util.RestClientObjectUtils;
import bback.module.http.wrapper.ResponseMetadata;
import org.springframework.lang.Nullable;

public abstract class AbstractRestReturnResolver implements RestReturnResolver {

    protected final ResponseMapper responseMapper;
    protected final Class<?> rawType;
    @Nullable
    protected final Class<?> rawWrapType;

    protected AbstractRestReturnResolver(ResponseMapper responseMapper, Class<?> rawType, Class<?> rawWrapType) {
        this.responseMapper = responseMapper;
        this.rawType = rawType;
        this.rawWrapType = rawWrapType;
    }

    protected abstract Object doWrapping(Object result, ResponseMetadata responseMetadata) throws RestClientDataMappingException;

    @Override
    public Object resolve(ResponseMetadata response) throws RestClientDataMappingException {
        Object result = null;
        String responseValue = response.getStringResponse();
        if ( RestClientObjectUtils.isEmpty(responseValue) ) {
            result = RestClientClassUtils.getTypeInitValue(this.rawType);
        } else if (response.isXml()) {
            result = this.responseMapper.toXml(responseValue, this.rawType);
        } else {
            if ( this.rawWrapType != null ) {
                result = this.responseMapper.convert(responseValue, this.rawWrapType, this.rawType);
            } else {
                result = this.responseMapper.convert(responseValue, this.rawType);
            }
        }

        return doWrapping(result, response);
    }
}
