package com.naengtori.repository;

import com.naengtori.entity.RecipeEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository
        extends JpaRepository<RecipeEntity, Long> {

    @EntityGraph(attributePaths = {
            "ingredients",
            "ingredients.ingredient"
    })
    List<RecipeEntity> findAllByOrderByIdAsc();

    boolean existsByName(String name);
}