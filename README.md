# Rest Client

## 1. Overview

`FeginClient`  벤치마킹 http client library.

## 2. Quick Start


### 2.1 Maven
```xml
...
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
...
<dependency>
    <groupId>com.github.BBackJK</groupId>
    <artifactId>rest-client</artifactId>
    <version>v0.1.4</version>
</dependency>
...
```

### 2.2 Gradle

```groovy
...
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
...
dependencies {
    implementation 'com.github.BBackJK:rest-client:v0.1.4'
}
...
```

## 3. How To Use

### 3.1 Spring boot 가 아닌 경우

```java
@SpringBootApplication
@EnableRestClient
public class WhateverApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhateverApplication.class, args);
    }

}
```

`@EnableRestClient` 어노테이션으로 해당 어노테이션으로 이루어진 Application Start Point Bean Package 를 찾아서 `@RestClient` 어노테이션을 가진 인터페이스를 Scan 하여 Bean 으로 등록한다.

### 3.2 Spring boot 인 경우

Spring-Boot 는 Auto-Configuration 을 이용하여 `@ComponentScan` 어노테이션을 찾아서 해당 어노테이션이 쓰인 Package , `@ComponentScan`의 `value()`, `basePackages()`, `basePackageClasses()` 속성을 찾아서 package 에서 `@RestClient` 어노테이션을 가진 인터페이스를 Scan 하여 Bean 으로 등록합니다.

> Spring boot 에서 `@EnableRestClient` 어노테이션을 선언하여도 충돌이 일어나지 않습니다.


### 3.3 http client 를 작성할 인터페이스 선언

```java
// 예시 1. 카카오 Rest Api 호출
@RestClient
public interface KakaoClient {
    
    @PostMapping(
            value = "https://kauth.kakao.com/oauth/token"
            , consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    String getToken(Map<String, String> requestValues);

    @GetMapping(value = "https://kapi.kakao.com/v2/user/me")
    String getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
```

### 3.4 Bean 에서 호출

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class KakaoController {

    private static final String BASE_APP_URL = "http://localhost:8080";
    private static final String APP_KEY = "#############################"; // REST API KEY
    private final KakaoClient kakaoClient;  // Kakao Rest Client Bean 주입

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/kakao/oauth")
    public String kakaoOauth() {
        String oauthUrl = "https://kauth.kakao.com/oauth/authorize";
        oauthUrl += "?response_type=code";
        oauthUrl += "&client_id=" + APP_KEY;
        oauthUrl += "&redirect_uri=" + BASE_APP_URL + "/kakao/oauth/callback";
        oauthUrl += "&prompt=login";    // 기존 세션 로그인 여부와 상관없이 로그인
        return "redirect:" + oauthUrl;
    }

    @GetMapping("/kakao/oauth/callback")
    public String kakaoCallback(
            @RequestParam(value = "code", required = false) String kakaoOauthCode
            , @RequestParam(value = "error", required = false) String kakaoOauthError
    ) {
        String redirectHome = "redirect:/";
        if (RestClientObjectUtils.isEmpty(kakaoOauthCode)) {
            log.error(" 로그인에 실패하였습니다. ");
            return redirectHome;
        }
        if (RestClientObjectUtils.isNotEmpty(kakaoOauthError)) {
            log.error(" 로그인을 취소하셨습니다. ");
            return redirectHome;
        }

        Map<String, String> requestValues = new HashMap<>();
        requestValues.put("grant_type", "authorization_code");
        requestValues.put("client_id", APP_KEY);
        requestValues.put("redirect_uri", BASE_APP_URL + "/kakao/oauth/callback");
        requestValues.put("code", kakaoOauthCode);
        String tokenResponse = kakaoClient.getToken(requestValues);
        log.info(tokenResponse);    // info..
        return redirectHome;
    }
}

```

## 5. Document

[Document](https://github.com/BBackJK/rest-client/tree/main/document)