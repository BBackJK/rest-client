package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.wrapper.ResponseMetadata;

import java.util.Optional;

class OptionalReturnResolver extends AbstractRestReturnResolver {

    public OptionalReturnResolver(ResponseMapper responseMapper, Class<?> rawType, Class<?> rawWrapType) {
        super(responseMapper, rawType, rawWrapType);
    }

    @Override
    protected Object doWrapping(Object result, ResponseMetadata responseMetadata) throws RestClientDataMappingException {
        return Optional.ofNullable(result);
    }
}
