ALTER TABLE fridge_ingredients
    ADD COLUMN member_id BIGINT;

ALTER TABLE fridge_ingredients
    DROP CONSTRAINT uq_fridge_ingredients_ingredient;

UPDATE fridge_ingredients
SET member_id = (SELECT MIN(id) FROM members);

INSERT INTO fridge_ingredients (member_id, ingredient_id)
SELECT member.id, fridge.ingredient_id
FROM members member
CROSS JOIN fridge_ingredients fridge
WHERE fridge.member_id = (SELECT MIN(id) FROM members)
  AND member.id <> (SELECT MIN(id) FROM members);

ALTER TABLE fridge_ingredients
    ALTER COLUMN member_id SET NOT NULL;

ALTER TABLE fridge_ingredients
    ADD CONSTRAINT fk_fridge_ingredients_member
        FOREIGN KEY (member_id) REFERENCES members (id),
    ADD CONSTRAINT uq_fridge_ingredients_member_ingredient
        UNIQUE (member_id, ingredient_id);

CREATE INDEX idx_fridge_ingredients_member
    ON fridge_ingredients (member_id, id);
