CREATE TABLE IF NOT EXISTS articles
(
    id          SERIAL PRIMARY KEY,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    body        TEXT         NOT NULL,
    author_id   INTEGER      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);