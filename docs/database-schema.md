# Easy-Config 数据库设计文档

## 一、设计概述

### 1.1 设计目标

为 easy-config 轻量级配置管理工具设计一套简洁、高效、可扩展的数据库表结构，支持以下核心功能：
- 多环境配置管理（dev/test/prod等）
- 配置热更新（通过WebSocket推送）
- 配置版本管理（可追溯、可回滚）
- 配置导入/导出（YAML/Properties格式）

### 1.2 设计原则

1. **轻量级优先**：基于 H2 嵌入式数据库，表结构简洁，避免过度设计
2. **性能优化**：合理创建索引，确保查询效率
3. **扩展性考虑**：预留字段和表结构，支持未来功能扩展
4. **数据完整性**：通过外键和唯一约束保证数据一致性
5. **版本管理**：完整的配置版本追踪和历史记录

### 1.3 技术选型

- **数据库**：H2（轻量级、嵌入式、支持标准SQL）
- **连接模式**：嵌入式模式（无额外服务部署）
- **文件存储**：文件系统（数据持久化到本地文件）
- **事务隔离**：READ_COMMITTED（平衡性能和一致性）

---

## 二、ER图说明

### 2.1 表关系图（文字描述）

```
environment (1) ──────── (∞) config
     │                             │
     │                             │
     │                       (∞) config_version
     │
     │
 (∞) import_export_log

config (1) ──────── (∞) config_change_log
```

### 2.2 关系说明

- **environment → config**：一对多关系，一个环境包含多个配置项
- **config → config_version**：一对多关系，一个配置项有多个版本记录
- **config → config_change_log**：一对多关系，一个配置项有多条变更日志
- **environment → import_export_log**：一对多关系，一个环境有多次导入导出记录

---

## 三、表结构设计

### 3.1 environment 表（环境管理）

**设计理由**：支持多环境隔离是配置管理的基础需求，独立的环境表便于管理和扩展。

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|--------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| name | VARCHAR | 50 | NOT NULL, UNIQUE | 环境名称（如 dev、test、prod） |
| description | VARCHAR | 200 | NULL | 环境描述 |
| sort_order | INT | - | DEFAULT 0 | 排序序号（用于前端展示顺序） |
| created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- `idx_env_name`：唯一索引（name）

**设计考虑**：
- `name` 设置唯一约束，避免环境名称重复
- `sort_order` 字段支持前端自定义环境显示顺序
- 时间戳字段支持审计和追踪

---

### 3.2 config 表（配置管理）

**设计理由**：核心配置表，存储所有配置项的当前值，支持快速查询和更新。

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|--------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY, AUTO_INCREMENT | 主键 |
 | environment_id | BIGINT | - | NOT NULL, FOREIGN KEY | 环境ID，关联 environment 表 |
 | config_key | VARCHAR | 200 | NOT NULL | 配置键 |
 | config_value | CLOB | - | NULL | 配置值（大文本，支持复杂配置） |
| value_type | VARCHAR | 20 | DEFAULT 'STRING' | 值类型（STRING/NUMBER/BOOLEAN/JSON） |
| description | VARCHAR | 500 | NULL | 配置描述 |
| group_name | VARCHAR | 100 | NULL | 配置分组（用于组织相关配置） |
| encrypted | BOOLEAN | - | DEFAULT FALSE | 是否加密存储（敏感信息） |
| enabled | BOOLEAN | - | DEFAULT TRUE | 是否启用（软删除） |
| version | BIGINT | - | DEFAULT 1 | 当前版本号 |
| created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

**索引**：
- `idx_config_env_key`：联合唯一索引（environment_id + config_key）
- `idx_config_group`：普通索引（group_name）
- `idx_config_enabled`：普通索引（enabled）

**外键**：
- `fk_config_environment`：environment_id → environment(id)，级联删除

**设计考虑**：
- `environment_id + config_key` 联合唯一约束，确保同一环境内配置键不重复
- `config_value` 使用 CLOB 类型，支持存储复杂配置（如JSON、长文本）
- `value_type` 支持类型感知，便于前端校验和展示
- `encrypted` 标识敏感配置，支持加密存储（如密码、密钥）
- `enabled` 支持软删除，保留历史记录
- `version` 字段快速获取当前版本号，避免频繁查询版本表

---

### 3.3 config_version 表（配置版本管理）

