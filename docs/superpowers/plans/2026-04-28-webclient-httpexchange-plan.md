# WebClient + @HttpExchange 集成实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 IM 模块中集成 WebClient + @HttpExchange，提供声明式 HTTP 调用能力

**Architecture:** 通过 spring-boot-starter-webflux（排掉 Netty）引入 WebClient，通过 HttpServiceProxyFactory 将 WebClient 接入 @HttpExchange 注解接口

**Tech Stack:** Spring Boot 3.4.5 WebFlux、@HttpExchange、Tomcat

---

## 文件结构

```
module/im/
├── config/http/
│   └── WebClientConfig.java          # WebClient 和 HttpServiceProxyFactory 配置
├── exchange/
│   ├── IMReportExchange.java          # @HttpExchange 接口定义
│   └── IMReportExchangeConstant.java # 常量接口
└── business/
    └── S2ReportBusiness.java         # HTTP 调用业务类
```

---

## Task 1: 添加 WebFlux 依赖

**Files:**
- Modify: `pom.xml:32-37`

- [ ] **Step 1: 在 pom.xml 中 spring-boot-starter-web 之后添加 webflux 依赖**

在 `pom.xml` 的 `<dependencies>` 中，在 `spring-boot-starter-web` 依赖之后添加：

```xml
<!-- Spring Boot WebFlux (排除 Netty，使用 Tomcat) -->
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

- [ ] **Step 2: 验证依赖添加成功**

Run: `mvn dependency:tree -Dincludes=org.springframework.boot:spring-boot-starter-webflux`
Expected: 输出包含 spring-boot-starter-webflux

- [ ] **Step 3: 提交**

```bash
git add pom.xml
git commit -m "feat(im): 添加 spring-boot-starter-webflux 依赖"
```

---

## Task 2: 创建 WebClientConfig 配置类

**Files:**
- Create: `module/im/config/http/WebClientConfig.java`

- [ ] **Step 1: 创建 config/http 目录**

```bash
mkdir -p module/im/config/http
```

- [ ] **Step 2: 创建 WebClientConfig.java**

```java
package com.report.module.im.config.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * WebClient + @HttpExchange 配置
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(WebClient webClient) {
        return HttpServiceProxyFactory.builderFor(webClient).build();
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add module/im/config/http/WebClientConfig.java
git commit -m "feat(im): 添加 WebClientConfig 配置类"
```

---

## Task 3: 创建 IMReportExchangeConstant 常量接口

**Files:**
- Create: `module/im/exchange/IMReportExchangeConstant.java`

- [ ] **Step 1: 创建 exchange 目录**

```bash
mkdir -p module/im/exchange
```

- [ ] **Step 2: 创建 IMReportExchangeConstant.java**

```java
package com.report.module.im.exchange;

/**
 * 级联上报接口常量定义
 */
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

- [ ] **Step 3: 提交**

```bash
git add module/im/exchange/IMReportExchangeConstant.java
git commit -m "feat(im): 添加 IMReportExchangeConstant 常量接口"
```

---

## Task 4: 创建 IMReportExchange 注解接口

**Files:**
- Create: `module/im/exchange/IMReportExchange.java`

- [ ] **Step 1: 创建 IMReportExchange.java**

```java
package com.report.module.im.exchange;

import com.report.module.im.pojo.vo.S2ReportResultVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.annotation.RequestPart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * 级联上报 - 上级MC接口定义（对应 V1S2SubMcController 服务端）
 *
 * @HttpExchange url 用于动态指定上级MC地址
 */
@HttpExchange
public interface IMReportExchange extends IMReportExchangeConstant {

    /**
     * 上报告警文件（级联转发）
     *
     * @param baseUrl            上级MC地址，如 http://192.168.1.100:8181
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类（路径变量）
     * @param module             告警模块
     * @param alarmFileDescList  告警文件描述 JSON
     * @param alarmFileCountJsonObj 告警文件数量统计 JSON（可选）
     * @param files              告警文件列表
     * @return 响应结果
     */
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

    /**
     * 上报告警消息（级联转发）
     *
     * @param baseUrl            上级MC地址
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类（路径变量）
     * @param alarmCountJsonObj  告警消息数量统计 JSON（可选）
     * @param files              告警消息文件列表
     * @return 响应结果
     */
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

- [ ] **Step 2: 提交**

```bash
git add module/im/exchange/IMReportExchange.java
git commit -m "feat(im): 添加 IMReportExchange 声明式HTTP接口"
```

---

## Task 5: 创建 S2ReportBusiness 调用类

**Files:**
- Create: `module/im/business/S2ReportBusiness.java`

- [ ] **Step 1: 查看 ImCacheKeysName.java 中是否已有 IM_S2_BASE_URL 缓存定义**

查看 `module/im/constants/ImCacheKeysName.java`，确认是否已有 `IM_S2_BASE_URL` 的定义。如果已有，可直接引用；如果没有，可以在 ImCacheKeysName.java 中添加或在 S2ReportBusiness 中直接定义。

- [ ] **Step 2: 创建 S2ReportBusiness.java**

```java
package com.report.module.im.business;

import com.report.common.util.cache.CacheDef;
import com.report.common.util.cache.Caches;
import com.report.module.im.exchange.IMReportExchange;
import com.report.module.im.pojo.vo.S2ReportResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

/**
 * 级联上报业务类
 */
@Slf4j
@Component
public class S2ReportBusiness {

    /** 上级MC地址缓存定义 */
    private static final CacheDef<String> IM_S2_BASE_URL = CacheDef.local("im:s2:base:url");

    private final IMReportExchange exchange;

    public S2ReportBusiness(HttpServiceProxyFactory httpServiceProxyFactory) {
        this.exchange = httpServiceProxyFactory.createClient(IMReportExchange.class);
    }

    /**
     * 上报告警文件（级联转发）
     *
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类
     * @param module             告警模块
     * @param alarmFileDescList  告警文件描述 JSON
     * @param alarmFileCountJsonObj 告警文件数量统计 JSON
     * @param files              告警文件列表
     * @return 响应结果
     */
    public S2ReportResultVo forwardAlarmFile(String userAgent, String cookie,
                                              String category, String module, String alarmFileDescList,
                                              String alarmFileCountJsonObj, List<MultipartFile> files) {
        String baseUrl = Caches.get(IM_S2_BASE_URL);
        log.info("级联上报告警文件, baseUrl={}", baseUrl);
        return exchange.alarmFileForward(baseUrl, userAgent, cookie, category, module,
                alarmFileDescList, alarmFileCountJsonObj, files).getBody();
    }

    /**
     * 上报告警消息（级联转发）
     *
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类
     * @param alarmCountJsonObj  告警消息数量统计 JSON
     * @param files              告警消息文件列表
     * @return 响应结果
     */
    public S2ReportResultVo forwardAlarm(String userAgent, String cookie,
                                         String category, String alarmCountJsonObj, List<MultipartFile> files) {
        String baseUrl = Caches.get(IM_S2_BASE_URL);
        log.info("级联上报告警消息, baseUrl={}", baseUrl);
        return exchange.alarmForward(baseUrl, userAgent, cookie, category, alarmCountJsonObj, files).getBody();
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add module/im/business/S2ReportBusiness.java
git commit -m "feat(im): 添加 S2ReportBusiness 级联上报业务类"
```

---

## Task 6: 编译验证

- [ ] **Step 1: 运行 Maven 编译验证**

Run: `mvn compile -q`
Expected: BUILD SUCCESS，无编译错误

- [ ] **Step 2: 运行测试验证**

Run: `mvn test -q`
Expected: BUILD SUCCESS

---

## 自检清单

1. **Spec 覆盖检查**:
   - [x] 依赖添加 - Task 1
   - [x] WebClientConfig 配置 - Task 2
   - [x] IMReportExchangeConstant 常量 - Task 3
   - [x] IMReportExchange 接口 - Task 4
   - [x] S2ReportBusiness 调用层 - Task 5
   - [x] 编译验证 - Task 6

2. **占位符检查**: 无 TBD/TODO

3. **类型一致性**:
   - `IMReportExchange.alarmFileForward()` 返回 `ResponseEntity<S2ReportResultVo>`，S2ReportBusiness 中 `.getBody()` 匹配
   - `Caches.get(IM_S2_BASE_URL)` 签名与 `CacheDef<String>` 泛型匹配
   - `CacheDef.local("im:s2:base:url")` 使用固定 key，符合设计
