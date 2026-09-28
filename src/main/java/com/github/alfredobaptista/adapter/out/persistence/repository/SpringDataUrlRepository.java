package com.github.alfredobaptista.adapter.out.persistence.repository;

import com.github.alfredobaptista.adapter.out.persistence.entity.UrlJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUrlRepository
        extends JpaRepository<UrlJpaEntity, Long> {

    Optional<UrlJpaEntity> findByShortCode(String shortCode);
}