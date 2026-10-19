package com.naengtori.service;

import com.naengtori.dto.Ingredient;
import com.naengtori.dto.PublicRecipeResponse;
import com.naengtori.dto.RecipeRecommendation;
import com.naengtori.entity.RecipeEntity;
import com.naengtori.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private final IngredientService ingredientService;
    private final RecipeRepository recipeRepository;

    public List<RecipeRecommendation> recommend(Long memberId) {
        Set<String> owned = ingredientService.findAll(memberId)
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
                .limit(20)
                .toList();
    }

    public List<PublicRecipeResponse> findPopular() {
        return recipeRepository.findTop12ByOrderByViewCountDescIdAsc()
                .stream()
                .map(this::createPublicRecipe)
                .toList();
    }

    public List<PublicRecipeResponse> search(String ingredient) {
        String keyword = ingredient.trim();

        if (keyword.isEmpty()) {
            return List.of();
        }

        return recipeRepository.searchByRecipeOrIngredient(keyword)
                .stream()
                .limit(30)
                .map(this::createPublicRecipe)
                .toList();
    }

    @Transactional
    public PublicRecipeResponse findPublicRecipe(Long id) {
        RecipeEntity recipe = recipeRepository.findOneById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "레시피를 찾을 수 없습니다."
                ));

        recipeRepository.incrementViewCount(id);

        return createPublicRecipe(recipe, recipe.getViewCount() + 1);
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
                recipe.getId(),
                recipe.getName(),
                recipe.getMinutes(),
                required.size() - missing.size(),
                required.size(),
                missing,
                List.copyOf(recipe.getSteps())
        );
    }

    private PublicRecipeResponse createPublicRecipe(
            RecipeEntity recipe
    ) {
        return createPublicRecipe(recipe, recipe.getViewCount());
    }

    private PublicRecipeResponse createPublicRecipe(
            RecipeEntity recipe,
            long viewCount
    ) {
        List<String> ingredientNames = recipe.getIngredients()
                .stream()
                .map(recipeIngredient ->
                        recipeIngredient.getIngredient().getName()
                )
                .toList();

        return new PublicRecipeResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getMinutes(),
                viewCount,
                ingredientNames,
                List.copyOf(recipe.getSteps())
        );
    }
}
