package bback.module.http.reflector;

import bback.module.http.exceptions.RestClientDataMappingException;
import bback.module.http.wrapper.ResponseMetadata;
import org.springframework.lang.NonNull;

public interface RestReturnResolver {

    Object resolve(@NonNull ResponseMetadata response) throws RestClientDataMappingException;
}
