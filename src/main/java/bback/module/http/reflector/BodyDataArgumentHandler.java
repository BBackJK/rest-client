package bback.module.http.reflector;

import java.util.Optional;

class BodyDataArgumentHandler implements ParameterArgumentHandler {

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        preset.set("", null);   // 초기화
        arg.ifPresent(o -> preset.set("", o));
    }
}
