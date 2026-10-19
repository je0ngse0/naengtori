ALTER TABLE recipes
    ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_recipes_popular
    ON recipes (view_count DESC, id ASC);
