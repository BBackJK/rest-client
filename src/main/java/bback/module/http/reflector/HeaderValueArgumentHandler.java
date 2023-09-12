package bback.module.http.reflector;

import java.util.Optional;

class HeaderValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public HeaderValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        arg.ifPresent(o -> preset.set(this.metadata.getParamName(), String.valueOf(o)));
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
