package com.naengtori.repository;

import com.naengtori.entity.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository
        extends JpaRepository<IngredientEntity, Long> {

    List<IngredientEntity> findAllByOrderByIdAsc();
    boolean existsByNameIgnoreCase(String name);
    Optional<IngredientEntity> findByNameIgnoreCase(String name);
}