**设计理由**：支持配置版本历史，实现配置回滚和变更追溯。

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|--------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY, AUTO_INCREMENT | 主键 |
 | config_id | BIGINT | - | NOT NULL, FOREIGN KEY | 配置ID，关联 config 表 |
 | version | BIGINT | - | NOT NULL | 版本号（递增） |
 | config_key | VARCHAR | 200 | NOT NULL | 配置键快照（冗余字段，提升查询性能） |
 | config_value | CLOB | - | NULL | 配置值快照 |
| operation | VARCHAR | 20 | NOT NULL | 操作类型（CREATE/UPDATE/DELETE） |
| operator | VARCHAR | 50 | NULL | 操作人标识 |
| change_reason | VARCHAR | 500 | NULL | 变更原因 |
| created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**：
- `idx_version_config`：普通索引（config_id）
- `idx_version_created`：普通索引（created_at）

**外键**：
- `fk_version_config`：config_id → config(id)，级联删除

**设计考虑**：
- `version` 字段递增，便于排序和回滚
- `config_key` 冗余字段避免频繁关联查询 config 表，提升性能
- `operation` 记录操作类型，支持完整的变更追踪
- `change_reason` 记录变更原因，便于审计和追溯
- 保留历史版本，支持任意版本回滚

---

### 3.4 config_change_log 表（配置变更日志）

**设计理由**：详细的变更日志，用于审计和问题排查。

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|--------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY, AUTO_INCREMENT | 主键 |
 | config_id | BIGINT | - | NOT NULL, FOREIGN KEY | 配置ID，关联 config 表 |
 | operation | VARCHAR | 20 | NOT NULL | 操作类型（CREATE/UPDATE/DELETE） |
 | old_config_value | CLOB | - | NULL | 变更前值 |
 | new_config_value | CLOB | - | NULL | 变更后值 |
| operator | VARCHAR | 50 | NULL | 操作人标识 |
| ip_address | VARCHAR | 45 | NULL | 操作来源IP（支持IPv6） |
| user_agent | VARCHAR | 500 | NULL | 操作来源User-Agent |
| change_reason | VARCHAR | 500 | NULL | 变更原因 |
| created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**：
- `idx_log_config`：普通索引（config_id）
- `idx_log_operation`：普通索引（operation）
- `idx_log_created`：普通索引（created_at）

**外键**：
- `fk_log_config`：config_id → config(id)，级联删除

**设计考虑**：
- `old_config_value` 和 `new_config_value` 记录变更前后值，便于对比和审计
- `ip_address` 和 `user_agent` 记录操作来源，增强安全性
- 独立于 config_version 表，提供更详细的操作日志
- 支持按时间、操作类型、配置项等多维度查询

---

### 3.5 import_export_log 表（导入导出日志）

**设计理由**：记录配置导入导出操作，便于追踪和问题排查。

| 字段名 | 类型 | 长度 | 约束 | 说明 |
|--------|------|------|------|------|
| id | BIGINT | - | PRIMARY KEY, AUTO_INCREMENT | 主键 |
| operation_type | VARCHAR | 20 | NOT NULL | 操作类型（IMPORT/EXPORT） |
| file_name | VARCHAR | 255 | NOT NULL | 文件名 |
| file_format | VARCHAR | 20 | NOT NULL | 文件格式（YAML/PROPERTIES/JSON） |
| environment_id | BIGINT | - | NULL, FOREIGN KEY | 环境ID（可选，导出时使用） |
| record_count | INT | - | NULL | 处理记录数 |
| status | VARCHAR | 20 | NOT NULL | 状态（SUCCESS/FAILED/PARTIAL） |
| error_message | CLOB | - | NULL | 错误信息（失败时） |
| file_path | VARCHAR | 500 | NULL | 文件存储路径 |
| operator | VARCHAR | 50 | NULL | 操作人标识 |
| created_at | TIMESTAMP | - | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引**：
- `idx_import_env`：普通索引（environment_id）
- `idx_import_type`：普通索引（operation_type）
- `idx_import_created`：普通索引（created_at）

**外键**：
- `fk_import_environment`：environment_id → environment(id)，级联删除

**设计考虑**：
- `file_format` 支持多种格式，便于未来扩展
- `environment_id` 可选，支持全局导出或按环境导出
- `status` 记录操作状态，支持成功、失败、部分成功
- `record_count` 记录处理记录数，便于统计和验证
- `file_path` 记录文件存储路径，便于下载和管理

---

## 四、设计决策与理由

### 4.1 为什么选择 CLOB 存储 config.config_value？

**决策**：使用 CLOB（大文本）类型存储配置值。

**理由**：
1. **灵活性**：支持存储复杂配置（如JSON、YAML、长文本）
2. **兼容性**：H2 支持 CLOB 类型，兼容标准SQL
3. **扩展性**：未来可支持配置模板、脚本等复杂场景

