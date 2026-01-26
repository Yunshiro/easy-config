-- 环境表
CREATE TABLE IF NOT EXISTS environment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_env_name UNIQUE (name)
);

-- 配置表
CREATE TABLE IF NOT EXISTS config (
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
CREATE TABLE IF NOT EXISTS config_version (
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
CREATE TABLE IF NOT EXISTS config_change_log (
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
CREATE TABLE IF NOT EXISTS import_export_log (
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
CREATE INDEX IF NOT EXISTS idx_env_name ON environment(name);
CREATE INDEX IF NOT EXISTS idx_config_group ON config(group_name);
CREATE INDEX IF NOT EXISTS idx_config_enabled ON config(enabled);
CREATE INDEX IF NOT EXISTS idx_version_config ON config_version(config_id);
CREATE INDEX IF NOT EXISTS idx_version_created ON config_version(created_at);
CREATE INDEX IF NOT EXISTS idx_log_config ON config_change_log(config_id);
CREATE INDEX IF NOT EXISTS idx_log_operation ON config_change_log(operation);
CREATE INDEX IF NOT EXISTS idx_log_created ON config_change_log(created_at);
CREATE INDEX IF NOT EXISTS idx_import_env ON import_export_log(environment_id);
CREATE INDEX IF NOT EXISTS idx_import_type ON import_export_log(operation_type);
CREATE INDEX IF NOT EXISTS idx_import_created ON import_export_log(created_at);