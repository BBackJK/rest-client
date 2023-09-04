# Other Documents..

* [1_REST_CLIENT](https://github.com/BBackJK/rest-client/tree/main/document/1_REST_CLIENT.MD)
* [2_REQUEST](https://github.com/BBackJK/rest-client/tree/main/document/2_REQUEST.MD)
* [3_HEADER](https://github.com/BBackJK/rest-client/tree/main/document/3_HEADER.MD)
* [4_PATH_VARIABLE](https://github.com/BBackJK/rest-client/tree/main/document/4_PATH_VARIABLE.MD)
* [5_QUERY_PARAMETER](https://github.com/BBackJK/rest-client/tree/main/document/5_QUERY_PARAMETER.MD)
* [6_BODY_DATA](https://github.com/BBackJK/rest-client/tree/main/document/6_BODY_DATA.MD)
* [8_REST_CALLBACK](https://github.com/BBackJK/rest-client/tree/main/document/8_REST_CALLBACK.MD)
* [9_CONFIGURATION](https://github.com/BBackJK/rest-client/tree/main/document/9_CONFIGURATION.MD)
* [10_KAKAO_EXAMPLE](https://github.com/BBackJK/rest-client/tree/main/document/10_KAKAO_EXAMPLE.MD)

# RestResponse

Rest Client 모듈은 Response Http Code 가 2xx 이 아닐 경우, 실패했다는 Exception 을 기본적으로 터트립니다.

이럴 경우, Http 호출에 대한 실패 응답을 핸들링 하고 싶을 경우, `RestResponse` 를 사용할 수 있습니다.

```java
// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/member")
    RestResponse<String> getMappingUseCase();
}

// service
@Service
@RequiredArgsConstructor
public class SomeService {
    private final ApiTestClient apiTestClient;

    public void request1() {
        RestResponse<String> response = apiTestClient.getMappingUseCase();
        response.ifSuccess(data -> {
                    // String 형태의 data
                    
                    // Do Something...
                })
                .ifFailure((code, message) -> {
                    // 실패한 응답 code 값, 실패한 message
                    // 실패하였을 경우 Exception 이 터지지 않는다.
                    
                    // Do Something...
                });
        
        String result = response.getData();
    }
}
```