**权衡**：CLOB 查询性能略低于 VARCHAR，但配置值通常是整条读取，性能影响可接受。

---

### 4.2 为什么 config 表保留 version 字段？

**决策**：config 表保留 version 字段，冗余存储当前版本号。

**理由**：
1. **性能优化**：查询配置时无需关联 config_version 表，提升性能
2. **简化逻辑**：前端展示时无需额外查询版本信息
3. **一致性保证**：通过事务保证 config.version 与 config_version 同步

**权衡**：增加了少量存储冗余，但换来了显著的性能提升。

---

### 4.3 为什么分离 config_version 和 config_change_log？

**决策**：将配置版本表和变更日志表分离，而非合并。

**理由**：
1. **职责分离**：config_version 专注于版本管理，config_change_log 专注于审计日志
2. **查询优化**：版本查询和日志查询场景不同，分离后可针对性优化索引
3. **存储策略**：版本记录可能长期保留，日志记录可定期清理，分离后便于管理

**权衡**：增加了表数量，但提升了系统的可维护性和灵活性。

---

### 4.4 为什么使用软删除而非物理删除？

**决策**：config 表使用 enabled 字段实现软删除。

**理由**：
1. **数据安全**：避免误删导致配置丢失
2. **审计需求**：保留删除记录，便于审计和追溯
3. **恢复能力**：支持快速恢复误删的配置

**权衡**：软删除可能导致表中存在大量无效数据，需定期清理（如归档旧数据）。

---

### 4.5 为什么引入 group_name 字段？

**决策**：config 表引入 group_name 字段，支持配置分组。

**理由**：
1. **组织结构**：按业务逻辑组织配置，便于管理
2. **批量操作**：支持按组批量导入导出配置
3. **前端展示**：支持分组展示，提升用户体验

**权衡**：增加了字段，但提升了配置管理的易用性。

---

## 五、性能优化策略

### 5.1 索引设计

| 表名 | 索引名 | 字段 | 类型 | 说明 |
|------|--------|------|------|------|
| environment | idx_env_name | name | 唯一索引 | 快速查询环境 |
| config | idx_config_env_key | environment_id + config_key | 唯一索引 | 快速查询配置 |
| config | idx_config_group | group_name | 普通索引 | 按组查询配置 |
| config | idx_config_enabled | enabled | 普通索引 | 查询有效配置 |
| config_version | idx_version_config | config_id | 普通索引 | 查询配置版本 |
| config_version | idx_version_created | created_at | 普通索引 | 按时间查询版本 |
| config_change_log | idx_log_config | config_id | 普通索引 | 查询变更日志 |
| config_change_log | idx_log_operation | operation | 普通索引 | 按操作类型查询 |
| config_change_log | idx_log_created | created_at | 普通索引 | 按时间查询日志 |
| import_export_log | idx_import_env | environment_id | 普通索引 | 按环境查询日志 |
| import_export_log | idx_import_type | operation_type | 普通索引 | 按操作类型查询 |
| import_export_log | idx_import_created | created_at | 普通索引 | 按时间查询日志 |

### 5.2 查询优化建议

1. **配置查询**：优先使用 `environment_id + config_key` 唯一索引，避免全表扫描
2. **版本查询**：按 `config_id` 查询时使用索引，按时间排序时利用 `created_at` 索引
3. **日志查询**：按时间范围查询时利用 `created_at` 索引，避免范围扫描
4. **分组查询**：利用 `group_name` 索引，支持按组快速查询配置

### 5.3 存储优化

1. **CLOB 使用**：仅在必要时使用 CLOB，简单配置尽量使用 VARCHAR
2. **索引优化**：避免过度索引，影响写入性能
3. **定期清理**：定期清理过期的变更日志和导入导出日志

---

## 六、扩展性考虑

### 6.1 未来功能扩展

#### 6.1.1 配置加密增强

- **当前设计**：`encrypted` 字段标识敏感配置
- **扩展方案**：引入加密算法配置表，支持多种加密策略

#### 6.1.2 配置权限管理

- **扩展方案**：新增 `config_permission` 表，支持用户/角色级别的配置访问控制
- **字段设计**：config_id, user_role_id, permission_type（READ/WRITE）

#### 6.1.3 配置模板管理

- **扩展方案**：新增 `config_template` 表，支持配置模板定义和应用
- **字段设计**：template_name, template_content, description

#### 6.1.4 配置同步

