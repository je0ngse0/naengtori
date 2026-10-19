ALTER TABLE fridge_ingredients
    ADD COLUMN member_id BIGINT;

ALTER TABLE fridge_ingredients
    DROP CONSTRAINT uq_fridge_ingredients_ingredient;

DELETE FROM fridge_ingredients;

ALTER TABLE fridge_ingredients
    ALTER COLUMN member_id SET NOT NULL;

ALTER TABLE fridge_ingredients
    ADD CONSTRAINT fk_fridge_ingredients_member
        FOREIGN KEY (member_id) REFERENCES members (id),
    ADD CONSTRAINT uq_fridge_ingredients_member_ingredient
        UNIQUE (member_id, ingredient_id);

CREATE INDEX idx_fridge_ingredients_member
    ON fridge_ingredients (member_id, id);
