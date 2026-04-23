# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目规范

**所有编码工作必须遵守 `doc/项目规范.md`。** 根据场景查阅对应章节：

| 场景 | 查阅章节 |
|---|---|
| 新建类、调整包结构 | 一、项目包结构 |
| 定义数据对象、转换逻辑 | 二、POJO 分类与数据流转 |
| 编写 Controller/Service/Mapper/Business | 三、分层调用规范 |
| MyBatis-Plus 相关 | 四、MyBatis-Plus 规范 |
| 接口返回值、异常处理 | 五、响应与异常规范 |
| Filter/Interceptor/Init/配置/工具类/注释日志 | 六、工程约束 |
| 编写测试用例 | 七、单元测试规范 |

**注释规范重点（详见 `doc/项目规范.md` 六、6.4 节）：** 代码注释必须详尽，包括步骤标记、WHY 注释、变量用途注释、操作意图注释。执行 simplify 等代码优化操作时，不得将这些业务注释视为冗余而删除。

## Build & Run

```bash
mvn spring-boot:run                # 启动应用（端口 8082）
mvn compile                        # 编译
mvn test                           # 运行所有测试
mvn test -Dtest=FooTest            # 运行单个测试类
mvn package -DskipTests            # 打包（跳过测试）
```

项目无 Maven wrapper 脚本，使用 `mvn` 命令。

## 技术栈

- Java 17 + Spring Boot 3.4.5
- 构建工具：Maven（pom.xml）
- ORM：MyBatis-Plus 3.5.12 + dynamic-datasource 4.3.1
- 数据库：MySQL 8.2.0（master/slave 双数据源）
- 缓存：Caffeine（本地）+ Redis（集群），统一通过 `CacheFactory` 静态方法访问
- 消息队列：Kafka
- POJO 转换：MapStruct（禁止手动 BeanUtil.copyProperties）
- 工具类：Hutool（优先使用）
- 配置加密：Jasypt
- 注解简化：Lombok

## 架构概览

### 产品形态与 Profile

项目支持三种产品形态，通过 Spring Profile 切换：

| Profile | 产品形态 | 数据源 | 组件扫描 |
|---|---|---|---|
| `product-im` | 接入口检测（IM） | master | `com.report.module.im` |
| `product-bm` | 保密监测（BM） | slave | `com.report.module.bm` |
| `product-all` | 统一管理（ALL） | master + slave | `com.report.module` |

部署方式通过 `deploy-single`（单机）/ `deploy-cluster`（集群）Profile 控制，影响 Redis 和数据源配置。`application.yml` 中的 `spring.profiles.active` 决定加载哪些配置文件。

### 模块加载机制

启动类 `Application` 上的 `@EnableModuleLoad` 注解触发 `ModuleLoadSelector`，在 Spring 容器初始化时读取 `product-xxx` Profile，设置 `ProductType` 静态缓存。同时通过 `@Profile` + `@ComponentScan` + `@MapperScan` 仅加载对应产品形态的 Bean 和 Mapper。`DataBaseConfig` 根据产品形态过滤动态数据源配置。

### 包结构

```
com.report
├── common/            # 通用基础（业务无关，可移植）
│   ├── config/        # 通用配置（线程池、调度器）
│   ├── exception/     # 通用异常
│   ├── response/      # 统一返回体（BaseResponse → Single/Multi/PageResponse）
│   ├── aop/           # 切面（分布式锁）
│   └── util/          # 工具类（缓存、Redis、Kafka、Shell、线程批处理）
└── module/
    ├── preload/       # 启动引导（产品形态选择、数据源配置）
    ├── im/            # IM 业务模块（按规范分层）
    └── bm/            # BM 业务模块（按规范分层）
```

### 分层调用

```
Controller / Task / Listener → Business → Service → Mapper
```

入口层（Controller/Task/Listener）必须通过 Business 层进入，禁止直接调用 Service 或 Mapper。各层 POJO 约束：Controller 用 Request/VO，Business 用 DTO/BO，Service 用 BO，Mapper 用 Entity。

### 缓存系统

`CacheFactory` 提供全局静态方法（`get`/`set`/`del`），通过 `CacheDef<T>` 定义缓存项，根据 `CacheScope`（LOCAL/CLUSTER）自动路由到 Caffeine 或 Redis。业务方无需关心底层存储。

### 配置文件

- `application.yml` — 主配置，激活 profiles
- `application-product-{im,bm,all}.yml` — 产品形态配置
- `application-database-mysql.yml` — 数据库与连接池配置（含 deploy-single/cluster 分块）
- 自定义配置统一收敛在 `zcenter.xxx` 前缀下
