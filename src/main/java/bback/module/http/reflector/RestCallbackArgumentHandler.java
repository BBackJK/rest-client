package bback.module.http.reflector;

import java.util.Optional;

class RestCallbackArgumentHandler implements ParameterArgumentHandler {

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        // ignore
    }

    @Override
    public boolean isHeaderHandler() {
        return false;
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
