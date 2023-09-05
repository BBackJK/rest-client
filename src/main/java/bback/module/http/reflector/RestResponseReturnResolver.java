package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.wrapper.ResponseMetadata;
import bback.module.http.wrapper.RestResponse;

public class RestResponseReturnResolver extends AbstractRestReturnResolver {

    public RestResponseReturnResolver(ResponseMapper responseMapper, Class<?> rawType, Class<?> rawWrapType) {
        super(responseMapper, rawType, rawWrapType);
    }

    @Override
    protected Object doWrapping(Object result, ResponseMetadata response) throws RestClientDataMappingException {
        return response.isSuccess()
                ? RestResponse.success(result, response.getHttpCode())
                : RestResponse.fail(response.getHttpCode(), response.getFailMessage());
    }
}
