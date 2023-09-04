# Other Documents..

* [1_REST_CLIENT](https://github.com/BBackJK/rest-client/tree/main/document/1_REST_CLIENT.MD)
* [2_REQUEST](https://github.com/BBackJK/rest-client/tree/main/document/2_REQUEST.MD)
* [3_HEADER](https://github.com/BBackJK/rest-client/tree/main/document/3_HEADER.MD)
* [4_PATH_VARIABLE](https://github.com/BBackJK/rest-client/tree/main/document/4_PATH_VARIABLE.MD)
* [6_BODY_DATA](https://github.com/BBackJK/rest-client/tree/main/document/6_BODY_DATA.MD)
* [7_REST_RESPONSE](https://github.com/BBackJK/rest-client/tree/main/document/7_REST_RESPONSE.MD)
* [8_REST_CALLBACK](https://github.com/BBackJK/rest-client/tree/main/document/8_REST_CALLBACK.MD)
* [9_CONFIGURATION](https://github.com/BBackJK/rest-client/tree/main/document/9_CONFIGURATION.MD)
* [10_KAKAO_EXAMPLE](https://github.com/BBackJK/rest-client/tree/main/document/10_KAKAO_EXAMPLE.MD)

# Query Parameter

Http 요청 시 자주 사용하는 값으로, 여러가지 방법으로 Query Parameter 를 명시할 수 있습니다.

## 1. @RequestParam 어노테이션이 존재할 경우

이럴 경우 기존 **spring-web.jar** 에 있는 `@RequestParam` 어노테이션을 사용하며, 
*Parameter* 중 `@RequestParam` 어노테이션이 하나라도 존재할 경우, `@RequestParam` 어노테이션에 대해서만 Query Parameter 로 인지합니다. 

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    /**
     * @RequestParam 변수명으로 key 값을 명시해주고, key1 에 해당하는 값이 value1일 경우,
     * 다음과 같은 url 을 호출합니다.
     * http://api.test.io/api/v1/query1?key1=value1
     */
    @GetMapping("/api/v1/query1")
    String requestParamUseCase1(@RequestParam String key1);

    /**
     * RequestParam 은 Map 혹은 Field에 대한 getter 가 있는 POJO 도 지원합니다.
     */
    @GetMapping("/api/v1/query2")
    String requestParamUseCase2(@RequestParam Map<String, String> queryMap);

    @GetMapping("/api/v1/query3")
    String requestParamUseCase3(@RequestParam SomeObject someObject); // 이 경우 Reflect 를 활용하기 때문에 Getter 필수. 
}
```

## 2. 모든 파라미터에 어노테이션이 비어있을 경우

Header 라면 `@RequestHeader` 를 통한 명시,
Path 라면 `@PathVariable` 를 통한 명시를 하며, 
만약 파라미터에 어노테이션이 모두 없을 경우, 모든 값(Map, POJO 제외)을 Query Parameter 라고 인지합니다.

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    /**
     * @RequestParam 변수명으로 key 값을 명시해주고, key1 에 해당하는 값이 value1, key2 에 해당하는 값이 2, key3 에 해당하는 값이 val3 일 경우
     * 다음과 같은 url 을 호출합니다.
     * http://api.test.io/api/v1/query1?key1=value1&key2=2&key3=val3
     */
    @GetMapping("/api/v1/query1")
    String requestParamUseCase1(String key1, int key2, String key3);

    /**
     * 해당 경우처럼, path variable 와 동일한 변수명이 있을 경우, query parameter 로는 key2, key3 만 인지하며,
     * pathValue 변수는 @PathVariable 어노테이션이 없어서, run&build 시 exception 을 발생시킵니다.
     */
    @GetMapping("/api/v1/{pathValue}/query2")
    String requestParamUseCase2(String pathValue, int key2, String key3);
}
```