# Other Documents..

* [1_REST_CLIENT](https://github.com/BBackJK/rest-client/tree/main/document/1_REST_CLIENT.MD)
* [3_HEADER](https://github.com/BBackJK/rest-client/tree/main/document/3_HEADER.MD)
* [4_PATH_VARIABLE](https://github.com/BBackJK/rest-client/tree/main/document/4_PATH_VARIABLE.MD)
* [5_QUERY_PARAMETER](https://github.com/BBackJK/rest-client/tree/main/document/5_QUERY_PARAMETER.MD)
* [6_BODY_DATA](https://github.com/BBackJK/rest-client/tree/main/document/6_BODY_DATA.MD)
* [7_REST_RESPONSE](https://github.com/BBackJK/rest-client/tree/main/document/7_REST_RESPONSE.MD)
* [8_REST_CALLBACK](https://github.com/BBackJK/rest-client/tree/main/document/8_REST_CALLBACK.MD)
* [9_CONFIGURATION](https://github.com/BBackJK/rest-client/tree/main/document/9_CONFIGURATION.MD)
* [10_KAKAO_EXAMPLE](https://github.com/BBackJK/rest-client/tree/main/document/10_KAKAO_EXAMPLE.MD)

# Request

기존 **spring-web.jar** 에 있는 `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping` 을 이용하여 Request Method 를 판단합니다.

Request Method `GET`, `POST`, `PUT`, `PATCH`, `DELETE` 를 지원합니다.

## 1. GET

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    // RequestMapping 을 이용한 Get 선언 (method 명시 안할 시 기본값은 Get)
    @RequestMapping(value="/api/v1/member", method = RequestMethod.GET)
    String requestMappingUseCase(/** some argument **/);

    // GetMapping 을 이용한 Get 선언
    @GetMapping("/api/v1/member")
    String getMappingUseCase(/** some argument **/);
}
```

## 2. POST

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    // RequestMapping 을 이용한 POST 선언 (method 명시 안할 시 기본값은 Get)
    @RequestMapping(value="/api/v1/member", method = RequestMethod.POST)
    String requestMappingUseCase(/** some argument **/);

    // PostMapping 을 이용한 Post 선언
    @PostMapping("/api/v1/member")
    String postMappingUseCase(/** some argument **/);
}
```

## 3. PUT

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    // RequestMapping 을 이용한 PUT 선언 (method 명시 안할 시 기본값은 Get)
    @RequestMapping(value="/api/v1/member", method = RequestMethod.PUT)
    String requestMappingUseCase(/** some argument **/);

    // PutMapping 을 이용한 Post 선언
    @PutMapping("/api/v1/member")
    String putMappingUseCase(/** some argument **/);
}
```

## 4. PATCH

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    // RequestMapping 을 이용한 PATCH 선언 (method 명시 안할 시 기본값은 Get)
    @RequestMapping(value="/api/v1/member", method = RequestMethod.PATCH)
    String requestMappingUseCase(/** some argument **/);

    // PatchMapping 을 이용한 Post 선언
    @PatchMapping("/api/v1/member")
    String patchMappingUseCase(/** some argument **/);
}
```

## 5. DELETE

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    // RequestMapping 을 이용한 DELETE 선언 (method 명시 안할 시 기본값은 Get)
    @RequestMapping(value="/api/v1/member", method = RequestMethod.DELETE)
    String requestMappingUseCase(/** some argument **/);

    // DeleteMapping 을 이용한 Post 선언
    @DeleteMapping("/api/v1/member")
    String deleteMappingUseCase(/** some argument **/);
}
```