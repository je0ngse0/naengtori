package com.naengtori.dto;

import java.util.List;
public record RecipeRecommendation(String name, int minutes, int ownedCount, int totalCount,
                                   List<String> missingIngredients, List<String> steps) {}
