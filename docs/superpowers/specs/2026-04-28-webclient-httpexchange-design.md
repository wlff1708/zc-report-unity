# WebClient + @HttpExchange 集成设计

**日期**: 2026-04-28
**状态**: 待用户评审

---

## 1. 背景

项目需要集成 HTTP 客户端，用于调用第三方 REST API。技术选型：
- **WebClient**: Spring 官方推荐的现代 HTTP 客户端，作为底层
- **@HttpExchange**: Spring 6 的声明式 HTTP 接口，配合 WebClient 工作

---

## 2. 依赖设计

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-reactor-netty</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

排除 Netty 依赖，保留 WebClient 和响应式 Core。

---

## 3. 配置类

### 3.1 WebClientConfig

**路径**: `module/im/config/http/WebClientConfig.java`

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(WebClient webClient) {
        return HttpServiceProxyFactory.builderFor(webClient)
                .build();
    }
}
```

**说明**:
- 排除 Netty 后，Spring Boot WebFlux 自动使用 Tomcat 作为底层引擎
- 通过 `HttpServiceProxyFactory` 将 WebClient 接入 @HttpExchange

### 3.2 Tomcat 依赖

如果使用 Tomcat（Spring Boot 默认），无需额外依赖。排除 Netty 后会自动回退到 Tomcat。

---

## 4. 接口层

### 4.1 IMReportExchangeConstant

**路径**: `module/im/exchange/IMReportExchangeConstant.java`

```java
public interface IMReportExchangeConstant {
    String HEADER_USER_AGENT = "User-Agent";
    String COOKIE_SESSION = "Cookie";
    String HEADER_CONTENT_FILEDESC = "Content-FileDesc";
    String HEADER_ALARM_FILE_COUNT = "Alarm-File-Count";
    String HEADER_ALARM_COUNT = "Alarm-Count";
    String PARAM_MODULE = "module";
    String PARAM_FILE = "file";
}
```

### 4.2 IMReportExchange

**路径**: `module/im/exchange/IMReportExchange.java`

```java
@HttpExchange
public interface IMReportExchange extends IMReportExchangeConstant {

    @PostExchange(url = "{baseUrl}/S2/V1/forward/file/alarm/{category}")
    ResponseEntity<S2ReportResultVo> alarmFileForward(
            @PathVariable("baseUrl") String baseUrl,
            @RequestHeader(HEADER_USER_AGENT) String userAgent,
            @RequestHeader(COOKIE_SESSION) String cookie,
            @PathVariable("category") String category,
            @RequestParam(PARAM_MODULE) String module,
            @RequestHeader(HEADER_CONTENT_FILEDESC) String alarmFileDescList,
            @RequestHeader(value = HEADER_ALARM_FILE_COUNT, required = false) String alarmFileCountJsonObj,
            @RequestParam(value = PARAM_FILE, required = false) List<MultipartFile> files);

    @PostExchange(url = "{baseUrl}/S2/V1/forward/alarm/{category}")
    ResponseEntity<S2ReportResultVo> alarmForward(
            @PathVariable("baseUrl") String baseUrl,
            @RequestHeader(HEADER_USER_AGENT) String userAgent,
            @RequestHeader(COOKIE_SESSION) String cookie,
            @PathVariable("category") String category,
            @RequestHeader(value = HEADER_ALARM_COUNT, required = false) String alarmCountJsonObj,
            @RequestParam(value = PARAM_FILE, required = false) List<MultipartFile> files);
}
```

---

## 5. 调用层

### 5.1 S2ReportBusiness

**路径**: `module/im/business/S2ReportBusiness.java`

```java
@Service
public class S2ReportBusiness {

    private final IMReportExchange exchange;
    private final Caches caches;  // 缓存访问

    public S2ReportBusiness(HttpServiceProxyFactory httpServiceProxyFactory, Caches caches) {
        this.exchange = httpServiceProxyFactory.createClient(IMReportExchange.class);
        this.caches = caches;
    }

    public S2ReportResultVo forwardAlarmFile(String imCode, ...) {
        // 从缓存获取上级 MC 地址
        String baseUrl = caches.get(CacheDef.IM_MC_ADDRESS, imCode);

        // 调用级联上报告警接口
        return exchange.alarmFileForward(
                baseUrl, userAgent, cookie, category, module,
                alarmFileDescList, alarmFileCountJsonObj, files
        ).getBody();
    }
}
```

---

## 6. 超时配置

超时在 `WebClient` 层级统一配置：

```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
            .clientConnector(new JettyClientConnector())
            .build();
}
```

**说明**: 当前设计超时为默认值（连接超时 5s、读取超时 10s）。如需自定义，可通过 `ClientCodecConfigurer` 或 `HttpClient` 显式配置。

---

## 7. 包结构

```
module/im/
├── config/http/
│   └── WebClientConfig.java
├── exchange/
│   ├── IMReportExchange.java
│   └── IMReportExchangeConstant.java
└── business/
    └── S2ReportBusiness.java
```

---

## 8. 后续扩展

- **超时自定义**: 在 WebClientConfig 中通过 `HttpClient` 显式配置超时参数
- **日志拦截**: 通过 `WebClient.builder().filter()` 添加日志
- **重试机制**: 通过 `ExchangeFilterFunctions.retry()` 添加重试
