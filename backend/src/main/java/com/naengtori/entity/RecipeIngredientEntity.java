package com.naengtori.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "recipe_ingredients",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"recipe_id", "ingredient_id"}
        )
)
public class RecipeIngredientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private RecipeEntity recipe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private IngredientEntity ingredient;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(length = 20)
    private String unit;

    RecipeIngredientEntity(
            RecipeEntity recipe,
            IngredientEntity ingredient,
            BigDecimal quantity,
            String unit
    ) {
        this.recipe = recipe;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }
}