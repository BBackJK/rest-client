package bback.module.http.reflector;

import java.util.Optional;

class BodyDataArgumentHandler implements ParameterArgumentHandler {

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        preset.set("", null);   // 초기화
        arg.ifPresent(o -> preset.set("", o));
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
        return true;
    }
}
