package bback.module.http.helper;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.Optional;

public class GetFieldInvoker {

    @NonNull
    private final Field field;

    public GetFieldInvoker(
            @NonNull Field field
    ) {
        this.field = field;
    }

    @Nullable
    public Object invoke(Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return null;
        }
    }

    public Optional<Object> invokeWrapper(Object target) {
        return Optional.ofNullable(this.invoke(target));
    }

    public String getFieldName() {
        return this.field.getName();
    }
}
