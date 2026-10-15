package com.naengtori.repository;

import com.naengtori.entity.FridgeIngredientEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FridgeIngredientRepository
        extends JpaRepository<FridgeIngredientEntity, Long> {

    @EntityGraph(attributePaths = "ingredient")
    List<FridgeIngredientEntity> findAllByOrderByIdAsc();

    boolean existsByIngredientNameIgnoreCase(String name);
}