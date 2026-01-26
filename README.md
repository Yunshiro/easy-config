# easy-config

## 文档

- [数据库设计文档](docs/database-schema.md)
- [项目结构文档](docs/PROJECT_STRUCTURE.md)
- [需求文档](docs/REQUIREMENTS.md)
## 技术栈
Java
Spring Boot

## 要解决的问题
1. 多环境配置手动切换麻烦，并且每次都需要重新编译代码，实现热部署更新配置
2. 内置Web管理配置页面，但足够轻量，比nacos配置简单

## 阶段性工作
第一阶段：服务端基础（Week 1）
1. 数据库 + 实体层
- 添加H2数据库依赖
- 创建 Environment 和 Config 实体
- 创建 Repository 接口
- 编写 schema.sql 初始化脚本
2. REST API
- 添加 Web、JPA、WebSocket 依赖
- 创建 Controller（ConfigController、EnvironmentController）
- 创建 Service 层
- 实现增删改查接口
3. 配置热更新基础
- 配置 WebSocket
- 实现 PushService（配置变化推送）
---
第二阶段：SDK开发（Week 2）
4. SDK客户端
- 创建 HTTP 客户端（ConfigClient）
- 创建 WebSocket 客户端（WebSocketClient）
- 实现配置拉取逻辑
5. Spring Boot集成
- 创建自动配置类
- 实现配置刷新机制（@RefreshScope 或自定义）
- 添加注解（@EasyConfig、@EasyConfigListener）
---
第三阶段：Web前端（Week 3）
6. Vue3管理界面
- 创建配置列表页面（ConfigList.vue）
- 创建配置编辑页面（ConfigEdit.vue）
- 创建环境管理页面（EnvironmentManage.vue）
- 实现 API 调用封装（api/config.js、api/environment.js）
7. WebSocket集成
- 前端监听配置变化推送
- 实时更新UI
---