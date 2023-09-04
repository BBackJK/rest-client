# Other Documents..

* [1_REST_CLIENT](https://github.com/BBackJK/rest-client/tree/main/document/1_REST_CLIENT.MD)
* [2_REQUEST](https://github.com/BBackJK/rest-client/tree/main/document/2_REQUEST.MD)
* [3_HEADER](https://github.com/BBackJK/rest-client/tree/main/document/3_HEADER.MD)
* [4_PATH_VARIABLE](https://github.com/BBackJK/rest-client/tree/main/document/4_PATH_VARIABLE.MD)
* [5_QUERY_PARAMETER](https://github.com/BBackJK/rest-client/tree/main/document/5_QUERY_PARAMETER.MD)
* [7_REST_RESPONSE](https://github.com/BBackJK/rest-client/tree/main/document/7_REST_RESPONSE.MD)
* [8_REST_CALLBACK](https://github.com/BBackJK/rest-client/tree/main/document/8_REST_CALLBACK.MD)
* [9_CONFIGURATION](https://github.com/BBackJK/rest-client/tree/main/document/9_CONFIGURATION.MD)
* [10_KAKAO_EXAMPLE](https://github.com/BBackJK/rest-client/tree/main/document/10_KAKAO_EXAMPLE.MD)

# Body Data

Http `POST`, `PUT`, `PATCH` 요청 시 사용하며, 이 경우 *content-type* 에 따라 2가지 케이스로 나누어지게 됩니다.

1. x-www-form-urlencoded
2. application/json

## 1. x-www-form-urlencoded

이 경우, 기존 `@xxxMapping` 어노테이션의 `consumes` 속성을 이용하여 처리합니다. 

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    /**
     * consumes 속성을 통하여, x-www-form-url-encoded 타입임을 알려주고, BodyData 로 사용할 변수는 어노테이션을 선언하지 않습니다.
     * consumes 미 선언 시, 기본값으로 application/json 으로 인식.
     */
    @PostMapping(value="/api/v1/post1", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String postFormUseCase1(SomePOJO somePOJO);

    /**
     * 이 경우, pathValue 변수가 어떠한 어노테이션도 없을 시, run&build 시에 exception 을 터트리게됩니다.
     */
    @PostMapping(value="/api/v1/{pathValue}/post2", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String postFormUseCase2(@PathVariable String pathValue, SomePOJO somePOJO);

    /**
     * otherBodyDataCandidate 가 아무런 어노테이션을 가지고 있지 않아, body data 가 2개로 인식 되면서 run&build 시에 exception 을 터트리게 됩니다.
     */
    @PostMapping(value="/api/v1/post2", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String postFormUseCase3(String otherBodyDataCandidate, SomePOJO somePOJO);
}
```

2. application/json

해당 경우에는 Body Data 에 `@RequestBody` 어노테이션을 명시해줍니다.

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    /**
     * 다음과 같이 BodyData 로 사용할 변수는 @RequestBody 어노테이션을 선언합니다.
     */
    @PostMapping(value="/api/v1/post1")
    String postFormUseCase1(@RequestBody SomePOJO somePOJO);
}
```

> 어떠한 경우에도, body data 는 하나로만 사용이 가능합니다. 2개 이상 인식될 경우 run&build 시점에서 exception 을 터트리게 됩니다.
