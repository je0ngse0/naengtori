package com.naengtori.service;

import com.naengtori.dto.Ingredient;
import com.naengtori.entity.FridgeIngredientEntity;
import com.naengtori.entity.IngredientEntity;
import com.naengtori.repository.FridgeIngredientRepository;
import com.naengtori.repository.IngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final FridgeIngredientRepository fridgeRepository;

    public IngredientService(
            IngredientRepository ingredientRepository,
            FridgeIngredientRepository fridgeRepository
    ) {
        this.ingredientRepository = ingredientRepository;
        this.fridgeRepository = fridgeRepository;
    }

    public List<Ingredient> findAll() {
        return fridgeRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public Ingredient add(String rawName) {
        String name = rawName.trim();

        if (fridgeRepository.existsByIngredientNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "이미 냉장고에 등록된 식재료입니다."
            );
        }

        IngredientEntity ingredient =
                ingredientRepository.findByNameIgnoreCase(name)
                        .orElseGet(() ->
                                ingredientRepository.save(
                                        new IngredientEntity(name)
                                )
                        );

        FridgeIngredientEntity fridgeIngredient =
                fridgeRepository.save(
                        new FridgeIngredientEntity(ingredient)
                );

        return toDto(fridgeIngredient);
    }

    @Transactional
    public void delete(long id) {
        if (!fridgeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "냉장고에서 식재료를 찾을 수 없습니다."
            );
        }

        fridgeRepository.deleteById(id);
    }

    private Ingredient toDto(
            FridgeIngredientEntity fridgeIngredient
    ) {
        return new Ingredient(
                fridgeIngredient.getId(),
                fridgeIngredient.getIngredient().getName()
        );
    }
}