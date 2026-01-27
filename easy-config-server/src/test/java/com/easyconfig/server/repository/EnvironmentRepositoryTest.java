package com.easyconfig.server.repository;

import com.easyconfig.server.entity.Environment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EnvironmentRepositoryTest {

    @Autowired
    private EnvironmentRepository environmentRepository;

    private Environment env1;
    private Environment env2;
    private Environment env3;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        // 清空数据库
        environmentRepository.deleteAll();

        // 准备测试数据
        env1 = Environment.builder()
                .name("dev1")
                .description("开发环境测试用")
                .sortOrder(1)
                .build();

        env2 = Environment.builder()
                .name("test1")
                .description("测试环境，用于集成测试")
                .sortOrder(2)
                .build();

        env3 = Environment.builder()
                .name("prod1")
                .description("生产环境，线上运行环境")
                .sortOrder(3)
                .build();

        //保存测试数据
        env1 = environmentRepository.save(env1);
        env2 = environmentRepository.save(env2);
        env3 = environmentRepository.save(env3);
    }

    @AfterEach
    void tearDown() {
        environmentRepository.deleteAll();
    }

    @Test
    void testFindByName_Success() {
        // 执行
        Optional<Environment> result = environmentRepository.findByName("dev1");
        
        // 验证
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("dev1");
        assertThat(result.get().getDescription()).isEqualTo("开发环境测试用");
    }

    @Test
    void testFindByName_NotFound() {
        // 执行
        Optional<Environment> result = environmentRepository.findByName("nonexistent");

        // 验证
        assertThat(result).isNotPresent();
    }

    // @Test
    // void testFindById_Success() {
    //     // 执行
    //     Optional<Environment> result = environmentRepository.findById(env1.getId());

    //     // 验证
    //     assertThat(result).isPresent();
    //     assertThat(result.get().getId()).isEqualTo(env1.getId());
    //     assertThat(result.get().getName()).isEqualTo("dev");
    // }

    @Test
    void testFindById_NotFound() {
        // 执行
        Optional<Environment> result = environmentRepository.findById(9999L);

        // 验证
        assertThat(result).isNotPresent();
    }

    @Test
    void testFindByDescription_KeywordMatch() {
        // 执行
        List<Environment> result = environmentRepository.findByDescription("测试");

        // 验证
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("name")
                .containsExactlyInAnyOrder("test1", "dev1");
    }
}
