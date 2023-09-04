package bback.module.http.reflector;

import java.util.List;

final class ParameterArgumentHandlerFactory {

    private ParameterArgumentHandlerFactory() {
        throw new UnsupportedOperationException("This is a factory class and cannot be instantiated");
    }

    public static ParameterArgumentHandler getHandler(RequestParamMetadata metadata, boolean isOnlyRequestParam, boolean isEmptyAllAnnotation, List<String> pathValueNames) {
        if (metadata.isAnnotationRequestHeader()) {
            return new HeaderValueArgumentHandler(metadata);
        } else if (metadata.isAnnotationPathVariable()) {
            return new PathValueArgumentHandler(metadata);
        } else if (metadata.canRequestParam(isOnlyRequestParam, isEmptyAllAnnotation, pathValueNames)) {
            return new QueryValueArgumentHandler(metadata);
        } else if (metadata.isRestCallback()) {
            return new RestCallbackArgumentHandler();
        } else if (metadata.isAnnotationAuthorization()) {
            return new HeaderAuthorizationArgumentHandler(metadata);
        } else {
            return new BodyDataArgumentHandler();
        }
    }
}
