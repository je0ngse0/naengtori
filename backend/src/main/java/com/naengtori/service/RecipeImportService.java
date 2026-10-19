package com.naengtori.service;

import com.naengtori.dto.RecipeImportData;
import com.naengtori.entity.IngredientEntity;
import com.naengtori.entity.RecipeEntity;
import com.naengtori.repository.IngredientRepository;
import com.naengtori.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeImportService {

    private final ObjectMapper objectMapper;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    @Transactional
    public int importRecipes(Path jsonPath) {
        List<RecipeImportData> recipeDataList =
                readRecipes(jsonPath);

        int importedCount = 0;

        for (RecipeImportData recipeData : recipeDataList) {
            if (recipeRepository.existsByName(recipeData.name())) {
                continue;
            }

            RecipeEntity recipe = new RecipeEntity(
                    recipeData.name(),
                    recipeData.minutes(),
                    recipeData.steps()
            );

            for (RecipeImportData.IngredientData ingredientData
                    : recipeData.ingredients()) {

                IngredientEntity ingredient =
                        findOrCreateIngredient(ingredientData.name());

                recipe.addIngredient(
                        ingredient,
                        ingredientData.quantity(),
                        ingredientData.unit()
                );
            }

            recipeRepository.save(recipe);
            importedCount++;
        }

        return importedCount;
    }

    private List<RecipeImportData> readRecipes(Path jsonPath) {
        try {
            return objectMapper.readValue(
                    jsonPath.toFile(),
                    new TypeReference<List<RecipeImportData>>() {
                    }
            );
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "레시피 JSON 파일을 읽을 수 없습니다: " + jsonPath,
                    exception
            );
        }
    }

    private IngredientEntity findOrCreateIngredient(String name) {
        String normalizedName = name.trim();

        return ingredientRepository
                .findByNameIgnoreCase(normalizedName)
                .orElseGet(() ->
                        ingredientRepository.save(
                                new IngredientEntity(normalizedName)
                        )
                );
    }
}