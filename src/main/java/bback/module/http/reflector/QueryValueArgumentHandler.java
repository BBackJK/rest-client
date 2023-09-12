package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientCallException;
import bback.module.http.helper.GetFieldInvoker;

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
                List<GetFieldInvoker> fieldInvokerList = this.metadata.getFieldInvokerList();
                for (GetFieldInvoker invoker : fieldInvokerList) {
                    invoker.invokeWrapper(o).ifPresent(value -> preset.set(invoker.getFieldName(), String.valueOf(value)));
                }
            } else {
                preset.set(metadata.getParamName(), String.valueOf(o));
            }
        });
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
        return true;
    }

    @Override
    public boolean isBodyHandler() {
        return false;
    }
}
