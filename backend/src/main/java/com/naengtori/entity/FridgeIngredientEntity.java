package com.naengtori.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "fridge_ingredients",
        uniqueConstraints = @UniqueConstraint(
                columnNames = "ingredient_id"
        )
)
public class FridgeIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientEntity ingredient;

    public FridgeIngredientEntity(IngredientEntity ingredient) {
        this.ingredient = ingredient;
    }
}