- **扩展方案**：新增 `config_sync_log` 表，支持跨环境配置同步
- **字段设计**：source_env_id, target_env_id, sync_status, sync_time

### 6.2 数据归档策略

- **变更日志**：保留最近 6 个月日志，超过期限归档
- **版本历史**：保留最近 50 个版本，超过期限压缩或归档
- **导入导出日志**：保留最近 3 个月日志

### 6.3 分布式扩展

如果未来需要支持分布式部署，可考虑以下方案：
1. **引入注册中心**：使用轻量级注册中心（如 Consul）管理节点
2. **配置中心化**：将数据库迁移到外部数据库（如 MySQL）
3. **消息队列**：引入消息队列（如 RabbitMQ）处理配置变更通知

---

## 七、初始化 SQL

### 7.1 建表语句

```sql
-- 环境表
CREATE TABLE environment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_env_name UNIQUE (name)
);

-- 配置表
CREATE TABLE config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    config_key VARCHAR(200) NOT NULL,
    config_value CLOB,
    value_type VARCHAR(20) DEFAULT 'STRING',
    description VARCHAR(500),
    group_name VARCHAR(100),
    encrypted BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_config_environment FOREIGN KEY (environment_id) REFERENCES environment(id) ON DELETE CASCADE,
    CONSTRAINT uk_config_env_key UNIQUE (environment_id, config_key)
);

-- 配置版本表
CREATE TABLE config_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT NOT NULL,
    version BIGINT NOT NULL,
    config_key VARCHAR(200) NOT NULL,
    config_value CLOB,
    operation VARCHAR(20) NOT NULL,
    operator VARCHAR(50),
    change_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_version_config FOREIGN KEY (config_id) REFERENCES config(id) ON DELETE CASCADE
);

-- 配置变更日志表
CREATE TABLE config_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_id BIGINT NOT NULL,
    operation VARCHAR(20) NOT NULL,
    old_config_value CLOB,
    new_config_value CLOB,
    operator VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    change_reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_config FOREIGN KEY (config_id) REFERENCES config(id) ON DELETE CASCADE
);

-- 导入导出日志表
CREATE TABLE import_export_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(20) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_format VARCHAR(20) NOT NULL,
    environment_id BIGINT,
    record_count INT,
    status VARCHAR(20) NOT NULL,
    error_message CLOB,
    file_path VARCHAR(500),
    operator VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_environment FOREIGN KEY (environment_id) REFERENCES environment(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_env_name ON environment(name);
CREATE INDEX idx_config_group ON config(group_name);
CREATE INDEX idx_config_enabled ON config(enabled);
CREATE INDEX idx_version_config ON config_version(config_id);
CREATE INDEX idx_version_created ON config_version(created_at);
CREATE INDEX idx_log_config ON config_change_log(config_id);
CREATE INDEX idx_log_operation ON config_change_log(operation);
CREATE INDEX idx_log_created ON config_change_log(created_at);
CREATE INDEX idx_import_env ON import_export_log(environment_id);
CREATE INDEX idx_import_type ON import_export_log(operation_type);
CREATE INDEX idx_import_created ON import_export_log(created_at);
```

### 7.2 初始化数据

```sql
-- 初始化默认环境
INSERT INTO environment (name, description, sort_order, created_at, updated_at) VALUES
('dev', '开发环境', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('test', '测试环境', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod', '生产环境', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

---

## 八、总结

### 8.1 设计亮点

1. **轻量级**：仅 5 张表，结构简洁，符合 easy-config 的定位
2. **性能优化**：合理创建索引，支持高效查询
3. **扩展性强**：预留字段和表结构，支持未来功能扩展
4. **数据完整**：通过外键和唯一约束保证数据一致性
5. **审计完善**：完整的版本管理和变更日志，支持追溯和回滚

### 8.2 适用场景

- 中小型应用的配置管理
- 多环境配置隔离
- 配置热更新需求
- 配置版本管理需求
- 轻量级部署（单jar包）

### 8.3 不适用场景

- 超大规模配置管理（建议使用 Nacos）
- 复杂的分布式配置同步
- 高并发配置访问（建议引入缓存）

### 8.4 后续优化建议

1. **监控告警**：引入数据库性能监控，及时发现性能瓶颈
2. **数据归档**：定期归档历史数据，避免数据库膨胀
3. **缓存优化**：引入本地缓存（如 Caffeine）提升配置读取性能
4. **数据备份**：定期备份数据库文件，避免数据丢失

---

**文档版本**：v1.0
**创建日期**：2026-01-26
**作者**：Easy-Config Team
