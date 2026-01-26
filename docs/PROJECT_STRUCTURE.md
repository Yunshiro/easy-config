# easy-config 项目目录结构

## 1. 整体结构

```
easy-config/
├── easy-config-server/              # 服务端模块（REST API + WebSocket服务）
├── easy-config-web/                 # Vue3 Web前端模块（管理界面）
├── easy-config-sdk/                 # SDK模块（Spring Boot Starter）
├── easy-config-samples/             # 示例项目
│   ├── simple-example/              # 简单示例
│   └── hot-reload-example/          # 热更新示例
├── docs/                            # 项目文档
├── pom.xml                          # 父POM
├── README.md
└── REQUIREMENTS.md
```

---

## 2. 服务端模块 (easy-config-server)

配置管理中心，提供REST API和WebSocket服务。

```
easy-config-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── easyconfig/
│   │   │           └── server/
│   │   │               ├── ServerApplication.java           # 启动类
│   │   │               ├── config/                          # 配置类
│   │   │               │   ├── DatabaseConfig.java
│   │   │               │   ├── WebSocketConfig.java
│   │   │               │   └── CorsConfig.java              # CORS配置
│   │   │               ├── controller/                      # REST API控制器
│   │   │               │   ├── ConfigController.java        # 配置管理API
│   │   │               │   ├── EnvironmentController.java   # 环境管理API
│   │   │               │   └── WebSocketController.java    # WebSocket接口
│   │   │               ├── service/                         # 业务逻辑
│   │   │               │   ├── ConfigService.java
│   │   │               │   ├── EnvironmentService.java
│   │   │               │   └── PushService.java            # WebSocket推送
│   │   │               ├── repository/                      # 数据访问
│   │   │               │   ├── ConfigRepository.java
│   │   │               │   └── EnvironmentRepository.java
│   │   │               ├── entity/                         # 实体类
│   │   │               │   ├── Config.java
│   │   │               │   └── Environment.java
│   │   │               └── dto/                             # 数据传输对象
│   │   │                   ├── ConfigDTO.java
│   │   │                   ├── EnvironmentDTO.java
│   │   │                   └── ApiResponse.java
│   │   ├── resources/
│   │   │   ├── application.yml                              # 配置文件
│   │   │   ├── db/
│   │   │   │   └── schema.sql                               # 数据库初始化脚本
│   │   │   └── static/                                       # Vue3构建后的静态资源
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── easyconfig/
│   │                   └── server/
│   │                       └── service/
│   │                           └── ConfigServiceTest.java
│   └── pom.xml
└── Dockerfile                                               # 容器化部署
```

---

## 3. Web前端模块 (easy-config-web)

Vue3管理界面，提供可视化的配置管理功能。

```
easy-config-web/
├── src/
│   ├── api/                          # API调用
│   │   ├── config.js                 # 配置相关API
│   │   └── environment.js           # 环境相关API
│   ├── assets/                       # 静态资源
│   │   ├── images/
│   │   └── styles/
│   ├── components/                   # 公共组件
│   │   ├── ConfigTable.vue           # 配置表格
│   │   ├── EnvironmentSelector.vue   # 环境选择器
│   │   └── ConfirmDialog.vue         # 确认对话框
│   ├── layouts/                      # 布局组件
│   │   └── MainLayout.vue            # 主布局
│   ├── router/                       # 路由配置
│   │   └── index.js
│   ├── stores/                       # 状态管理（Pinia）
│   │   ├── config.js
│   │   └── environment.js
│   ├── views/                        # 页面组件
│   │   ├── Dashboard.vue             # 仪表盘
│   │   ├── ConfigList.vue            # 配置列表
│   │   ├── ConfigEdit.vue            # 配置编辑
│   │   └── EnvironmentManage.vue     # 环境管理
│   ├── App.vue                       # 根组件
│   └── main.js                       # 入口文件
├── public/                           # 公共静态资源
│   ├── favicon.ico
│   └── index.html
├── .env.development                  # 开发环境变量
├── .env.production                   # 生产环境变量
├── vite.config.js                    # Vite配置
├── package.json
└── pnpm-lock.yaml / package-lock.json
```

---

## 4. SDK模块 (easy-config-sdk)

Spring Boot Starter，供客户端应用集成。

```
easy-config-sdk/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── easyconfig/
│   │   │           └── sdk/
│   │   │               ├── EasyConfigAutoConfiguration.java   # 自动配置类
│   │   │               ├── annotation/                         # 注解
│   │   │               │   ├── EasyConfig.java
│   │   │               │   ├── EasyConfigListener.java
│   │   │               │   └── EnableEasyConfig.java
│   │   │               ├── client/                             # 客户端
│   │   │               │   ├── ConfigClient.java               # HTTP客户端
│   │   │               │   └── WebSocketClient.java           # WebSocket客户端
│   │   │               ├── listener/                           # 监听器
│   │   │               │   ├── ConfigChangeListener.java
│   │   │               │   └── ConfigChangeEvent.java
│   │   │               ├── refresh/                            # 配置刷新
│   │   │               │   ├── ConfigRefresher.java
│   │   │               │   └── ConfigRefreshScope.java
│   │   │               ├── properties/                         # 配置属性
│   │   │               │   └── EasyConfigProperties.java
│   │   │               └── util/
│   │   │                   └── SpringContextUtil.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── spring/
│   │               └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/
│       └── java/
│           └── com/
│               └── easyconfig/
│                   └── sdk/
│                       └── ConfigClientTest.java
└── pom.xml
```

