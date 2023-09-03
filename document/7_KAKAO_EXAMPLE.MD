# 카카오 샘플 코드

## RestClient

```java
@RestClient(agent = UnirestHttpAgent.class)
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

## POJO


### KakaoTokenResponseModel

```java
@NoArgsConstructor
@Data
public class KakaoTokenResponseModel {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("refresh_token_expires_in")
    private int refreshTokenExpiresIn;
}


```

### KakaoUserInfoResponseModel

```java
@NoArgsConstructor
@Data
public class KakaoUserInfoResponseModel {

    private int id;

    @JsonProperty("connected_at")
    private String connectedAt;

    private Properties properties;

    @JsonProperty("kakao_account")
    private Account kakaoAccount;


    @NoArgsConstructor
    @Data
    public static class Properties {
        private String nickname;

        @JsonProperty("profile_image")
        private String profileImage;

        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }

    @NoArgsConstructor
    @Data
    public static class Account {

        @JsonProperty("profile_needs_agreement")
        private boolean profileNeedsAgreement;

        private Profile profile;

        @JsonProperty("has_email")
        private boolean hasEmail;

        @JsonProperty("email_needs_agreement")
        private boolean emailNeedsAgreement;
    }

    @NoArgsConstructor
    @Data
    public static class Profile {
        private String nickname;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        @JsonProperty("thumbnail_image_url")
        private String thumbnailImageUrl;

        @JsonProperty("is_default_image")
        private boolean isDefaultImage;
    }
}
```

## controller

```java
@Controller
@RequiredArgsConstructor
@Slf4j
public class KakaoController {

    private static final String BASE_APP_URL = "http://localhost:8080";
    private static final String APP_KEY = "##############################"; // TODO: REST API KEY 를 입력하세요.
    private final KakaoClient kakaoClient;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/kakao/oauth")
    public String kakaoOauth(@RequestHeader) {
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
        KakaoTokenResponseModel tokenResponse = kakaoClient.getToken(requestValues);
        String bearerToken = tokenResponse.getAccessToken();

        KakaoUserInfoResponseModel userInfoResponse = kakaoClient.getUserInfo("Bearer " + bearerToken);
        log.info("userInfoResponse :: {}", userInfoResponse);

        return redirectHome;
    }
}

```