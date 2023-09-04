# Other Documents..

* [1_REST_CLIENT](https://github.com/BBackJK/rest-client/tree/main/document/1_REST_CLIENT.MD)
* [2_REQUEST](https://github.com/BBackJK/rest-client/tree/main/document/2_REQUEST.MD)
* [3_HEADER](https://github.com/BBackJK/rest-client/tree/main/document/3_HEADER.MD)
* [5_QUERY_PARAMETER](https://github.com/BBackJK/rest-client/tree/main/document/5_QUERY_PARAMETER.MD)
* [6_BODY_DATA](https://github.com/BBackJK/rest-client/tree/main/document/6_BODY_DATA.MD)
* [7_REST_RESPONSE](https://github.com/BBackJK/rest-client/tree/main/document/7_REST_RESPONSE.MD)
* [8_REST_CALLBACK](https://github.com/BBackJK/rest-client/tree/main/document/8_REST_CALLBACK.MD)
* [9_CONFIGURATION](https://github.com/BBackJK/rest-client/tree/main/document/9_CONFIGURATION.MD)
* [10_KAKAO_EXAMPLE](https://github.com/BBackJK/rest-client/tree/main/document/10_KAKAO_EXAMPLE.MD)

# Path Variable

Http 요청 시 Url Path 안에 동적으로 들어가야하는 값을 명시해야하는 경우가 존재합니다.

이럴 경우 기존 **spring-web.jar** 에 있는 `@PathVariable` 어노테이션을 사용합니다.

## 1. @PathVariable

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    /**
     * @PathVariable 어노테이션은 기본적으로 parameter name 과 path variable name 이 같을 때 동작합니다.
     */
    @GetMapping("/api/v1/${pathValue}/path1")
    String pathVariableUseCase1(@PathVariable String pathValue);

    /**
     * 다른 name 을 사용하고 싶을 경우 다음과 같이 사용할 수 있습니다.
     */
    @GetMapping("/api/v1/${pathValue}/path1")
    String pathVariableUseCase1(@PathVariable("pathValue") String val);
}
```