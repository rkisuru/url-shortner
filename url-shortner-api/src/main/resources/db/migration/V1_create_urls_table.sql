CREATE TABLE urls (
                      id BIGSERIAL PRIMARY KEY,
                      short_code VARCHAR(10) NOT NULL,
                      long_url TEXT NOT NULL,
                      user_id BIGINT,
                      created_at TIMESTAMP NOT NULL DEFAULT now(),
                      expires_at TIMESTAMP,
                      click_count BIGINT NOT NULL DEFAULT 0,
                      CONSTRAINT uq_short_code UNIQUE (short_code)
);

CREATE INDEX idx_urls_user_id ON urls (user_id);