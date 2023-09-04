package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientCallException;
import bback.module.http.util.RestClientReflectorUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class QueryValueArgumentHandler implements ParameterArgumentHandler {

    private final RequestParamMetadata metadata;

    public QueryValueArgumentHandler(RequestParamMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void handle(ArgumentPresetMetadata<?> preset, Optional<Object> arg) {
        arg.ifPresent(o -> {
            boolean isCollection = o instanceof Collection;
            boolean isMap = o instanceof Map;

            if (isCollection) {
                throw new RestClientCallException("RestClient 는 List 타입의 파라미터는 지원하지 않습니다.");
            }

            if (isMap) {
                Map<?, ?> map = (Map<?, ?>) o;
                map.forEach((k, v) -> preset.set(String.valueOf(k), v == null ? null : String.valueOf(v)));
            } else if (this.metadata.isReferenceType()) {
                List<Field> fields = RestClientReflectorUtils.filterLocalFields(o.getClass());
                for (Field f : fields) {
                    f.setAccessible(true);
                    try {
                        Object v = f.get(o);
                        if (v != null) {
                            preset.set(f.getName(), String.valueOf(v));
                        }
                    }  catch (IllegalAccessException e) {
                        // ignore ...
                    }
                }
            } else {
                preset.set(metadata.getParamName(), String.valueOf(o));
            }
        });
    }
}
