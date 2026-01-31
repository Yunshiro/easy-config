package com.easyconfig.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.easyconfig.server.entity.Environment;
import com.easyconfig.server.repository.EnvironmentRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * 环境服务类
 * <p>
 * 提供环境的完整 CRUD 操作，包括：
 * - 环境创建、更新、删除
 * - 环境查询（按 ID、名称、描述）
 * - 环境搜索（名称或描述模糊匹配）
 * - 环境排序
 */
@Service
@Transactional
public class EnvironmentService {

    @Autowired
    private EnvironmentRepository environmentRepository;

    /**
     * 创建环境
     *
     * @param environment 环境实体
     * @return 创建后的环境实体
     * @throws IllegalArgumentException 如果环境名称已存在
     */
    public Environment createEnvironment(Environment environment) {
        // 检查环境名称是否已存在
        Optional<Environment> existingEnv = environmentRepository.findByName(environment.getName());
        if (existingEnv.isPresent()) {
            throw new IllegalArgumentException("Environment with name '" + environment.getName() + "' already exists");
        }

        // 设置默认排序（如果未设置）
        if (environment.getSortOrder() == null) {
            environment.setSortOrder(0);
        }

        return environmentRepository.save(environment);
    }

    /**
     * 更新环境
     *
     * @param id          环境ID
     * @param environment 更新的环境实体
     * @return 更新后的环境实体
     * @throws EntityNotFoundException 如果环境不存在
     * @throws IllegalArgumentException 如果新名称与其他环境冲突
     */
    public Environment updateEnvironment(Long id, Environment environment) {
        Environment existingEnv = getEnvironmentById(id);

        // 如果修改了名称，检查新名称是否与其他环境冲突
        if (!existingEnv.getName().equals(environment.getName())) {
            Optional<Environment> envWithSameName = environmentRepository.findByName(environment.getName());
            if (envWithSameName.isPresent() && !envWithSameName.get().getId().equals(id)) {
                throw new IllegalArgumentException("Environment with name '" + environment.getName() + "' already exists");
            }
        }

        // 更新字段
        existingEnv.setName(environment.getName());
        existingEnv.setDescription(environment.getDescription());
        if (environment.getSortOrder() != null) {
            existingEnv.setSortOrder(environment.getSortOrder());
        }

        return environmentRepository.save(existingEnv);
    }

    /**
     * 删除环境
     *
     * @param id 环境ID
     * @throws EntityNotFoundException 如果环境不存在
     */
    public void deleteEnvironment(Long id) {
        if (!environmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Environment with id '" + id + "' not found");
        }
        environmentRepository.deleteById(id);
    }

    /**
     * 根据 ID 获取环境
     *
     * @param id 环境ID
     * @return 环境实体
     * @throws EntityNotFoundException 如果环境不存在
     */
    public Environment getEnvironmentById(Long id) {
        return environmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Environment with id '" + id + "' not found"));
    }

    /**
     * 根据名称获取环境
     *
     * @param name 环境名称
     * @return 环境实体
     * @throws EntityNotFoundException 如果环境不存在
     */
    public Environment getEnvironmentByName(String name) {
        return environmentRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Environment with name '" + name + "' not found"));
    }

    /**
     * 获取所有环境
     *
     * @return 环境列表
     */
    public List<Environment> getAllEnvironments() {
        return environmentRepository.findAll();
    }

    /**
     * 获取所有环境（按排序序号升序）
     *
     * @return 环境列表（按 sort_order 升序）
     */
    public List<Environment> getAllEnvironmentsOrdered() {
        return environmentRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * 根据描述搜索环境
     *
     * @param keyword 描述关键词
     * @return 环境列表
     */
    public List<Environment> searchEnvironmentsByDescription(String keyword) {
        return environmentRepository.findByDescription(keyword);
    }

    /**
     * 根据名称或描述搜索环境
     *
     * @param keyword 关键词（同时匹配名称和描述）
     * @return 环境列表
     */
    public List<Environment> searchEnvironments(String keyword) {
        return environmentRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    /**
     * 根据创建时间范围查询环境
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 环境列表
     */
    public List<Environment> getEnvironmentsByCreatedBetween(LocalDateTime start, LocalDateTime end) {
        return environmentRepository.findByCreatedAtBetween(start, end);
    }

    /**
     * 根据更新时间范围查询环境
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 环境列表
     */
    public List<Environment> getEnvironmentsByUpdatedBetween(LocalDateTime start, LocalDateTime end) {
        return environmentRepository.findByUpdatedAtBetween(start, end);
    }

    /**
     * 统计环境数量
     *
     * @return 环境总数
     */
    public long getEnvironmentCount() {
        return environmentRepository.count();
    }

    /**
     * 检查环境名称是否存在
     *
     * @param name 环境名称
     * @return true 表示存在，false 表示不存在
     */
    public boolean existsByName(String name) {
        return environmentRepository.findByName(name).isPresent();
    }

    /**
     * 检查环境 ID 是否存在
     *
     * @param id 环境ID
     * @return true 表示存在，false 表示不存在
     */
    public boolean existsById(Long id) {
        return environmentRepository.existsById(id);
    }
}
