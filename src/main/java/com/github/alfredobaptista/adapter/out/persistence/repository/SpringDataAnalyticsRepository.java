package com.github.alfredobaptista.adapter.out.persistence.repository;

import com.github.alfredobaptista.adapter.out.persistence.entity.UrlAnalyticsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface SpringDataAnalyticsRepository
        extends JpaRepository<UrlAnalyticsJpaEntity, Long> {

    long countByShortCode(String shortCode);

    Optional<UrlAnalyticsJpaEntity> findFirstByShortCodeOrderByAccessedAtAsc(
            String shortCode
    );

    Optional<UrlAnalyticsJpaEntity> findFirstByShortCodeOrderByAccessedAtDesc(
            String shortCode
    );
}
