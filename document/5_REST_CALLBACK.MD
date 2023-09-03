# RestCallback

`RestResponse` 와 마찬가지로, `RestCallback` 은 실패한 호출에 대해서 핸들링이 가능하며, 동기 및 비동기로 실행이 가능합니다.

```java
// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/member")
    String getMappingUseCase(RestCallback<String> callback);
}

// service
@Service
@RequiredArgsConstructor
@Slf4j
public class SomeService {
    private final ApiTestClient apiTestClient;

    public void request1() {
        RestCallback restCallback = new RestCallback<String>() {
            
            @Override
            public boolean isAsync() {
                return false;   // 기본값은 true
            }

            @Override
            public void onSuccess(int httpCode, String data) {
                log.info("code :: {}, data :: {}", httpCode, data);
                // Do SomeThing...
            }

            @Override
            public void onFailure(int httpCode, String errorMessage) {
                log.info("code :: {}, errorMessage :: {}", httpCode, errorMessage);
                // Do SomeThing...
            }
        };
        
        String response = apiTestClient.getMappingUseCase(restCallback);
    }
}
```

> `RestCallback` 은 *2_GET.MD*, *3_POST.MD* 에서 설명했던 `RequestMetadata` 의 값으로 인지되는 `PathVariable`, `Header`, `Query Parameter`, `Body` 에 영향을 주지 않습니다.

> `RestCallback` 은 `RestResponse` 와 같이 사용하는 것을 지원하지 않습니다.