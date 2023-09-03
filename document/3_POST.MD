# Get

## 1. 기본 선언

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    @RequestMapping(value="/api/v1/member", method = RequestMethod.POST) // 기존 spring 에 존재하는 어노테이션
    String requestMappingUseCase();
    
    @PostMapping("/api/v1/member")
    String postMappingUseCase();
}
```

## 2. PathVariable

[2_GET.MD](https://github.com/BBackJK/rest-client/tree/main/document/2_GET.MD#2-pathvariable)
와 동일합니다.

## 3. Header

[2_GET.MD](https://github.com/BBackJK/rest-client/tree/main/document/2_GET.MD#3-header)
와 동일합니다.

## 4. Query Parameter

쿼리 파라미터는 Get 과 마찬가지로 다양하게 사용될 수 있습니다.

우선 `Get` 은 특정 어노테이션이 없을 경우, Query Parameter 로 인지하여 url 을 호출하지만,

`Post` 는 `Content-Type` 이 `x-www-form-urlencoded` 일 경우, 기존 Spring Controller 에서 사용하던 것과 마찬가지로, 아무런 어노테이션이 없는 경우, 해당 값을 formData 로 인지합니다.

따라서, 크게 `Content-Type` 이 `x-www-form-urlencoded` 와 `application/json` 일 경우 2가지로 나눌 수 있습니다.

* x-www-form-urlencoded 일 경우

**Mapping 어노테이션의 `consumes` 속성을 통해 어떤 content-type 인지 알려줍니다. 

> 없을 경우 기본은 `application/json`

```java
// Object
@Getter
public class SampleObject {
    private String key1=value1;
    private String key2=value2;
    private String key3=value3;
}

// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @PostMapping(value = "/api/v1/member", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String getMappingUseCase(SampleObject value); // 해당 값을 form data 로 인지하여, field 로 채움.

    // 주의
    // 다음과 같이 Content-Type 이 x-www-form-urlencoded 에는
    // 어노테이션이 없는 파라미터가 1개 이상일 경우, body 값을 2개로 판단하게 되어 run&build 시점에서 exception 을 발생시킵니다.
    @PostMapping(value = "/api/v1/member", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    String getMappingUseCase(String key1, String key2, String key3); 
}
```

* application/json 일 경우

이 경우에는 필수로 `@RequestBody` 어노테이션을 지정해주어야 합니다. (1개만 가능.)

```java
// Object
@Getter
public class SampleObject {
    private String key1=value1;
    private String key2=value2;
    private String key3=value3;
}

// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @PostMapping("/api/v1/member")
    String getMappingUseCase(@RequestBody SampleObject value); // 해당 값을 form data 로 인지하여, field 로 채움.

    // 주의
    // 다음과 같이 Content-Type 이 application/json 에는
    // @RequestBody 어노테이션이 없을 경우 오류로 인지하여 run&build 시점에 Exception 을 발생시킵니다.
    @PostMapping("/api/v1/member")
    String getMappingUseCase(String key1, String key2, String key3); 
}
```