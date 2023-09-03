# Http connect 설정

rest-client 는 http agent 에서 해당 client 에 대한 인스턴스를 만들어서 활용하고 있습니다.

또한 각 라이브러리는 `Connection Pool` 을 관리할 수 있기에, HttpAgent 구현체가 싱글톤이 아닐 경우, 
메모리 낭비 혹은 사이드이펙트가 발생할 수 있습니다.

rest-client 에서는 다음 값들을 활용하여, connection 을 처리합니다.

```properties
bback.http.connect.pool-size: 20
bback.http.connect.keep-alive: 60
bback.http.connect.pool-per-route: 5

bback.http.socker.timeout: 180
```

```yaml
bback:
  http:
    connect:
      pool-size: 20 # 풀 개수 정의 (Default 20)
      keep-alive: 60 # Connection Keep Alive 정의 (Default 60 SEC) 
      pool-per-route: 5 # Request 요청 당 사용가능한 Route 수 (Default 5)
    socket:
      timeout: 180 # SocketTime 시간 정의 (Default 180 SEC)

```

위 설정들은 `RestClientConnectProperties` 객체가 가지고 있고,

```java
@Configuration
public class RestClientConnectProperties {

    private static final String PROPERTY_CONNECTION_PREFIX = "bback.http.connect";
    private static final String PROPERTY_SOCKET_PREFIX = "bback.http.socket";
    private static final String KEY_CONNECT_TIMEOUT = "timeout";
    private static final String KEY_CONNECT_POOL_SIZE = "pool-size";
    private static final String KEY_CONNECT_KEEP_ALIVE = "keep-alive";
    private static final String KEY_CONNECT_POOL_PER_ROUTE = "pool-per-route";
    private final int socketTimeout;
    private final int connectPoolSize;
    private final int connectKeepAlive;
    private final int connectPoolPerRoute;

    public RestClientConnectProperties(Environment env) {
        this.socketTimeout = Integer.parseInt(this.getSocketValue(env, KEY_CONNECT_TIMEOUT).orElseGet(() -> "180"));                // second
        this.connectPoolSize = Integer.parseInt(this.getConnectValue(env, KEY_CONNECT_POOL_SIZE).orElseGet(() -> "20"));            // amount
        this.connectKeepAlive = Integer.parseInt(this.getConnectValue(env, KEY_CONNECT_KEEP_ALIVE).orElseGet(() -> "5"));           // second
        this.connectPoolPerRoute = Integer.parseInt(this.getConnectValue(env, KEY_CONNECT_POOL_PER_ROUTE).orElseGet(() -> "5"));    // amount
    }

    private Optional<String> getConnectValue(Environment env, String key) {
        return Optional.ofNullable(env.getProperty(String.format("%s.%s", PROPERTY_CONNECTION_PREFIX, key)));
    }

    private Optional<String> getSocketValue(Environment env, String key) {
        return Optional.ofNullable(env.getProperty(String.format("%s.%s", PROPERTY_SOCKET_PREFIX, key)));
    }

    public int getSocketTimeout() {
        return this.socketTimeout;
    }

    public int getConnectPoolSize() {
        return this.connectPoolSize;
    }

    public int getConnectKeepAlive() {
        return this.connectKeepAlive;
    }

    public int getConnectPoolPerRoute() {
        return this.connectPoolPerRoute;
    }

    @Override
    public String toString() {
        return "RestClientConnectProperties{" +
                "socketTimeout=" + socketTimeout +
                ", connectPoolSize=" + connectPoolSize +
                ", connectKeepAlive=" + connectKeepAlive +
                ", connectPoolPerRoute=" + connectPoolPerRoute +
                '}';
    }
}
```

`RestClientConnectProperties` 는 http agent 에서 사용됩니다.

```java
@Component
public class RestTemplateAgent implements HttpAgent {

    private final RestTemplate template;
    private final ObjectMapper om;

    public RestTemplateAgent(RestClientConnectProperties connectProperties) {
        HttpClient httpClient = HttpClientBuilder.create()
                .setMaxConnTotal(connectProperties.getConnectPoolSize())
                .setMaxConnPerRoute(connectProperties.getConnectPoolPerRoute())
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(connectProperties.getConnectKeepAlive() * 1000);
        factory.setReadTimeout(connectProperties.getConnectKeepAlive() * 1000);
        factory.setConnectionRequestTimeout(connectProperties.getConnectKeepAlive() * 1000);

        this.template = new RestTemplate(factory);
        this.om = new ObjectMapper();
    }
    
    ...
}

@Component
public class UnirestHttpAgent implements HttpAgent {
    
    private final UnirestInstance unirest;
    private final ObjectMapper om;

    public UnirestHttpAgent(RestClientConnectProperties connectProperties) {

        Config config = new Config();
        config.socketTimeout(connectProperties.getSocketTimeout() * 1000)
                .connectTimeout(connectProperties.getConnectKeepAlive() * 1000)
                .concurrency(
                        connectProperties.getConnectPoolSize()
                        , connectProperties.getConnectPoolPerRoute()
                )
                .followRedirects(false)
                .enableCookieManagement(false)
                .cacheResponses(
                        Cache.builder().depth(10)
                                .maxAge(10, TimeUnit.SECONDS)
                )
        ;

        this.unirest = new UnirestInstance(config);
        this.om = new ObjectMapper();
    }
}
```