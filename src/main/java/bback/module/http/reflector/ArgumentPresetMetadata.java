package bback.module.http.reflector;

public interface ArgumentPresetMetadata<T> {

    T get();
    T set(String paramName, Object value);
}
