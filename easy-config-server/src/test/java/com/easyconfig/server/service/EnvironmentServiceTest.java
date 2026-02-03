package com.easyconfig.server.service;

import com.easyconfig.server.entity.Environment;
import com.easyconfig.server.repository.EnvironmentRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnvironmentService 单元测试")
class EnvironmentServiceTest {

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private EnvironmentService environmentService;

    private Environment devEnv;
    private Environment testEnv;

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
    }

    @Test
    @DisplayName("创建环境 - 成功")
    void createEnvironment_Success() {
        // Given
        Environment newEnv = Environment.builder()
                .name("prod")
                .description("生产环境")
                .sortOrder(3)
                .build();

        when(environmentRepository.findByName("prod")).thenReturn(Optional.empty());
        when(environmentRepository.save(any(Environment.class))).thenAnswer(invocation -> {
            Environment saved = invocation.getArgument(0);
            saved.setId(3L);
            saved.setCreatedAt(LocalDateTime.now());
            saved.setUpdatedAt(LocalDateTime.now());
            return saved;
        });

        // When
        Environment result = environmentService.createEnvironment(newEnv);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("prod");
        assertThat(result.getDescription()).isEqualTo("生产环境");
        assertThat(result.getSortOrder()).isEqualTo(3);

        verify(environmentRepository).findByName("prod");
        verify(environmentRepository).save(any(Environment.class));
    }

    @Test
    @DisplayName("创建环境 - 名称已存在")
    void createEnvironment_NameAlreadyExists() {
        // Given
        Environment newEnv = Environment.builder()
                .name("dev")
                .description("重复的开发环境")
                .build();

        when(environmentRepository.findByName("dev")).thenReturn(Optional.of(devEnv));

        // When & Then
        assertThatThrownBy(() -> environmentService.createEnvironment(newEnv))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(environmentRepository).findByName("dev");
        verify(environmentRepository, never()).save(any(Environment.class));
    }

    @Test
    @DisplayName("创建环境 - 默认排序")
    void createEnvironment_DefaultSortOrder() {
        // Given
        Environment newEnv = Environment.builder()
                .name("uat")
                .description("预发布环境")
                .build();

        when(environmentRepository.findByName("uat")).thenReturn(Optional.empty());
        when(environmentRepository.save(any(Environment.class))).thenAnswer(invocation -> {
            Environment saved = invocation.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        // When
        Environment result = environmentService.createEnvironment(newEnv);

        // Then
        assertThat(result.getSortOrder()).isEqualTo(0);
        verify(environmentRepository).save(any(Environment.class));
    }

    @Test
    @DisplayName("更新环境 - 成功")
    void updateEnvironment_Success() {
        // Given
        Environment updateEnv = Environment.builder()
                .id(1L)
                .name("dev-updated")
                .description("开发环境（已更新）")
                .sortOrder(5)
                .build();

        lenient().when(environmentRepository.findById(1L)).thenReturn(Optional.of(devEnv));
        when(environmentRepository.findByName("dev-updated")).thenReturn(Optional.empty());
        when(environmentRepository.save(any(Environment.class))).thenReturn(updateEnv);

        // When
        Environment result = environmentService.updateEnvironment(1L, updateEnv);

        // Then
        assertThat(result.getName()).isEqualTo("dev-updated");
        assertThat(result.getDescription()).isEqualTo("开发环境（已更新）");
        assertThat(result.getSortOrder()).isEqualTo(5);

        verify(environmentRepository).findById(1L);
        verify(environmentRepository).findByName("dev-updated");
        verify(environmentRepository).save(any(Environment.class));
    }

    @Test
    @DisplayName("更新环境 - 环境不存在")
    void updateEnvironment_NotFound() {
        // Given
        Environment updateEnv = Environment.builder()
                .name("nonexistent")
                .build();

        when(environmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> environmentService.updateEnvironment(999L, updateEnv))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(environmentRepository).findById(999L);
        verify(environmentRepository, never()).save(any(Environment.class));
    }

    @Test
    @DisplayName("更新环境 - 名称冲突")
    void updateEnvironment_NameConflict() {
        // Given
        Environment updateEnv = Environment.builder()
                .id(1L)
                .name("test")
                .build();

        when(environmentRepository.findById(1L)).thenReturn(Optional.of(devEnv));
        when(environmentRepository.findByName("test")).thenReturn(Optional.of(testEnv));

        // When & Then
        assertThatThrownBy(() -> environmentService.updateEnvironment(1L, updateEnv))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(environmentRepository).findById(1L);
        verify(environmentRepository).findByName("test");
        verify(environmentRepository, never()).save(any(Environment.class));
    }

    @Test
    @DisplayName("删除环境 - 成功")
    void deleteEnvironment_Success() {
        // Given
        when(environmentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(environmentRepository).deleteById(1L);

        // When
        environmentService.deleteEnvironment(1L);

        // Then
        verify(environmentRepository).existsById(1L);
        verify(environmentRepository).deleteById(1L);
    }

    @Test
    @DisplayName("删除环境 - 环境不存在")
    void deleteEnvironment_NotFound() {
        // Given
        when(environmentRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> environmentService.deleteEnvironment(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(environmentRepository).existsById(999L);
        verify(environmentRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("根据 ID 获取环境 - 成功")
    void getEnvironmentById_Success() {
        // Given
        when(environmentRepository.findById(1L)).thenReturn(Optional.of(devEnv));

        // When
        Environment result = environmentService.getEnvironmentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("dev");

        verify(environmentRepository).findById(1L);
    }

    @Test
    @DisplayName("根据 ID 获取环境 - 不存在")
    void getEnvironmentById_NotFound() {
        // Given
        when(environmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> environmentService.getEnvironmentById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("根据名称获取环境 - 成功")
    void getEnvironmentByName_Success() {
        // Given
        when(environmentRepository.findByName("dev")).thenReturn(Optional.of(devEnv));

        // When
        Environment result = environmentService.getEnvironmentByName("dev");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("dev");

        verify(environmentRepository).findByName("dev");
    }

    @Test
    @DisplayName("获取所有环境")
    void getAllEnvironments() {
        // Given
        when(environmentRepository.findAll()).thenReturn(Arrays.asList(devEnv, testEnv));

        // When
        List<Environment> result = environmentService.getAllEnvironments();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("dev", "test");

        verify(environmentRepository).findAll();
    }

    @Test
    @DisplayName("获取所有环境 - 排序")
    void getAllEnvironmentsOrdered() {
        // Given
        when(environmentRepository.findAllByOrderBySortOrderAsc())
                .thenReturn(Arrays.asList(devEnv, testEnv));

        // When
        List<Environment> result = environmentService.getAllEnvironmentsOrdered();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("sortOrder").containsExactly(1, 2);

        verify(environmentRepository).findAllByOrderBySortOrderAsc();
    }

    @Test
    @DisplayName("根据描述搜索环境")
    void searchEnvironmentsByDescription() {
        // Given
        when(environmentRepository.findByDescription("测试")).thenReturn(Arrays.asList(devEnv, testEnv));

        // When
        List<Environment> result = environmentService.searchEnvironmentsByDescription("测试");

        // Then
        assertThat(result).hasSize(2);

        verify(environmentRepository).findByDescription("测试");
    }

    @Test
    @DisplayName("根据名称或描述搜索环境")
    void searchEnvironments() {
        // Given
        when(environmentRepository.findByNameContainingOrDescriptionContaining("dev", "dev"))
                .thenReturn(Arrays.asList(devEnv));

        // When
        List<Environment> result = environmentService.searchEnvironments("dev");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("dev");

        verify(environmentRepository).findByNameContainingOrDescriptionContaining("dev", "dev");
    }

    @Test
    @DisplayName("根据创建时间范围查询环境")
    void getEnvironmentsByCreatedBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        when(environmentRepository.findByCreatedAtBetween(start, end)).thenReturn(Arrays.asList(devEnv, testEnv));

        // When
        List<Environment> result = environmentService.getEnvironmentsByCreatedBetween(start, end);

        // Then
        assertThat(result).hasSize(2);
        verify(environmentRepository).findByCreatedAtBetween(start, end);
    }

    @Test
    @DisplayName("根据更新时间范围查询环境")
    void getEnvironmentsByUpdatedBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        when(environmentRepository.findByUpdatedAtBetween(start, end)).thenReturn(Arrays.asList(devEnv));

        // When
        List<Environment> result = environmentService.getEnvironmentsByUpdatedBetween(start, end);

        // Then
        assertThat(result).hasSize(1);
        verify(environmentRepository).findByUpdatedAtBetween(start, end);
    }

    @Test
    @DisplayName("统计环境数量")
    void getEnvironmentCount() {
        // Given
        when(environmentRepository.count()).thenReturn(3L);

        // When
        long count = environmentService.getEnvironmentCount();

        // Then
        assertThat(count).isEqualTo(3L);
        verify(environmentRepository).count();
    }

    @Test
    @DisplayName("检查环境名称是否存在 - 存在")
    void existsByName_True() {
        // Given
        when(environmentRepository.findByName("dev")).thenReturn(Optional.of(devEnv));

        // When
        boolean result = environmentService.existsByName("dev");

        // Then
        assertThat(result).isTrue();
        verify(environmentRepository).findByName("dev");
    }

    @Test
    @DisplayName("检查环境名称是否存在 - 不存在")
    void existsByName_False() {
        // Given
        when(environmentRepository.findByName("nonexistent")).thenReturn(Optional.empty());

        // When
        boolean result = environmentService.existsByName("nonexistent");

        // Then
        assertThat(result).isFalse();
        verify(environmentRepository).findByName("nonexistent");
    }

    @Test
    @DisplayName("检查环境 ID 是否存在 - 存在")
    void existsById_True() {
        // Given
        when(environmentRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = environmentService.existsById(1L);

        // Then
        assertThat(result).isTrue();
        verify(environmentRepository).existsById(1L);
    }

    @Test
    @DisplayName("检查环境 ID 是否存在 - 不存在")
    void existsById_False() {
        // Given
        when(environmentRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = environmentService.existsById(999L);

        // Then
        assertThat(result).isFalse();
        verify(environmentRepository).existsById(999L);
    }
}
