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