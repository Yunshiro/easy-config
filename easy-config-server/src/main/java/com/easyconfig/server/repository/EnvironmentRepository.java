package com.easyconfig.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.easyconfig.server.entity.Environment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, Long> {

    Optional<Environment> findByName(String name);

    Optional<Environment> findById(long id);

    @Query("SELECT e FROM Environment e WHERE e.description LIKE %:keyword%")
    List<Environment> findByDescription(String keyword);

    List<Environment> findAllByOrderBySortOrderAsc();
    
    List<Environment> findByNameContainingOrDescriptionContaining(String nameKeyword, String descKeyword);

    List<Environment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Environment> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}