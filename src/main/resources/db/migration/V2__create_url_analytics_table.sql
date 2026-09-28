CREATE TABLE url_analytics (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_url_analytics_short_code
    ON url_analytics (short_code);

CREATE INDEX idx_url_analytics_accessed_at
    ON url_analytics (accessed_at);