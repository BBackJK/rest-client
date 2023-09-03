# @RestClient

`@RestClient` 는 다음과 같은 속성을 가지고 있습니다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RestClient {

    @AliasFor("context")
    String value() default "";
    @AliasFor("value")
    String context() default "";
    String url() default "";

    Class<? extends HttpAgent> agent() default RestTemplateAgent.class;
    Class<? extends ResponseMapper> mapper() default DefaultResponseMapper.class;
}
```

## 1. value

`value` 속성은 Logging 을 위한 Context 값입니다.

**README** 파일 에서 읽었던 `KakaoClient` 를 예시로, `value` 값이 LOGGING_CONTEXT 라면,
```java
@RestClient("LOGGING_CONTEXT")
public interface KakaoClient {
    String getToken(/**  some argument **/);
}
```

다음과 같이 로깅됩니다.

```shell
2023-09-03 17:13:39.133  INFO 23254 --- [nio-8080-exec-2] bback.module.http.helper.LogHelper       : [KakaoClient#LOGGING_CONTEXT::getToken LOG] Request		========================================================================================
2023-09-03 17:13:39.133  INFO 23254 --- [nio-8080-exec-2] bback.module.http.helper.LogHelper       : [KakaoClient#LOGGING_CONTEXT::getToken LOG] Request		| Agent				: UnirestHttpAgent
2023-09-03 17:13:39.133  INFO 23254 --- [nio-8080-exec-2] bback.module.http.helper.LogHelper       : [KakaoClient#LOGGING_CONTEXT::getToken LOG] Request		| Url				: POST https://kauth.kakao.com/oauth/token
2023-09-03 17:13:39.133  INFO 23254 --- [nio-8080-exec-2] bback.module.http.helper.LogHelper       : [KakaoClient#LOGGING_CONTEXT::getToken LOG] Request		| Header			: Content-Type - application/x-www-form-urlencoded
2023-09-03 17:13:39.133  INFO 23254 --- [nio-8080-exec-2] bback.module.http.helper.LogHelper       : [KakaoClient#LOGGING_CONTEXT::getToken LOG] Request		========================================================================================
```

패턴은 다음과 같습니다.

[@RestClient 가 선언된 class name]#[`value` or `context` 값]::[호출된 method 명] Logging...

## 2. context

1 의 value 와 동일합니다.

## 3. url

`url` 속성은 기본 baseUrl 와도 같이 쓰일 수 있습니다.

만약 다음과 같은 request url 들이 있으면

* http request url 1 : http://api.test.io/api/v1/member
* http request url 2 : http://api.test.io/api/v1/product

`@RestClient` 는 다음과 같이 사용할 수 있습니다.

```java
@RestClient(url="http://api.test.io")
public interface ApiTestClient {
    
    @GetMapping("/api/v1/member")
    String request1();
    @GetMapping("/api/v1/product")
    String request2();
}
```

## 4. agent

`agent` 속성은 http 요청을 대신 호출할 agent class 명을 선언합니다.

default agent 는 apache http client factory 를 이용한 *RestTemplate* 를 사용합니다.

그 외에 구현되어 있는 다른 agent 로는 경량화된 http library 인 *unirest* 를 사용합니다. 

> class : UnirestHttpAgent.class

해당 agent 들은 전부 `HttpAgent` 인터페이스를 통해 구현되어있으며, Bean 으로 설정되어있어야 합니다.

하여, 가존 구현되어있는 agent 대신, spring-webflux 에 starter 에 있는 WebClient 를 사용하고 싶으면, 

다음과 같이 `HttpAgent` 를 구현한 클래스를 작성하고

```java
@Component
public class WebClientAgent implements HttpAgent {
    
    private final WebClient webClient;
    
    public WebClientAgent(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    ResponseMetadata doGet(RequestMetadata requestMetadata) throws RestClientCallException {
        // get implement...
    }
    
    @Override
    ResponseMetadata doPost(RequestMetadata requestMetadata) throws RestClientCallException {
        // post implement...
    }
    @Override
    ResponseMetadata doPatch(RequestMetadata requestMetadata) throws RestClientCallException {
        // patch implement...
    }
    @Override
    ResponseMetadata doPut(RequestMetadata requestMetadata) throws RestClientCallException {
        // put implement...
    }
    
    @Override
    ResponseMetadata doDelete(RequestMetadata requestMetadata) throws RestClientCallException {
        // delete implement...
    }
}
```

다음과 같이 `RestClient` 를 정의해줄 수 있습니다.

```java
@RestClient(url="http://api.test.io", agent= WebClientAgent.class)
public interface ApiTestClient {
    
    @GetMapping("/api/v1/member")
    String request1();
    @GetMapping("/api/v1/product")
    String request2();
}
```

그럼, `ApiTestClient` Method 를 호출할 때 마다, `HttpAgent` 는 `WebClientAgent` 를 이용하여 호출하게 됩니다.

## 5. mapper

`mapper` 속성은 http 응답에 대한 값(String)을 실제 Method 에 Return Type 에 맞게 convert 를 하는 클래스를 선언합니다.

default mapper 로는 ObjectMapper 를 이용한 `DefaultResponseMapper` 를 사용합니다.

해당 mapper 는 `ResponseMapper` 인터페이스를 구현한 클래스이며, Bean 으로 설정되어있어야 합니다.

하여, 가존 구현되어있는 mapper 대신, 다른 mapper 를 사용하고 싶을 경우엔, **4. agent** 와 마찬가지로 `ResponseMapper` 를 구현하여 Bean 으로 설정 후 `@RestClient` 에 선언해줄 수 있습니다.