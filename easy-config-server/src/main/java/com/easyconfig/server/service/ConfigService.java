package com.easyconfig.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easyconfig.server.entity.Config;
import com.easyconfig.server.entity.Environment;
import com.easyconfig.server.repository.ConfigRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 配置服务类
 * <p>
 * 提供配置的完整 CRUD 操作，包括：
 * - 配置创建、更新、删除（软删除）
 * - 配置查询（按 ID、环境、分组、类型等）
 * - 配置搜索（键名、描述）
 * - 配置启用/禁用
 * - 配置统计
 */
@Service
@Transactional
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private EnvironmentService environmentService;

    /**
     * 创建配置
     *
     * @param config 配置实体
     * @return 创建后的配置实体
     * @throws IllegalArgumentException 如果环境ID无效或配置键已存在
     */
    public Config createConfig(Config config) {
        // 验证环境是否存在
        if (config.getEnvironment() == null || config.getEnvironment().getId() == null) {
            throw new IllegalArgumentException("Environment ID is required");
        }

        Environment environment = environmentService.getEnvironmentById(config.getEnvironment().getId());

        // 检查同一环境下配置键是否已存在
        Optional<Config> existingConfig = configRepository.findByEnvironmentIdAndConfigKey(
                environment.getId(), config.getConfigKey());
        if (existingConfig.isPresent()) {
            throw new IllegalArgumentException(
                    "Config with key '" + config.getConfigKey() + "' already exists in environment '"
                            + environment.getName() + "'");
        }

        // 设置默认值
        if (config.getValueType() == null) {
            config.setValueType("STRING");
        }
        if (config.getEncrypted() == null) {
            config.setEncrypted(false);
        }
        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        if (config.getVersion() == null) {
            config.setVersion(1L);
        }

        config.setEnvironment(environment);

        return configRepository.save(config);
    }

    /**
     * 更新配置
     *
     * @param id    配置ID
     * @param config 更新的配置实体
     * @return 更新后的配置实体
     * @throws EntityNotFoundException 如果配置不存在
     * @throws IllegalArgumentException 如果新键名与其他配置冲突
     */
    public Config updateConfig(Long id, Config config) {
        Config existingConfig = getConfigById(id);

        // 验证环境是否存在
        if (config.getEnvironment() != null && config.getEnvironment().getId() != null) {
            Environment environment = environmentService.getEnvironmentById(config.getEnvironment().getId());
            existingConfig.setEnvironment(environment);
        }

        // 如果修改了配置键，检查新键名是否与同一环境下的其他配置冲突
        if (config.getConfigKey() != null && !config.getConfigKey().equals(existingConfig.getConfigKey())) {
            Optional<Config> configWithSameKey = configRepository.findByEnvironmentIdAndConfigKey(
                    existingConfig.getEnvironment().getId(), config.getConfigKey());
            if (configWithSameKey.isPresent() && !configWithSameKey.get().getId().equals(id)) {
                throw new IllegalArgumentException(
                        "Config with key '" + config.getConfigKey() + "' already exists in environment '"
                                + existingConfig.getEnvironment().getName() + "'");
            }
            existingConfig.setConfigKey(config.getConfigKey());
        }

        // 更新字段
        if (config.getConfigValue() != null) {
            existingConfig.setConfigValue(config.getConfigValue());
        }
        if (config.getValueType() != null) {
            existingConfig.setValueType(config.getValueType());
        }
        if (config.getDescription() != null) {
            existingConfig.setDescription(config.getDescription());
        }
        if (config.getGroupName() != null) {
            existingConfig.setGroupName(config.getGroupName());
        }
        if (config.getEncrypted() != null) {
            existingConfig.setEncrypted(config.getEncrypted());
        }
        if (config.getEnabled() != null) {
            existingConfig.setEnabled(config.getEnabled());
        }

        // 更新版本号
        existingConfig.setVersion(existingConfig.getVersion() + 1);

        return configRepository.save(existingConfig);
    }

    /**
     * 删除配置（物理删除）
     *
     * @param id 配置ID
     * @throws EntityNotFoundException 如果配置不存在
     */
    public void deleteConfig(Long id) {
        if (!configRepository.existsById(id)) {
            throw new EntityNotFoundException("Config with id '" + id + "' not found");
        }
        configRepository.deleteById(id);
    }

    /**
     * 启用配置（软删除）
     *
     * @param id 配置ID
     * @return 更新后的配置实体
     * @throws EntityNotFoundException 如果配置不存在
     */
    public Config enableConfig(Long id) {
        Config config = getConfigById(id);
        config.setEnabled(true);
        return configRepository.save(config);
    }

    /**
     * 禁用配置（软删除）
     *
     * @param id 配置ID
     * @return 更新后的配置实体
     * @throws EntityNotFoundException 如果配置不存在
     */
    public Config disableConfig(Long id) {
        Config config = getConfigById(id);
        config.setEnabled(false);
        return configRepository.save(config);
    }

    /**
     * 根据 ID 获取配置
     *
     * @param id 配置ID
     * @return 配置实体
     * @throws EntityNotFoundException 如果配置不存在
     */
    public Config getConfigById(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Config with id '" + id + "' not found"));
    }

    /**
     * 根据环境ID和配置键获取配置
     *
     * @param environmentId 环境ID
     * @param configKey     配置键
     * @return 配置实体
     * @throws EntityNotFoundException 如果配置不存在
     */
    public Config getConfigByKey(Long environmentId, String configKey) {
        return configRepository.findByEnvironmentIdAndConfigKey(environmentId, configKey)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Config with key '" + configKey + "' not found in environment '" + environmentId + "'"));
    }

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    public List<Config> getAllConfigs() {
        return configRepository.findAll();
    }

    /**
     * 获取指定环境的所有配置
     *
     * @param environmentId 环境ID
     * @return 配置列表
     */
    public List<Config> getConfigsByEnvironment(Long environmentId) {
        return configRepository.findByEnvironmentId(environmentId);
    }

    /**
     * 获取指定环境的所有启用配置
     *
     * @param environmentId 环境ID
     * @return 配置列表
     */
    public List<Config> getEnabledConfigsByEnvironment(Long environmentId) {
        return configRepository.findByEnvironmentIdAndEnabledTrue(environmentId);
    }

    /**
     * 获取所有加密配置
     *
     * @return 配置列表
     */
    public List<Config> getEncryptedConfigs() {
        return configRepository.findByEncryptedTrue();
    }

    /**
     * 获取指定环境的加密配置
     *
     * @param environmentId 环境ID
     * @return 配置列表
     */
    public List<Config> getEncryptedConfigsByEnvironment(Long environmentId) {
        return configRepository.findByEnvironmentIdAndEncryptedTrue(environmentId);
    }

    /**
     * 根据配置键模糊查询配置
     *
     * @param configKey 配置键（支持模糊匹配）
     * @return 配置列表
     */
    public List<Config> searchConfigsByKey(String configKey) {
        return configRepository.findByConfigKeyContaining(configKey);
    }

    /**
     * 根据描述模糊查询配置
     *
     * @param keyword 关键词
     * @return 配置列表
     */
    public List<Config> searchConfigsByDescription(String keyword) {
        return configRepository.findByDescriptionContaining(keyword);
    }

    /**
     * 根据环境ID和描述模糊查询配置
     *
     * @param environmentId 环境ID
     * @param keyword      关键词
     * @return 配置列表
     */
    public List<Config> searchConfigsByEnvironmentAndDescription(Long environmentId, String keyword) {
        return configRepository.findByEnvironmentIdAndDescriptionContaining(environmentId, keyword);
    }

    /**
     * 根据分组名称查询配置
     *
     * @param groupName 分组名称
     * @return 配置列表
     */
    public List<Config> getConfigsByGroup(String groupName) {
        return configRepository.findByGroupName(groupName);
    }

    /**
     * 根据环境ID和分组名称查询配置
     *
     * @param environmentId 环境ID
     * @param groupName     分组名称
     * @return 配置列表
     */
    public List<Config> getConfigsByEnvironmentAndGroup(Long environmentId, String groupName) {
        return configRepository.findByEnvironmentIdAndGroupName(environmentId, groupName);
    }

    /**
     * 根据值类型查询配置
     *
     * @param valueType 值类型（STRING/NUMBER/BOOLEAN/JSON）
     * @return 配置列表
     */
    public List<Config> getConfigsByType(String valueType) {
        return configRepository.findByValueType(valueType);
    }

    /**
     * 根据环境ID和值类型查询配置
     *
     * @param environmentId 环境ID
     * @param valueType     值类型
     * @return 配置列表
     */
    public List<Config> getConfigsByEnvironmentAndType(Long environmentId, String valueType) {
        return configRepository.findByEnvironmentIdAndValueType(environmentId, valueType);
    }

    /**
     * 获取所有分组名称（去重）
     *
     * @return 分组名称列表
     */
    public List<String> getAllDistinctGroups() {
        return configRepository.findAllDistinctGroupNames();
    }

    /**
     * 统计指定环境的配置数量
     *
     * @param environmentId 环境ID
     * @return 配置数量
     */
    public long getConfigCountByEnvironment(Long environmentId) {
        return configRepository.countByEnvironmentId(environmentId);
    }

    /**
     * 统计指定环境的启用配置数量
     *
     * @param environmentId 环境ID
     * @return 启用配置数量
     */
    public long getEnabledConfigCountByEnvironment(Long environmentId) {
        return configRepository.countByEnvironmentIdAndEnabledTrue(environmentId);
    }

    /**
     * 统计指定环境的禁用配置数量
     *
     * @param environmentId 环境ID
     * @return 禁用配置数量
     */
    public long getDisabledConfigCountByEnvironment(Long environmentId) {
        return configRepository.countByEnvironmentIdAndEnabledFalse(environmentId);
    }

    /**
     * 统计指定环境的加密配置数量
     *
     * @param environmentId 环境ID
     * @return 加密配置数量
     */
    public long getEncryptedConfigCountByEnvironment(Long environmentId) {
        return configRepository.countByEnvironmentIdAndEncryptedTrue(environmentId);
    }

    /**
     * 根据创建时间范围查询配置
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 配置列表
     */
    public List<Config> getConfigsByCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return configRepository.findByCreatedAtBetween(start, end);
    }

    /**
     * 根据环境ID和创建时间范围查询配置
     *
     * @param environmentId 环境ID
     * @param start         开始时间
     * @param end           结束时间
     * @return 配置列表
     */
    public List<Config> getConfigsByEnvironmentAndCreatedBetween(Long environmentId, LocalDateTime start,
            LocalDateTime end) {
        return configRepository.findByEnvironmentIdAndCreatedAtBetween(environmentId, start, end);
    }

    /**
     * 根据更新时间范围查询配置
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 配置列表
     */
    public List<Config> getConfigsByUpdatedBetween(LocalDateTime start, LocalDateTime end) {
        return configRepository.findByUpdatedAtBetween(start, end);
    }

    /**
     * 根据环境ID和更新时间范围查询配置
     *
     * @param environmentId 环境ID
     * @param start         开始时间
     * @param end           结束时间
     * @return 配置列表
     */
    public List<Config> getConfigsByEnvironmentAndUpdatedBetween(Long environmentId, LocalDateTime start,
            LocalDateTime end) {
        return configRepository.findByEnvironmentIdAndUpdatedAtBetween(environmentId, start, end);
    }

    /**
     * 根据环境ID、启用状态和分组查询配置
     *
     * @param environmentId 环境ID
     * @param enabled       启用状态
     * @param groupName     分组名称
     * @return 配置列表
     */
    public List<Config> getConfigsByEnvironmentAndEnabledAndGroup(Long environmentId, Boolean enabled,
            String groupName) {
        return configRepository.findByEnvironmentIdAndEnabledAndGroupName(environmentId, enabled, groupName);
    }

    /**
     * 根据环境ID、启用状态和加密状态查询配置
     *
     * @param environmentId 环境ID
     * @param enabled       启用状态
     * @param encrypted     加密状态
     * @return 配置列表
     */
    public List<Config> getConfigsByEnvironmentAndEnabledAndEncrypted(Long environmentId, Boolean enabled,
            Boolean encrypted) {
        return configRepository.findByEnvironmentIdAndEnabledAndEncrypted(environmentId, enabled, encrypted);
    }

    /**
     * 根据环境ID和启用状态查询配置（按创建时间倒序）
     *
     * @param environmentId 环境ID
     * @param enabled       启用状态
     * @return 配置列表（按创建时间倒序）
     */
    public List<Config> getConfigsByEnvironmentAndEnabledOrderByCreatedDesc(Long environmentId, Boolean enabled) {
        return configRepository.findByEnvironmentIdAndEnabledOrderByCreatedAtDesc(environmentId, enabled);
    }

    /**
     * 根据环境ID查询配置（按更新时间倒序）
     *
     * @param environmentId 环境ID
     * @return 配置列表（按更新时间倒序）
     */
    public List<Config> getConfigsByEnvironmentOrderByUpdatedDesc(Long environmentId) {
        return configRepository.findByEnvironmentIdOrderByUpdatedAtDesc(environmentId);
    }

    /**
     * 检查配置键是否在指定环境中存在
     *
     * @param environmentId 环境ID
     * @param configKey     配置键
     * @return true 表示存在，false 表示不存在
     */
    public boolean existsByEnvironmentAndKey(Long environmentId, String configKey) {
        return configRepository.findByEnvironmentIdAndConfigKey(environmentId, configKey).isPresent();
    }

    /**
     * 检查配置ID是否存在
     *
     * @param id 配置ID
     * @return true 表示存在，false 表示不存在
     */
    public boolean existsById(Long id) {
        return configRepository.existsById(id);
    }
}
