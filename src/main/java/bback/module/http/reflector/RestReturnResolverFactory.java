package bback.module.http.reflector;

import bback.module.http.interfaces.ResponseMapper;

public final class RestReturnResolverFactory {

    private RestReturnResolverFactory() {
        throw new UnsupportedOperationException("This is a factory class and cannot be instantiated");
    }

    public static RestReturnResolver getResolver(RequestMethodMetadata requestMethodMetadata, ResponseMapper dataMapper) {

        if (requestMethodMetadata.isReturnResultWrap()) {
            if (requestMethodMetadata.isReturnRestResponse()) {
                return new RestResponseReturnResolver(
                        dataMapper
                        , requestMethodMetadata.getActualType()
                        , requestMethodMetadata.isOverWrap() ? requestMethodMetadata.getActualWrapperType() : null
                );
            } else if (requestMethodMetadata.isReturnOptional()) {
                return new OptionalReturnResolver(
                        dataMapper
                        , requestMethodMetadata.getActualType()
                        , requestMethodMetadata.isOverWrap() ? requestMethodMetadata.getActualWrapperType() : null
                );
            } else {
                return new CompletableFutureReturnResolver(
                        dataMapper
                        , requestMethodMetadata.getActualType()
                        , requestMethodMetadata.isOverWrap() ? requestMethodMetadata.getActualWrapperType() : null
                );
            }
        } else {
            return new CommonReturnResolver(dataMapper, requestMethodMetadata.getActualType(), requestMethodMetadata.getActualWrapperType());
        }
    }
}
