package com.easyconfig.server.repository;

import com.easyconfig.server.entity.Config;
import com.easyconfig.server.entity.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConfigRepositoryTest {

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private EnvironmentRepository environmentRepository;

    private Environment devEnv;
    private Environment testEnv;
    private Config config1;
    private Config config2;
    private Config config3;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        configRepository.deleteAll();
        environmentRepository.deleteAll();

        devEnv = Environment.builder()
                .name("dev")
                .description("开发环境")
                .sortOrder(1)
                .build();

        testEnv = Environment.builder()
                .name("test")
                .description("测试环境")
                .sortOrder(2)
                .build();

        devEnv = environmentRepository.save(devEnv);
        testEnv = environmentRepository.save(testEnv);

        config1 = Config.builder()
                .environment(devEnv)
                .configKey("app.name")
                .configValue("Easy-Config")
                .valueType("STRING")
                .description("应用名称")
                .groupName("basic")
                .encrypted(false)
                .enabled(true)
                .version(1L)
                .build();

        config2 = Config.builder()
                .environment(devEnv)
                .configKey("app.version")
                .configValue("1.0.0")
                .valueType("STRING")
                .description("应用版本")
                .groupName("basic")
                .encrypted(false)
                .enabled(true)
                .version(1L)
                .build();

        config3 = Config.builder()
                .environment(testEnv)
                .configKey("db.password")
                .configValue("secret123")
                .valueType("STRING")
                .description("数据库密码")
                .groupName("database")
                .encrypted(true)
                .enabled(true)
                .version(1L)
                .build();

        config1 = configRepository.save(config1);
        config2 = configRepository.save(config2);
        config3 = configRepository.save(config3);
    }

    @AfterEach
    void tearDown() {
        configRepository.deleteAll();
        environmentRepository.deleteAll();
    }

    @Test
    void testFindByEnvironmentAndConfigKey_Success() {
        Optional<Config> result = configRepository.findByEnvironmentAndConfigKey(devEnv, "app.name");

        assertThat(result).isPresent();
        assertThat(result.get().getConfigKey()).isEqualTo("app.name");
        assertThat(result.get().getConfigValue()).isEqualTo("Easy-Config");
    }

    @Test
    void testFindByEnvironmentAndConfigKey_NotFound() {
        Optional<Config> result = configRepository.findByEnvironmentAndConfigKey(devEnv, "nonexistent");

        assertThat(result).isNotPresent();
    }

    @Test
    void testFindByEnvironmentIdAndConfigKey_Success() {
        Optional<Config> result = configRepository.findByEnvironmentIdAndConfigKey(devEnv.getId(), "app.name");

        assertThat(result).isPresent();
        assertThat(result.get().getConfigKey()).isEqualTo("app.name");
    }

    @Test
    void testFindByConfigKeyContaining_KeywordMatch() {
        List<Config> result = configRepository.findByConfigKeyContaining("app");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("configKey")
                .containsExactlyInAnyOrder("app.name", "app.version");
    }

    @Test
    void testFindByEnvironmentId_Success() {
        List<Config> result = configRepository.findByEnvironmentId(devEnv.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("configKey")
                .containsExactlyInAnyOrder("app.name", "app.version");
    }

    @Test
    void testFindByEnvironment_Success() {
        List<Config> result = configRepository.findByEnvironment(devEnv);

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindByEnvironmentIdAndGroupName_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndGroupName(devEnv.getId(), "basic");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("configKey")
                .containsExactlyInAnyOrder("app.name", "app.version");
    }

    @Test
    void testFindByGroupName_Success() {
        List<Config> result = configRepository.findByGroupName("basic");

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindAllDistinctGroupNames_Success() {
        List<String> result = configRepository.findAllDistinctGroupNames();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("basic", "database");
    }

    @Test
    void testFindByEnabledTrue_Success() {
        List<Config> result = configRepository.findByEnabledTrue();

        assertThat(result).hasSize(3);
    }

    @Test
    void testFindByEnabledFalse_Empty() {
        List<Config> result = configRepository.findByEnabledFalse();

        assertThat(result).isEmpty();
    }

    @Test
    void testFindByEnvironmentIdAndEnabledTrue_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndEnabledTrue(devEnv.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindByEncryptedTrue_Success() {
        List<Config> result = configRepository.findByEncryptedTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("db.password");
    }

    @Test
    void testFindByEnvironmentIdAndEncryptedTrue_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndEncryptedTrue(testEnv.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("db.password");
    }

    @Test
    void testFindByValueType_Success() {
        List<Config> result = configRepository.findByValueType("STRING");

        assertThat(result).hasSize(3);
    }

    @Test
    void testFindByEnvironmentIdAndValueType_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndValueType(devEnv.getId(), "STRING");

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindByEnvironmentIdAndVersion_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndVersion(devEnv.getId(), 1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindByCreatedAtBetween_Success() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        List<Config> result = configRepository.findByCreatedAtBetween(startTime, endTime);

        assertThat(result).hasSize(3);
    }

    @Test
    void testFindByEnvironmentIdAndCreatedAtBetween_Success() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        List<Config> result = configRepository.findByEnvironmentIdAndCreatedAtBetween(devEnv.getId(), startTime, endTime);

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindByUpdatedAtBetween_Success() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        List<Config> result = configRepository.findByUpdatedAtBetween(startTime, endTime);

        assertThat(result).hasSize(3);
    }

    @Test
    void testFindByEnvironmentIdAndUpdatedAtBetween_Success() {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        List<Config> result = configRepository.findByEnvironmentIdAndUpdatedAtBetween(devEnv.getId(), startTime, endTime);

        assertThat(result).hasSize(2);
    }

    @Test
    void testFindByEnvironmentIdAndEnabledAndGroupName_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndEnabledAndGroupName(devEnv.getId(), true, "basic");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("configKey")
                .containsExactlyInAnyOrder("app.name", "app.version");
    }

    @Test
    void testFindByEnvironmentIdAndEnabledAndEncrypted_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndEnabledAndEncrypted(testEnv.getId(), true, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfigKey()).isEqualTo("db.password");
    }

    @Test
    void testFindByDescriptionContaining_KeywordMatch() {
        List<Config> result = configRepository.findByDescriptionContaining("应用");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("configKey")
                .containsExactlyInAnyOrder("app.name", "app.version");
    }

    @Test
    void testFindByEnvironmentIdAndDescriptionContaining_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndDescriptionContaining(devEnv.getId(), "应用");

        assertThat(result).hasSize(2);
    }

    @Test
    void testCountByEnvironmentId_Success() {
        long count = configRepository.countByEnvironmentId(devEnv.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByEnvironmentIdAndEnabledTrue_Success() {
        long count = configRepository.countByEnvironmentIdAndEnabledTrue(devEnv.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testCountByEnvironmentIdAndEnabledFalse_Success() {
        long count = configRepository.countByEnvironmentIdAndEnabledFalse(devEnv.getId());

        assertThat(count).isEqualTo(0);
    }

    @Test
    void testCountByEnvironmentIdAndEncryptedTrue_Success() {
        long count = configRepository.countByEnvironmentIdAndEncryptedTrue(devEnv.getId());

        assertThat(count).isEqualTo(0);
    }

    @Test
    void testCountByEnvironmentIdAndEncryptedTrue_WithEncryptedConfig() {
        long count = configRepository.countByEnvironmentIdAndEncryptedTrue(testEnv.getId());

        assertThat(count).isEqualTo(1);
    }

    @Test
    void testFindByEnvironmentIdAndEnabledOrderByCreatedAtDesc_Success() {
        List<Config> result = configRepository.findByEnvironmentIdAndEnabledOrderByCreatedAtDesc(devEnv.getId(), true);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("configKey")
                .containsExactly("app.version", "app.name");
    }

    @Test
    void testFindByEnvironmentIdOrderByUpdatedAtDesc_Success() {
        List<Config> result = configRepository.findByEnvironmentIdOrderByUpdatedAtDesc(devEnv.getId());

        assertThat(result).hasSize(2);
    }
}
