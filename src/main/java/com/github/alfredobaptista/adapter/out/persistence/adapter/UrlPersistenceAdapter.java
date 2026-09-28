package com.github.alfredobaptista.adapter.out.persistence.adapter;

import com.github.alfredobaptista.adapter.out.persistence.entity.UrlJpaEntity;
import com.github.alfredobaptista.adapter.out.persistence.repository.SpringDataUrlRepository;
import com.github.alfredobaptista.application.port.out.UrlRepository;
import com.github.alfredobaptista.domain.model.Url;
import com.github.alfredobaptista.domain.valueobject.OriginalUrl;
import com.github.alfredobaptista.domain.valueobject.ShortCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UrlPersistenceAdapter implements UrlRepository {

    private final SpringDataUrlRepository repository;

    public UrlPersistenceAdapter(
            SpringDataUrlRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Url save(Url url) {

        UrlJpaEntity entity = toJpaEntity(url);

        UrlJpaEntity savedEntity = repository.save(entity);

        return toDomainModel(savedEntity);
    }

    @Override
    public Optional<Url> findByShortCode(
            ShortCode shortCode
    ) {

        return repository.findByShortCode(
                shortCode.getValue()
        ).map(this::toDomainModel);
    }

    @Override
    public void deleteByShortCode(
            ShortCode shortCode
    ) {

        repository.findByShortCode(
                shortCode.getValue()
        ).ifPresent(repository::delete);
    }

    private UrlJpaEntity toJpaEntity(Url url) {

        UrlJpaEntity entity = new UrlJpaEntity();

        entity.setId(url.getId());

        entity.setShortCode(
                url.getShortCode().getValue()
        );

        entity.setOriginalUrl(
                url.getOriginalUrl().getValue()
        );

        entity.setCreatedAt(
                url.getCreatedAt()
        );

        entity.setExpiresAt(
                url.getExpiresAt()
        );

        return entity;
    }

    private Url toDomainModel(UrlJpaEntity entity) {

        ShortCode shortCode = new ShortCode(
                entity.getShortCode()
        );

        OriginalUrl originalUrl = new OriginalUrl(
                entity.getOriginalUrl()
        );

        return new Url(
                entity.getId(),
                shortCode,
                originalUrl,
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }
}