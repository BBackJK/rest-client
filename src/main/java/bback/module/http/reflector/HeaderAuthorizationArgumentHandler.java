package bback.module.http.reflector;

import bback.module.http.annotations.Authorization;
import bback.module.http.enums.AuthorizationScheme;

import java.util.Optional;

class HeaderAuthorizationArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public HeaderAuthorizationArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        arg.ifPresent(o -> {
            Authorization authorization = (Authorization) this.metadata.getAnnotation();
            String type = authorization != null ? authorization.type() : AuthorizationScheme.BEARER.get();
            boolean onPrefix = authorization == null || authorization.onPrefix();
            preset.set(this.metadata.getParamName(), onPrefix ? String.format("%s %s", type, o) : String.valueOf(o));
        });
    }

    @Override
    public boolean isHeaderHandler() {
        return true;
    }

    @Override
    public boolean isPathHandler() {
        return false;
    }

    @Override
    public boolean isQueryHandler() {
        return false;
    }

    @Override
    public boolean isBodyHandler() {
        return false;
    }
}
