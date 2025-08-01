--liquibase formatted sql

--changeset create-schema:0
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    chatId BIGINT UNIQUE
);

CREATE TABLE links (
    id BIGSERIAL PRIMARY KEY,
    url TEXT UNIQUE
);

CREATE TABLE userLinks (
    id BIGSERIAL PRIMARY KEY,
    linkId BIGINT REFERENCES links (id) ON DELETE CASCADE,
    userId BIGINT REFERENCES users (id) ON DELETE CASCADE,
    tags text[],
    filters text[]
);

CREATE TABLE linkUpdates (
    id BIGSERIAL PRIMARY KEY,
    linkId BIGINT REFERENCES links (id) ON DELETE CASCADE,
    topic TEXT,
    updatedAt TIMESTAMP,
    username TEXT,
    preview TEXT
);


CREATE INDEX users_index
ON users (chatId);

CREATE INDEX link_index
ON links (url);

CREATE INDEX user_link_index
ON userLinks (linkId, userId);

CREATE INDEX update_link_index
ON linkUpdates (linkId, updatedAt);
