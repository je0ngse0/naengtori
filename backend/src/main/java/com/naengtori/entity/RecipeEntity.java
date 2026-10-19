package com.naengtori.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipes")
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private int minutes;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> steps = new ArrayList<>();

    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RecipeIngredientEntity> ingredients =
            new ArrayList<>();

    public RecipeEntity(
            String name,
            int minutes,
            List<String> steps
    ) {
        this.name = name;
        this.minutes = minutes;
        this.steps.addAll(steps);
    }

    public void addIngredient(
            IngredientEntity ingredient,
            BigDecimal quantity,
            String unit
    ) {
        ingredients.add(
                new RecipeIngredientEntity(
                        this,
                        ingredient,
                        quantity,
                        unit
                )
        );
    }
}
