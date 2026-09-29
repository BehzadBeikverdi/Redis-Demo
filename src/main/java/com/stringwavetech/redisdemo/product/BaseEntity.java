package com.stringwavetech.redisdemo.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    public static final String PREFIX = "redis-demo__";

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "row_version", nullable = false)
    private int version;

    @Column(name = "created", nullable = false)
    @CreationTimestamp
    private LocalDateTime createDate;

    @Column(name = "updated", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updateDate;

    @PrePersist
    protected void onCreate() {
        updateDate = createDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }

}
