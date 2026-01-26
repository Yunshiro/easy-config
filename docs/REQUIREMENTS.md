# easy-config 需求文档

## 1. 项目背景

### 1.1 痛点
- 多环境配置手动切换麻烦，每次切换需要重新编译打包
- 缺乏统一的配置管理界面
- Nacos等配置中心过于复杂，对于中小项目来说"重"了

### 1.2 目标
构建一个轻量级的配置管理工具，支持配置热更新，内置Web管理界面

---

## 2. 核心功能需求

### 2.1 配置管理（P0）
- 支持多环境配置（dev/test/prod等）
- 配置项的增删改查
- 配置历史版本管理（可追溯）
- 配置导入/导出（YAML/Properties格式）

### 2.2 配置热更新（P0）
- 应用无需重启即可获取最新配置
- 支持监听配置变化推送
- 提供SDK集成到Spring Boot应用

### 2.3 Web管理界面（P1）
- 登录认证（简单账号密码，暂无复杂权限）
- 可视化配置编辑
- 配置发布/回滚
- 环境切换

---

## 3. 非功能性需求

### 3.1 轻量级
- 启动时间 < 5秒
- 内存占用 < 256MB
- 单文件部署或简单jar包部署

### 3.2 可靠性
- 配置数据持久化（嵌入式数据库H2）
- 支持配置备份

### 3.3 易用性
- 5分钟内完成接入
- SDK提供简单注解（@EasyConfig）

---

## 4. MVP范围（最小可行产品）

### 4.1 必须包含（V0.1）
- [ ] 配置数据模型设计（Application + Environment + Key-Value）
- [ ] H2数据库集成
- [ ] 配置REST API（CRUD）
- [ ] Spring Boot SDK（支持配置读取 + 热更新监听）
- [ ] Web界面（基础CRUD + 环境切换）

### 4.2 可选（V0.2）
- [ ] 配置版本历史
- [ ] 配置导入/导出
- [ ] 配置变更日志

---

## 5. 技术架构建议

### 5.1 后端
- Spring Boot 3.x
- H2 Database（嵌入，零配置）
- WebSocket（配置推送）

### 5.2 前端
- Vue 3 + Vite（现代化开发体验）
- Element Plus UI组件库
- Pinia状态管理
- 前后端分离开发，构建后部署到服务端静态资源目录

### 5.3 SDK设计
```java
// 配置读取
@EasyConfig
@Value("${app.config.key}")
private String myConfig;

// 配置监听
@EasyConfigListener(keys = "app.config.key")
public void onConfigChange(ConfigChangeEvent event) {
    // 配置变更处理
}
```

---

## 6. 快速落地路线图

### Week 1: 基础框架
- 搭建Spring Boot项目结构
- H2数据库 + 配置表设计
- 配置REST API实现

### Week 2: SDK开发
- Spring Boot Starter实现
- 配置读取逻辑（Environment后置处理）
- WebSocket推送机制

### Week 3: Web界面
- 搭建Vue3 + Vite项目结构
- 实现配置列表/编辑页面
- 环境切换组件
- WebSocket实时配置变更通知

### Week 4: 测试 + 文档
- Demo示例项目
- 接入文档
- 基础测试

---

## 7. 与Nacos差异化定位

| 特性 | easy-config | Nacos |
|------|------------|-------|
| 部署复杂度 | 单jar包 | 需要独立集群 |
| 配置模型 | 简单KV | Namespace + Group |
| 注册中心 | ❌ | ✅ |
| 适用场景 | 单应用/微服务简单配置 | 大规模微服务治理 |

---

## 8. 风险与约束

### 8.1 已知风险
- 配置热更新可能导致应用状态不一致
- 分布式环境下需要配置服务高可用（暂时不考虑）

### 8.2 当前约束
- 仅支持Spring Boot应用
- 单机部署模式
- 暂不支持复杂权限控制
