# Get

## 1. 기본 선언

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {

    @RequestMapping(value="/api/v1/member", method = RequestMethod.GET) // 기존 spring 에 존재하는 어노테이션
    String requestMappingUseCase();
    
    @GetMapping("/api/v1/member")
    String getMappingUseCase();
}
```

## 2. PathVariable

`PathVariable` 기능 또한 지원 합니다.

```java
// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/{memberId}/member")
    String getMappingUseCase(@PathVariable Long memberId);
}

// service
@Service
@RequiredArgsConstructor
public class SomeService {
    private final ApiTestClient apiTestClient;
    
    public void request1() {
        apiTestClient.getMappingUseCase(1L);
    }
}
```

위와 같이 작성을 하면, 호출 url은 다음과 같습니다. http://api.test.io/api/v1/1/member

> `@PathVariable` 어노테이션은 필수입니다.


## 3. Header

Request Header 에 들어갈 값도 정의할 수 있습니다.

```java
// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/{memberId}/member")
    String getMappingUseCase(@PathVariable Long memberId, @RequestHeader("x-screen-id") String screenId);
}
```

> `@RequestHeader` 어노테이션은 필수입니다.

## 4. Query Parameter

쿼리 파라미터는 다양하게 사용될 수 있습니다.

우선 `Get` 에 관한 query Parameter 로 말씀드리면,

* 특정 어노테이션이 없을 경우, Query Parameter 로 인지하게 됩니다.

```java
// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/member")
    String getMappingUseCase(String key1, String key2, String key3);
}

// service
@Service
@RequiredArgsConstructor
public class SomeService {
    private final ApiTestClient apiTestClient;

    public void request1() {
        apiTestClient.getMappingUseCase("value1", "value2", "value3");
    }
}
```

위와 같을 경우 호출 Url 은 http://api.test.io/api/v1/member?key1=value1&key2=value2&key3=value3

* @RequestParam 어노테이션 선언으로도 가능합니다.

```java
// http client
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/member")
    String getMappingUseCase(@RequestParam String key1);
}
```

* 특정 클래스로도 호출이 가능합니다.

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
    
    @GetMapping("/api/v1/member")
    String getMappingUseCase(@RequestParam SampleObject value);
}
```

* Map 으로도 호출이 가능합니다.

```java

import java.util.HashMap;

// http client
@RestClient(url = "http://api.test.io")
public interface ApiTestClient {

    @GetMapping("/api/v1/member")
    String getMappingUseCase(@RequestParam Map<String, Object> requestMap);
}

// service
@Service
@RequiredArgsConstructor
public class SomeService {
    private final ApiTestClient apiTestClient;

    public void request1() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        apiTestClient.getMappingUseCase(map);
    }
}
```
