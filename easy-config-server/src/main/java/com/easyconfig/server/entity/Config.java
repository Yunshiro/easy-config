package com.easyconfig.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"environment_id", "config_key"}, name = "uk_config_env_key")
}, indexes = {
    @Index(name = "idx_config_group", columnList = "group_name"),
    @Index(name = "idx_config_enabled", columnList = "enabled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "environment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_config_environment"))
    private Environment environment;

    @Column(name = "config_key", nullable = false, length = 200)
    private String configKey;

    @Lob
    @Column(name = "config_value", columnDefinition = "CLOB")
    private String configValue;

    @Column(name = "value_type", length = 20)
    @Builder.Default
    private String valueType = "STRING";

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "encrypted")
    @Builder.Default
    private Boolean encrypted = false;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "version")
    @Builder.Default
    private Long version = 1L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
