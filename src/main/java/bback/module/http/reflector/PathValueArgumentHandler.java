package bback.module.http.reflector;

import java.util.Optional;

class PathValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public PathValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        arg.ifPresent(o -> preset.set(this.metadata.getParamName(), String.valueOf(o)));
    }

    @Override
    public boolean isHeaderHandler() {
        return false;
    }

    @Override
    public boolean isPathHandler() {
        return true;
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
