package com.github.alfredobaptista.adapter.out.persistence.adapter;

import com.github.alfredobaptista.adapter.out.persistence.entity.UrlAnalyticsJpaEntity;
import com.github.alfredobaptista.adapter.out.persistence.repository.SpringDataAnalyticsRepository;
import com.github.alfredobaptista.application.port.out.AnalyticsRepository;
import com.github.alfredobaptista.domain.model.UrlAnalytics;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AnalyticsPersistenceAdapter
        implements AnalyticsRepository {

    private final SpringDataAnalyticsRepository repository;

    public AnalyticsPersistenceAdapter(
            SpringDataAnalyticsRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public UrlAnalytics save(UrlAnalytics analytics) {

        UrlAnalyticsJpaEntity entity =
                new UrlAnalyticsJpaEntity();

        entity.setId(analytics.getId());
        entity.setShortCode(
                analytics.getShortCode().getValue()
        );
        entity.setAccessedAt(
                analytics.getAccessedAt()
        );

        UrlAnalyticsJpaEntity savedEntity =
                repository.save(entity);

        return new UrlAnalytics(
                savedEntity.getId(),
                new ShortCode(savedEntity.getShortCode()),
                savedEntity.getAccessedAt()
        );
    }

    @Override
    public long countByShortCode(ShortCode shortCode) {
        return repository.countByShortCode(
                shortCode.getValue()
        );
    }

    @Override
    public LocalDateTime findFirstAccessedAt(
            ShortCode shortCode
    ) {
        return repository
                .findFirstByShortCodeOrderByAccessedAtAsc(
                        shortCode.getValue()
                )
                .map(UrlAnalyticsJpaEntity::getAccessedAt)
                .orElse(null);
    }

    @Override
    public LocalDateTime findLastAccessedAt(
            ShortCode shortCode
    ) {
        return repository
                .findFirstByShortCodeOrderByAccessedAtDesc(
                        shortCode.getValue()
                )
                .map(UrlAnalyticsJpaEntity::getAccessedAt)
                .orElse(null);
    }
}