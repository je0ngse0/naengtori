package com.naengtori.service;

import com.naengtori.dto.Ingredient;
import com.naengtori.dto.RecipeRecommendation;
import com.naengtori.entity.RecipeEntity;
import com.naengtori.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecipeService {

    private final IngredientService ingredientService;
    private final RecipeRepository recipeRepository;

    public RecipeService(
            IngredientService ingredientService,
            RecipeRepository recipeRepository
    ) {
        this.ingredientService = ingredientService;
        this.recipeRepository = recipeRepository;
    }

    public List<RecipeRecommendation> recommend() {
        Set<String> owned = ingredientService.findAll()
                .stream()
                .map(Ingredient::name)
                .collect(Collectors.toSet());

        return recipeRepository.findAllByOrderByIdAsc()
                .stream()
                .map(recipe -> createRecommendation(recipe, owned))
                .sorted(
                        Comparator
                                .comparingInt(
                                        (RecipeRecommendation item) ->
                                                item.missingIngredients().size()
                                )
                                .thenComparingInt(
                                        RecipeRecommendation::minutes
                                )
                )
                .toList();
    }

    private RecipeRecommendation createRecommendation(
            RecipeEntity recipe,
            Set<String> owned
    ) {
        List<String> required = recipe.getIngredients()
                .stream()
                .map(recipeIngredient ->
                        recipeIngredient.getIngredient().getName()
                )
                .toList();

        List<String> missing = required.stream()
                .filter(name -> !owned.contains(name))
                .toList();

        return new RecipeRecommendation(
                recipe.getName(),
                recipe.getMinutes(),
                required.size() - missing.size(),
                required.size(),
                missing,
                List.copyOf(recipe.getSteps())
        );
    }
}