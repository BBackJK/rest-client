# Other Documents..

* [1_REST_CLIENT](https://github.com/BBackJK/rest-client/tree/main/document/1_REST_CLIENT.MD)
* [2_REQUEST](https://github.com/BBackJK/rest-client/tree/main/document/2_REQUEST.MD)
* [4_PATH_VARIABLE](https://github.com/BBackJK/rest-client/tree/main/document/4_PATH_VARIABLE.MD)
* [5_QUERY_PARAMETER](https://github.com/BBackJK/rest-client/tree/main/document/5_QUERY_PARAMETER.MD)
* [6_BODY_DATA](https://github.com/BBackJK/rest-client/tree/main/document/6_BODY_DATA.MD)
* [7_REST_RESPONSE](https://github.com/BBackJK/rest-client/tree/main/document/7_REST_RESPONSE.MD)
* [8_REST_CALLBACK](https://github.com/BBackJK/rest-client/tree/main/document/8_REST_CALLBACK.MD)
* [9_CONFIGURATION](https://github.com/BBackJK/rest-client/tree/main/document/9_CONFIGURATION.MD)
* [10_KAKAO_EXAMPLE](https://github.com/BBackJK/rest-client/tree/main/document/10_KAKAO_EXAMPLE.MD)

# Header

Http 요청 시 Header 를 명시해야하는 경우가 존재합니다.

이럴 경우 기존 **spring-web.jar** 에 있는 `@RequestHeader` 어노테이션을 사용하거나, 인증과 같은 Header 일 경우 `@Authorization` 을 사용하여 명시할 수 있습니다.

## 1. @RequestHeader

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/header1")
    String requestHeaderUseCase1(@RequestHeader("x-screen-id") String screenId); // http 요청 시 header 에 "x-screen-id":"screenId값" 형태로 요청하게 됩니다.

    @GetMapping("/api/v1/header2")
    String requestHeaderUseCase2(@RequestHeader("Authorization") String bearerToken); // 해당 메소드처럼 Authorization Key 값을 이용하여, 값을 지정할 수 있습니다. 

    @GetMapping("/api/v1/header3")
    String requestHeaderUseCase3(@RequestHeader(RestClientUtils.HTTP_HEADER_AUTH_KEY) String bearerToken); // RestClientUtils.HTTP_HEADER_AUTH_KEY 를 사용하여 휴먼오류를 방지할 수도 있습니다.
}
```

## 2. @Authorization

인증에 관련된 헤더를 명시적으로 사용하기 위한 어노테이션입니다.

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authorization {

    String type() default "Bearer"; // type 명시를 안할 시 기본값은 Bearer (OAuth)

    boolean onPrefix() default true;    // type 값을 prefix 로 사용할 것인지 여부 기본값 true
}
```


```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    /**
     * 만약 bearerToken 이 '1234' 일 시, header 에는 다음과 같이 명시됩니다. 
     * "Authorization":"Bearer 1234" 
     * (type 속성을 통하여 prefix 지정)
     */
    @PostMapping("/api/v1/auth1")
    String authorizationUseCase1(@Authorization String bearerToken);

    /**
     * 만약 다른 type 값을 사용하고 싶으면, 다음과 같이 사용할 수 있고, 다음과 같이 명시됩니다.
     * "Authorization":"Jwt ${jwtToken값}"
     */
    @PostMapping("/api/v1/auth2")
    String authorizationUseCase2(@Authorization(type = "Jwt") String jwtToken);

    /**
     * 이런 type 들을 prefix 로 두기 싫은 경우, onPrefix 속성을 false 로 줄 수 있습니다.
     * "Authorization":"${jwtToken값}"
     */
    @PostMapping("/api/v1/auth3")
    String authorizationUseCase3(@Authorization(type = "Jwt", onPrefix = false) String jwtToken);

}
```
