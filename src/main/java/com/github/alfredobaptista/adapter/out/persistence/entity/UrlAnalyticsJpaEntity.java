package com.github.alfredobaptista.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UrlAnalyticsJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "short_code",
            nullable = false,
            length = 10
    )
    private String shortCode;

    @Column(
            name = "accessed_at",
            nullable = false
    )
    private LocalDateTime accessedAt;
}