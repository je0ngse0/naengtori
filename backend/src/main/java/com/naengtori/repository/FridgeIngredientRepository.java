package com.naengtori.repository;

import com.naengtori.entity.FridgeIngredientEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeIngredientRepository
        extends JpaRepository<FridgeIngredientEntity, Long> {

    @EntityGraph(attributePaths = "ingredient")
    List<FridgeIngredientEntity> findAllByMemberIdOrderByIdAsc(
            Long memberId
    );

    boolean existsByMemberIdAndIngredientNameIgnoreCase(
            Long memberId,
            String name
    );

    Optional<FridgeIngredientEntity> findByIdAndMemberId(
            Long id,
            Long memberId
    );
}