---

## 5. 示例模块 (easy-config-samples)

展示如何使用SDK的示例项目。

```
easy-config-samples/
├── simple-example/                     # 简单示例
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── example/
│   │       │           └── simple/
│   │       │               ├── SimpleApplication.java
│   │       │               ├── controller/
│   │       │               │   └── DemoController.java
│   │       │               └── service/
│   │       │                   └── DemoService.java
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
└── hot-reload-example/                 # 热更新示例
    ├── src/
    │   └── main/
    │       ├── java/
    │       │   └── com/
    │       │       └── example/
    │       │           └── reload/
    │       │               ├── HotReloadApplication.java
    │       │               ├── controller/
    │       │               │   └── ConfigController.java
    │       │               ├── listener/
    │       │               │   └── MyConfigListener.java
    │       │               └── service/
    │       │                   └── DynamicConfigService.java
    │       └── resources/
    │           └── application.yml
    └── pom.xml
```

---

## 6. 文档模块 (docs)

项目文档。

```
docs/
├── architecture/                       # 架构文档
│   ├── architecture.md                  # 整体架构
│   └── sequence-diagram.md              # 时序图
├── api/                                # API文档
│   ├── rest-api.md                     # REST API
│   └── websocket-api.md                # WebSocket API
├── user-guide/                         # 用户指南
│   ├── quick-start.md                  # 快速开始
│   ├── installation.md                 # 安装部署
│   ├── sdk-integration.md              # SDK集成
│   └── web-interface.md                # Web界面使用
├── development/                        # 开发文档
│   ├── development-guide.md             # 开发指南
│   ├── frontend-development.md         # 前端开发指南
│   ├── database-schema.md              # 数据库设计
│   └── contributing.md                 # 贡献指南
└── images/                             # 文档图片
    ├── architecture.png
    └── demo.png
```

---

## 7. 根目录文件

```
easy-config/
├── pom.xml                             # 父POM（多模块管理）
├── README.md                           # 项目说明
├── REQUIREMENTS.md                     # 需求文档
├── PROJECT_STRUCTURE.md                # 本文档
├── .gitignore                          # Git忽略文件
├── LICENSE                             # 开源协议
└── build.sh                            # 完整构建脚本（包含前端）
```

---

## 8. 模块依赖关系

```
┌─────────────────────────────────────────┐
│         easy-config-web (Vue3)         │
│         (管理界面 + WebSocket客户端)      │
└─────────────────────────────────────────┘
                    ↕ REST API / WebSocket
┌─────────────────────────────────────────┐
│         easy-config-server              │
│    (REST API + WebSocket服务)           │
└─────────────────────────────────────────┘
                    ↓ WebSocket
┌─────────────────────────────────────────┐
│           easy-config-sdk               │
│      (Spring Boot Starter客户端)        │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         easy-config-samples            │
│           (示例应用)                    │
└─────────────────────────────────────────┘
```

---

## 9. Maven父POM示例

```xml
<project>
    <groupId>com.easyconfig</groupId>
    <artifactId>easy-config</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>easy-config-server</module>
        <module>easy-config-sdk</module>
        <module>easy-config-samples</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.0</spring-boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

---

## 10. Vue3 Vite配置示例

```javascript
// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  },
  build: {
    outDir: '../easy-config-server/src/main/resources/static',
    emptyOutDir: true
  }
})
```

---

## 11. Vue3依赖包 (package.json)

```json
{
  "name": "easy-config-web",
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0",
    "element-plus": "^2.4.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

---

## 12. 数据库表设计 (H2)

### environment（环境表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 环境名称（dev/test/prod） |
| description | VARCHAR(255) | 环境描述 |
| created_at | TIMESTAMP | 创建时间 |

### config（配置表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| environment_id | BIGINT | 环境ID |
| key | VARCHAR(255) | 配置键 |
| value | TEXT | 配置值 |
| version | INT | 版本号 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

---

## 13. 快速开始命令

```bash
# 方式1：分别启动（开发模式推荐）

# 1. 启动后端服务
cd easy-config-server
mvn spring-boot:run

# 2. 启动前端开发服务器（新终端）
cd easy-config-web
pnpm install          # 或 npm install
pnpm dev              # 启动Vite开发服务器（端口3000）

# 方式2：完整构建（生产模式）

# 构建整个项目（包含前后端）
./build.sh

# 或手动执行
cd easy-config-web && pnpm build
cd ../easy-config-server && mvn clean package
java -jar target/easy-config-server-0.1.0-SNAPSHOT.jar
```

---

## 14. 前后端分离说明

### 开发模式
- 前端：Vite开发服务器（http://localhost:3000），通过代理访问后端API
- 后端：Spring Boot服务（http://localhost:8080），提供REST API
- 优势：前端热更新，开发体验好

### 生产模式
- 前端构建后输出到 `easy-config-server/src/main/resources/static/`
- Spring Boot直接提供静态资源服务
- 单一jar包部署，符合轻量级目标
- 访问入口：http://localhost:8080/

### API通信
- 所有API请求统一前缀 `/api`
- WebSocket连接端点 `/ws`
- 前端使用axios进行HTTP通信
- 前端使用原生WebSocket或库建立长连接
