package com.easyconfig.server.service;

import com.easyconfig.server.entity.Config;
import com.easyconfig.server.entity.Environment;
import com.easyconfig.server.repository.ConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigService 单元测试")
class ConfigServiceTest {

    @Mock
    private ConfigRepository configRepository;

    @Mock
    private EnvironmentService environmentService;

    @InjectMocks
    private ConfigService configService;

    private Environment devEnv;
    private Environment testEnv;
    private Config config1;
    private Config config2;

    @BeforeEach
    void setUp() {
        devEnv = Environment.builder()
                .id(1L)
                .name("dev")
                .description("开发环境")
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testEnv = Environment.builder()
                .id(2L)
                .name("test")
                .description("测试环境")
                .sortOrder(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        config1 = Config.builder()
                .id(1L)
                .environment(devEnv)
                .configKey("app.name")
                .configValue("Easy-Config")
                .valueType("STRING")
                .description("应用名称")
                .groupName("basic")
                .encrypted(false)
                .enabled(true)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        config2 = Config.builder()
                .id(2L)
                .environment(devEnv)
                .configKey("db.password")
                .configValue("secret123")
                .valueType("STRING")
                .description("数据库密码")
                .groupName("database")
                .encrypted(true)
                .enabled(true)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("创建配置 - 成功")
    void createConfig_Success() {
        // Given
        Config newConfig = Config.builder()
                .environment(devEnv)
                .configKey("app.version")
                .configValue("1.0.0")
                .valueType("STRING")
                .build();

        when(environmentService.getEnvironmentById(1L)).thenReturn(devEnv);
        when(configRepository.findByEnvironmentIdAndConfigKey(1L, "app.version")).thenReturn(Optional.empty());
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> {
            Config saved = invocation.getArgument(0);
            saved.setId(3L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        // When
        Config result = configService.createConfig(newConfig);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getConfigKey()).isEqualTo("app.version");
        assertThat(result.getConfigValue()).isEqualTo("1.0.0");
        assertThat(result.getValueType()).isEqualTo("STRING");
        assertThat(result.getEncrypted()).isFalse();
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getVersion()).isEqualTo(1L);

        verify(environmentService).getEnvironmentById(1L);
        verify(configRepository).findByEnvironmentIdAndConfigKey(1L, "app.version");
        verify(configRepository).save(any(Config.class));
    }

    @Test
    @DisplayName("创建配置 - 环境ID无效")
    void createConfig_InvalidEnvironmentId() {
        // Given
        Config newConfig = Config.builder()
                .configKey("app.version")
                .configValue("1.0.0")
                .build();

        // When & Then
        assertThatThrownBy(() -> configService.createConfig(newConfig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Environment ID is required");

        verify(environmentService, never()).getEnvironmentById(anyLong());
        verify(configRepository, never()).save(any(Config.class));
    }

    @Test
    @DisplayName("创建配置 - 配置键已存在")
    void createConfig_KeyAlreadyExists() {
        // Given
        Config newConfig = Config.builder()
                .environment(devEnv)
                .configKey("app.name")
                .configValue("New Value")
                .build();

        when(environmentService.getEnvironmentById(1L)).thenReturn(devEnv);
        when(configRepository.findByEnvironmentIdAndConfigKey(1L, "app.name")).thenReturn(Optional.of(config1));

        // When & Then
        assertThatThrownBy(() -> configService.createConfig(newConfig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(configRepository, never()).save(any(Config.class));
    }

    @Test
    @DisplayName("创建配置 - 设置默认值")
    void createConfig_SetDefaults() {
        // Given
        Config newConfig = Config.builder()
                .environment(testEnv)
                .configKey("new.key")
                .configValue("value")
                .build();

        when(environmentService.getEnvironmentById(2L)).thenReturn(testEnv);
        when(configRepository.findByEnvironmentIdAndConfigKey(2L, "new.key")).thenReturn(Optional.empty());
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Config result = configService.createConfig(newConfig);

        // Then
        assertThat(result.getValueType()).isEqualTo("STRING");
        assertThat(result.getEncrypted()).isFalse();
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("更新配置 - 成功")
    void updateConfig_Success() {
        // Given
        Config updateConfig = Config.builder()
                .id(1L)
                .configKey("app.name.updated")
                .configValue("Updated Value")
                .description("更新后的描述")
                .build();

        when(configRepository.findById(1L)).thenReturn(Optional.of(config1));
        when(configRepository.findByEnvironmentIdAndConfigKey(1L, "app.name.updated")).thenReturn(Optional.empty());
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Config result = configService.updateConfig(1L, updateConfig);

        // Then
        assertThat(result.getConfigKey()).isEqualTo("app.name.updated");
        assertThat(result.getConfigValue()).isEqualTo("Updated Value");
        assertThat(result.getDescription()).isEqualTo("更新后的描述");
        assertThat(result.getVersion()).isEqualTo(2L);

        verify(configRepository).findById(1L);
        verify(configRepository).save(any(Config.class));
    }

    @Test
    @DisplayName("更新配置 - 配置不存在")
    void updateConfig_NotFound() {
        // Given
        Config updateConfig = Config.builder()
                .configKey("new.key")
                .build();

        when(configRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> configService.updateConfig(999L, updateConfig))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(configRepository).findById(999L);
        verify(configRepository, never()).save(any(Config.class));
    }

    @Test
    @DisplayName("更新配置 - 键名冲突")
    void updateConfig_KeyConflict() {
        // Given
        Config updateConfig = Config.builder()
                .id(1L)
                .configKey("db.password")
                .build();

        when(configRepository.findById(1L)).thenReturn(Optional.of(config1));
        when(configRepository.findByEnvironmentIdAndConfigKey(1L, "db.password")).thenReturn(Optional.of(config2));

        // When & Then
        assertThatThrownBy(() -> configService.updateConfig(1L, updateConfig))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(configRepository, never()).save(any(Config.class));
    }

    @Test
    @DisplayName("更新配置 - 更新环境")
    void updateConfig_UpdateEnvironment() {
        // Given
        Config updateConfig = Config.builder()
                .id(1L)
                .environment(testEnv)
                .build();

        when(configRepository.findById(1L)).thenReturn(Optional.of(config1));
        when(environmentService.getEnvironmentById(2L)).thenReturn(testEnv);
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Config result = configService.updateConfig(1L, updateConfig);

        // Then
        assertThat(result.getEnvironment()).isEqualTo(testEnv);
        verify(environmentService).getEnvironmentById(2L);
    }

    @Test
    @DisplayName("删除配置 - 成功")
    void deleteConfig_Success() {
        // Given
        when(configRepository.existsById(1L)).thenReturn(true);
        doNothing().when(configRepository).deleteById(1L);

        // When
        configService.deleteConfig(1L);

        // Then
        verify(configRepository).existsById(1L);
        verify(configRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除配置 - 不存在")
    void deleteConfig_NotFound() {
        // Given
        when(configRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> configService.deleteConfig(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(configRepository).existsById(999L);
        verify(configRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("启用配置")
    void enableConfig() {
        // Given
        config1.setEnabled(false);
        when(configRepository.findById(1L)).thenReturn(Optional.of(config1));
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Config result = configService.enableConfig(1L);

        // Then
        assertThat(result.getEnabled()).isTrue();
        verify(configRepository).findById(1L);
        verify(configRepository).save(any(Config.class));
    }

    @Test
    @DisplayName("禁用配置")
    void disableConfig() {
        // Given
        when(configRepository.findById(1L)).thenReturn(Optional.of(config1));
        when(configRepository.save(any(Config.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Config result = configService.disableConfig(1L);

        // Then
        assertThat(result.getEnabled()).isFalse();
        verify(configRepository).save(any(Config.class));
    }

    @Test
    @DisplayName("根据 ID 获取配置 - 成功")
    void getConfigById_Success() {
        // Given
        when(configRepository.findById(1L)).thenReturn(Optional.of(config1));

        // When
        Config result = configService.getConfigById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getConfigKey()).isEqualTo("app.name");

        verify(configRepository).findById(1L);
    }

    @Test
    @DisplayName("根据 ID 获取配置 - 不存在")
    void getConfigById_NotFound() {
        // Given
        when(configRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> configService.getConfigById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("根据环境ID和配置键获取配置")
    void getConfigByKey() {
        // Given
        when(configRepository.findByEnvironmentIdAndConfigKey(1L, "app.name")).thenReturn(Optional.of(config1));

        // When
        Config result = configService.getConfigByKey(1L, "app.name");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getConfigKey()).isEqualTo("app.name");

        verify(configRepository).findByEnvironmentIdAndConfigKey(1L, "app.name");
    }

    @Test
    @DisplayName("获取所有配置")
    void getAllConfigs() {
        // Given
        when(configRepository.findAll()).thenReturn(Arrays.asList(config1, config2));

        // When
        List<Config> result = configService.getAllConfigs();

        // Then
        assertThat(result).hasSize(2);
        verify(configRepository).findAll();
    }

    @Test
    @DisplayName("获取指定环境的所有配置")
    void getConfigsByEnvironment() {
        // Given
        when(configRepository.findByEnvironmentId(1L)).thenReturn(Arrays.asList(config1, config2));

        // When
        List<Config> result = configService.getConfigsByEnvironment(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(configRepository).findByEnvironmentId(1L);
    }

    @Test
    @DisplayName("获取指定环境的所有启用配置")
    void getEnabledConfigsByEnvironment() {
        // Given
        when(configRepository.findByEnvironmentIdAndEnabledTrue(1L)).thenReturn(Arrays.asList(config1));

        // When
        List<Config> result = configService.getEnabledConfigsByEnvironment(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(configRepository).findByEnvironmentIdAndEnabledTrue(1L);
    }

    @Test
    @DisplayName("获取所有加密配置")
    void getEncryptedConfigs() {
        // Given
        when(configRepository.findByEncryptedTrue()).thenReturn(Arrays.asList(config2));

        // When
        List<Config> result = configService.getEncryptedConfigs();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEncrypted()).isTrue();

        verify(configRepository).findByEncryptedTrue();
    }

    @Test
    @DisplayName("根据配置键模糊查询")
    void searchConfigsByKey() {
        // Given
        when(configRepository.findByConfigKeyContaining("app")).thenReturn(Arrays.asList(config1));

        // When
        List<Config> result = configService.searchConfigsByKey("app");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("app.name");

        verify(configRepository).findByConfigKeyContaining("app");
    }

    @Test
    @DisplayName("根据描述模糊查询")
    void searchConfigsByDescription() {
        // Given
        when(configRepository.findByDescriptionContaining("应用")).thenReturn(Arrays.asList(config1));

        // When
        List<Config> result = configService.searchConfigsByDescription("应用");

        // Then
        assertThat(result).hasSize(1);
        verify(configRepository).findByDescriptionContaining("应用");
    }

    @Test
    @DisplayName("根据分组查询配置")
    void getConfigsByGroup() {
        // Given
        when(configRepository.findByGroupName("basic")).thenReturn(Arrays.asList(config1));

        // When
        List<Config> result = configService.getConfigsByGroup("basic");

        // Then
        assertThat(result).hasSize(1);
        verify(configRepository).findByGroupName("basic");
    }

    @Test
    @DisplayName("根据值类型查询配置")
    void getConfigsByType() {
        // Given
        when(configRepository.findByValueType("STRING")).thenReturn(Arrays.asList(config1, config2));

        // When
        List<Config> result = configService.getConfigsByType("STRING");

        // Then
        assertThat(result).hasSize(2);
        verify(configRepository).findByValueType("STRING");
    }

    @Test
    @DisplayName("获取所有分组名称")
    void getAllDistinctGroups() {
        // Given
        when(configRepository.findAllDistinctGroupNames()).thenReturn(Arrays.asList("basic", "database"));

        // When
        List<String> result = configService.getAllDistinctGroups();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("basic", "database");

        verify(configRepository).findAllDistinctGroupNames();
    }

    @Test
    @DisplayName("统计指定环境的配置数量")
    void getConfigCountByEnvironment() {
        // Given
        when(configRepository.countByEnvironmentId(1L)).thenReturn(5L);

        // When
        long count = configService.getConfigCountByEnvironment(1L);

        // Then
        assertThat(count).isEqualTo(5L);
        verify(configRepository).countByEnvironmentId(1L);
    }

    @Test
    @DisplayName("统计指定环境的启用配置数量")
    void getEnabledConfigCountByEnvironment() {
        // Given
        when(configRepository.countByEnvironmentIdAndEnabledTrue(1L)).thenReturn(3L);

        // When
        long count = configService.getEnabledConfigCountByEnvironment(1L);

        // Then
        assertThat(count).isEqualTo(3L);
        verify(configRepository).countByEnvironmentIdAndEnabledTrue(1L);
    }

    @Test
    @DisplayName("统计指定环境的禁用配置数量")
    void getDisabledConfigCountByEnvironment() {
        // Given
        when(configRepository.countByEnvironmentIdAndEnabledFalse(1L)).thenReturn(2L);

        // When
        long count = configService.getDisabledConfigCountByEnvironment(1L);

        // Then
        assertThat(count).isEqualTo(2L);
        verify(configRepository).countByEnvironmentIdAndEnabledFalse(1L);
    }

    @Test
    @DisplayName("统计指定环境的加密配置数量")
    void getEncryptedConfigCountByEnvironment() {
        // Given
        when(configRepository.countByEnvironmentIdAndEncryptedTrue(1L)).thenReturn(1L);

        // When
        long count = configService.getEncryptedConfigCountByEnvironment(1L);

        // Then
        assertThat(count).isEqualTo(1L);
        verify(configRepository).countByEnvironmentIdAndEncryptedTrue(1L);
    }

    @Test
    @DisplayName("根据创建时间范围查询配置")
    void getConfigsByCreatedBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        when(configRepository.findByCreatedAtBetween(start, end)).thenReturn(Arrays.asList(config1, config2));

        // When
        List<Config> result = configService.getConfigsByCreatedBetween(start, end);

        // Then
        assertThat(result).hasSize(2);
        verify(configRepository).findByCreatedAtBetween(start, end);
    }

    @Test
    @DisplayName("根据环境ID和启用状态查询配置 - 按创建时间倒序")
    void getConfigsByEnvironmentAndEnabledOrderByCreatedDesc() {
        // Given
        when(configRepository.findByEnvironmentIdAndEnabledOrderByCreatedAtDesc(1L, true))
                .thenReturn(Arrays.asList(config2, config1));

        // When
        List<Config> result = configService.getConfigsByEnvironmentAndEnabledOrderByCreatedDesc(1L, true);

        // Then
        assertThat(result).hasSize(2);
        verify(configRepository).findByEnvironmentIdAndEnabledOrderByCreatedAtDesc(1L, true);
    }

    @Test
    @DisplayName("根据环境ID查询配置 - 按更新时间倒序")
    void getConfigsByEnvironmentOrderByUpdatedDesc() {
        // Given
        when(configRepository.findByEnvironmentIdOrderByUpdatedAtDesc(1L)).thenReturn(Arrays.asList(config1, config2));

        // When
        List<Config> result = configService.getConfigsByEnvironmentOrderByUpdatedDesc(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(configRepository).findByEnvironmentIdOrderByUpdatedAtDesc(1L);
    }

    @Test
    @DisplayName("检查配置键是否存在 - 存在")
    void existsByEnvironmentAndKey_True() {
        // Given
        when(configRepository.findByEnvironmentIdAndConfigKey(1L, "app.name")).thenReturn(Optional.of(config1));

        // When
        boolean result = configService.existsByEnvironmentAndKey(1L, "app.name");

        // Then
        assertThat(result).isTrue();
        verify(configRepository).findByEnvironmentIdAndConfigKey(1L, "app.name");
    }

    @Test
    @DisplayName("检查配置键是否存在 - 不存在")
    void existsByEnvironmentAndKey_False() {
        // Given
        when(configRepository.findByEnvironmentIdAndConfigKey(1L, "nonexistent")).thenReturn(Optional.empty());

        // When
        boolean result = configService.existsByEnvironmentAndKey(1L, "nonexistent");

        // Then
        assertThat(result).isFalse();
        verify(configRepository).findByEnvironmentIdAndConfigKey(1L, "nonexistent");
    }

    @Test
    @DisplayName("检查配置ID是否存在")
    void existsById() {
        // Given
        when(configRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = configService.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(configRepository).existsById(1L);
    }
}
