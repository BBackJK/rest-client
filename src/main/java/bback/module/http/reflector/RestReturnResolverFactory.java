package bback.module.http.reflector;

import bback.module.http.interfaces.ResponseMapper;

public final class RestReturnResolverFactory {

    private RestReturnResolverFactory() {
        throw new UnsupportedOperationException("This is a factory class and cannot be instantiated");
    }

    public static RestReturnResolver getResolver(RequestMethodMetadata restClientMethod, ResponseMapper dataMapper) {

        if (restClientMethod.isReturnResultWrap()) {
            if (restClientMethod.isReturnRestResponse()) {
                return new RestResponseReturnResolver(
                        dataMapper
                        , restClientMethod.getActualType()
                        , restClientMethod.isOverWrap() ? restClientMethod.getActualWrapperType() : null
                );
            } else if (restClientMethod.isReturnOptional()) {
                return new OptionalReturnResolver(
                        dataMapper
                        , restClientMethod.getActualType()
                        , restClientMethod.isOverWrap() ? restClientMethod.getActualWrapperType() : null
                );
            } else {
                return new CompletableFutureReturnResolver(
                        dataMapper
                        , restClientMethod.getActualType()
                        , restClientMethod.isOverWrap() ? restClientMethod.getActualWrapperType() : null
                );
            }
        } else {
            return new CommonReturnResolver(dataMapper, restClientMethod.getActualType(), restClientMethod.getActualWrapperType());
        }
    }
}
