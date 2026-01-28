package com.easyconfig.server.repository;

import com.easyconfig.server.entity.Config;
import com.easyconfig.server.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {

    /**
     * 根据环境实体和配置键查询配置
     * <p>
     * 对应唯一约束：uk_config_env_key
     *
     * @param environment 环境实体
     * @param configKey   配置键
     * @return 配置对象（如果存在）
     */
    Optional<Config> findByEnvironmentAndConfigKey(Environment environment, String configKey);

    /**
     * 根据环境ID和配置键查询配置
     *
     * @param environmentId 环境ID
     * @param configKey     配置键
     * @return 配置对象（如果存在）
     */
    Optional<Config> findByEnvironmentIdAndConfigKey(Long environmentId, String configKey);

    /**
     * 根据配置键模糊查询所有环境的配置
     *
     * @param configKey 配置键（支持模糊匹配）
     * @return 配置列表
     */
    List<Config> findByConfigKeyContaining(String configKey);

    /**
     * 根据环境ID查询所有配置
     *
     * @param environmentId 环境ID
     * @return 配置列表
     */
    List<Config> findByEnvironmentId(Long environmentId);

    /**
     * 根据环境实体查询所有配置
     *
     * @param environment 环境实体
     * @return 配置列表
     */
    List<Config> findByEnvironment(Environment environment);

    /**
     * 根据环境ID和分组名称查询配置
     *
     * @param environmentId 环境ID
     * @param groupName     分组名称
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndGroupName(Long environmentId, String groupName);

    /**
     * 根据分组名称查询所有环境的配置
     *
     * @param groupName 分组名称
     * @return 配置列表
     */
    List<Config> findByGroupName(String groupName);

    /**
     * 查询所有分组名称（去重）
     *
     * @return 分组名称列表
     */
    @Query("SELECT DISTINCT c.groupName FROM Config c WHERE c.groupName IS NOT NULL")
    List<String> findAllDistinctGroupNames();

    /**
     * 查询所有启用的配置
     *
     * @return 配置列表
     */
    List<Config> findByEnabledTrue();

    /**
     * 查询所有禁用的配置
     *
     * @return 配置列表
     */
    List<Config> findByEnabledFalse();

    /**
     * 根据环境ID查询启用的配置
     *
     * @param environmentId 环境ID
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndEnabledTrue(Long environmentId);

    /**
     * 查询所有加密的配置
     *
     * @return 配置列表
     */
    List<Config> findByEncryptedTrue();

    /**
     * 根据环境ID查询加密的配置
     *
     * @param environmentId 环境ID
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndEncryptedTrue(Long environmentId);

    /**
     * 根据值类型查询配置
     *
     * @param valueType 值类型（STRING/NUMBER/BOOLEAN/JSON）
     * @return 配置列表
     */
    List<Config> findByValueType(String valueType);

    /**
     * 根据环境ID和值类型查询配置
     *
     * @param environmentId 环境ID
     * @param valueType     值类型
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndValueType(Long environmentId, String valueType);

    /**
     * 根据环境ID和版本号查询配置
     *
     * @param environmentId 环境ID
     * @param version       版本号
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndVersion(Long environmentId, Long version);

    /**
     * 根据创建时间范围查询配置
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 配置列表
     */
    List<Config> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 根据环境ID和创建时间范围查询配置
     *
     * @param environmentId 环境ID
     * @param start         开始时间
     * @param end           结束时间
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndCreatedAtBetween(Long environmentId, LocalDateTime start, LocalDateTime end);

    /**
     * 根据更新时间范围查询配置
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 配置列表
     */
    List<Config> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 根据环境ID和更新时间范围查询配置
     *
     * @param environmentId 环境ID
     * @param start         开始时间
     * @param end           结束时间
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndUpdatedAtBetween(Long environmentId, LocalDateTime start, LocalDateTime end);

    /**
     * 根据环境、启用状态和分组查询配置
     *
     * @param environmentId 环境ID
     * @param enabled       启用状态
     * @param groupName     分组名称
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndEnabledAndGroupName(Long environmentId, Boolean enabled, String groupName);

    /**
     * 根据环境ID、启用状态和加密状态查询配置
     *
     * @param environmentId 环境ID
     * @param enabled       启用状态
     * @param encrypted     加密状态
     * @return 配置列表
     */
    List<Config> findByEnvironmentIdAndEnabledAndEncrypted(Long environmentId, Boolean enabled, Boolean encrypted);

    /**
     * 根据描述模糊查询配置
     *
     * @param keyword 关键词
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.description LIKE %:keyword%")
    List<Config> findByDescriptionContaining(@Param("keyword") String keyword);

    /**
     * 根据环境ID和描述模糊查询配置
     *
     * @param environmentId 环境ID
     * @param keyword      关键词
     * @return 配置列表
     */
    @Query("SELECT c FROM Config c WHERE c.environment.id = :environmentId AND c.description LIKE %:keyword%")
    List<Config> findByEnvironmentIdAndDescriptionContaining(
            @Param("environmentId") Long environmentId,
            @Param("keyword") String keyword
    );

    /**
     * 统计指定环境的配置数量
     *
     * @param environmentId 环境ID
     * @return 配置数量
     */
    long countByEnvironmentId(Long environmentId);

    /**
     * 统计指定环境的启用配置数量
     *
     * @param environmentId 环境ID
     * @return 启用配置数量
     */
    long countByEnvironmentIdAndEnabledTrue(Long environmentId);

    /**
     * 统计指定环境的禁用配置数量
     *
     * @param environmentId 环境ID
     * @return 禁用配置数量
     */
    long countByEnvironmentIdAndEnabledFalse(Long environmentId);

    /**
     * 统计指定环境的加密配置数量
     *
     * @param environmentId 环境ID
     * @return 加密配置数量
     */
    long countByEnvironmentIdAndEncryptedTrue(Long environmentId);

    /**
     * 根据环境ID和启用状态查询配置（按创建时间倒序）
     *
     * @param environmentId 环境ID
     * @param enabled       启用状态
     * @return 配置列表（按创建时间倒序）
     */
    List<Config> findByEnvironmentIdAndEnabledOrderByCreatedAtDesc(Long environmentId, Boolean enabled);

    /**
     * 根据环境ID查询配置（按更新时间倒序）
     *
     * @param environmentId 环境ID
     * @return 配置列表（按更新时间倒序）
     */
    List<Config> findByEnvironmentIdOrderByUpdatedAtDesc(Long environmentId);
}
