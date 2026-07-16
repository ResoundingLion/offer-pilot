# OfferPilot 项目配置与依赖

## 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 7.x |
| Docker & Docker Compose | 最新 |
| Node.js (前端) | 18+ |

## Maven 父工程

**groupId:** `com.offerpilot`
**artifactId:** `offer-pilot`
**version:** `1.0.0-SNAPSHOT`

## 依赖版本管理（父工程统一控制）

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.x | 基础框架 |
| Spring Cloud Alibaba | 2023.0.x | 微服务生态 |
| Spring Cloud | 2023.0.x | 微服务基础 |
| Nacos | 2.3.x | 注册/配置中心 |
| MyBatis-Plus | 3.5.5+ | ORM |
| MySQL Connector | 8.0.x | 数据库驱动 |
| Knife4j | 4.5.x | API 文档 |
| Hutool | 5.8.x | 工具集 |
| Redisson | 3.30+ | Redis 客户端 |
| Spring Cloud Gateway | 3.2.x | Gateway |
| OpenFeign | 4.x | 声明式调用 |

> **为什么不写死版本号：** 父工程 `<dependencyManagement>` 统一锁定版本，子模块引用时不写版本。更换版本只需改父 pom。

## 模块结构

```
offer-pilot (父工程)
 ├── offer-common        — 公共模块：Result、异常、工具类、常量
 ├── offer-api           — OpenFeign 接口定义（DTO + Client）
 ├── offer-gateway       — 网关服务
 ├── offer-auth          — 认证服务
 ├── offer-user          — 用户/公司/岗位服务
 ├── offer-application   — 投递/面试/Offer 服务
 ├── offer-notification  — 通知服务（MVP 后）
 ├── docker-compose.yml   — 一键启动环境
 └── sql/                 — 数据库初始化脚本
```

## 关键配置约定

- 每个服务使用 `bootstrap.yml` 引导从 Nacos 拉取配置
- 配置中心命名规则：`{服务名}-{profile}.yml`
- 统一前缀路径：`/api/{资源名}`
- 日志使用 Logback，输出到 `logs/{服务名}/` 目录

## 编码依赖

- Lombok：各模块通用（注解简化代码）
- Hutool：字符串、集合、加解密等工具类
- Knife4j：Swagger 增强，接口文档可视化

## 构建命令

```bash
# 编译全部
mvn clean compile

# 打包（跳过测试）
mvn clean package -DskipTests

# 启动某个模块（以 offer-user 为例）
mvn -pl offer-user -am spring-boot:run
```

### 要点说明

所有版本号在父 pom 里统一锁定，子模块不写版本。这样升级某个框架（比如 Spring Boot 3.3）只改父 pom 一处，不会出现各模块版本打架的问题。
