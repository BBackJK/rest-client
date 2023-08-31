package bback.module.http.reflector;

import bback.module.http.interfaces.ResponseMapper;
import bback.module.http.util.RestClientClassUtils;
import bback.module.http.util.RestClientObjectUtils;
import bback.module.http.wrapper.ResponseMetadata;
import bback.module.http.wrapper.RestResponse;

import java.util.Map;
import java.util.Optional;

public class ReturnValueResolver {
    private final RequestMethodMetadata restClientMethod;
    private final ResponseMapper dataMapper;

    public ReturnValueResolver(RequestMethodMetadata restClientMethod, ResponseMapper dataMapper) {
        this.restClientMethod = restClientMethod;
        this.dataMapper = dataMapper;
    }

    // TODO: Refactoring
    public Object resolve(ResponseMetadata response) {
        if ( response == null ) {
            return null;
        }

        Object result = null;
        String responseValue = response.getStringResponse();
        Class<?> returnRawType = this.restClientMethod.getRawType();

        if (RestClientObjectUtils.isEmpty(responseValue)) {
            result = RestClientClassUtils.getTypeInitValue(returnRawType);
        } else if (response.isXml()) {
            result = this.dataMapper.toXml(responseValue, returnRawType);
        } else {
            if (this.restClientMethod.isReturnWrap()) {
                if (this.restClientMethod.isResultWrapper()) {
                    if (this.restClientMethod.isDoubleWrap()) {
                        result = this.dataMapper.convert(responseValue, this.restClientMethod.getSecondRawType(), returnRawType);
                    } else {
                        result = this.dataMapper.convert(responseValue, returnRawType);
                    }
                } else if (this.restClientMethod.isReturnMap()) {
                    result = this.dataMapper.convert(responseValue, Map.class);
                } else {
                    result = this.dataMapper.convert(responseValue, this.restClientMethod.getSecondRawType(), returnRawType);
                }
            } else {
                if (this.restClientMethod.isReturnString()) {
                    result = responseValue;
                } else if (!this.restClientMethod.isReturnVoid()) {
                    result = this.dataMapper.convert(responseValue, returnRawType);
                }
            }
        }

        // wrapping 해주는 return value 들
        if (restClientMethod.isReturnRestResponse()) {
            result = response.isSuccess()
                    ? RestResponse.success(result, response.getHttpCode())
                    : RestResponse.fail(response.getHttpCode(), response.getFailMessage());
        } else if (restClientMethod.isReturnOptional()) {
            result = Optional.ofNullable(result);
        }

        return result;
    }

}
