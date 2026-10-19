package com.naengtori.repository;

import com.naengtori.entity.RecipeEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository
        extends JpaRepository<RecipeEntity, Long> {

    @EntityGraph(attributePaths = {
            "ingredients",
            "ingredients.ingredient"
    })
    List<RecipeEntity> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {
            "ingredients",
            "ingredients.ingredient"
    })
    List<RecipeEntity> findTop12ByOrderByViewCountDescIdAsc();

    @EntityGraph(attributePaths = {
            "ingredients",
            "ingredients.ingredient"
    })
    @Query("""
            SELECT DISTINCT recipe
            FROM RecipeEntity recipe
            LEFT JOIN recipe.ingredients recipeIngredient
            LEFT JOIN recipeIngredient.ingredient ingredient
            WHERE LOWER(recipe.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(ingredient.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY recipe.viewCount DESC, recipe.id ASC
            """)
    List<RecipeEntity> searchByRecipeOrIngredient(
            @Param("keyword") String keyword
    );

    @EntityGraph(attributePaths = {
            "ingredients",
            "ingredients.ingredient"
    })
    Optional<RecipeEntity> findOneById(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE RecipeEntity recipe
            SET recipe.viewCount = recipe.viewCount + 1
            WHERE recipe.id = :id
            """)
    int incrementViewCount(@Param("id") Long id);

    boolean existsByName(String name);
}
