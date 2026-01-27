package com.easyconfig.server.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.easyconfig.server.entity.Environment;
import com.easyconfig.server.repository.EnvironmentRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EnvironmentService {
    @Autowired
    private EnvironmentRepository environmentRepository;

    public List<Environment> getEnvironmentsByDesc(String keyword) {
        return environmentRepository.findByDescription(keyword);
    }

    public Environment getEnvironmentByName(String name) {
        Optional<Environment> env = environmentRepository.findByName(name);
        return env.get();
    }
}
