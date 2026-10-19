package com.naengtori.dto;

import java.math.BigDecimal;
import java.util.List;

public record RecipeImportData(
        String name,
        int minutes,
        List<String> steps,
        List<IngredientData> ingredients
) {

    public record IngredientData(
            String name,
            BigDecimal quantity,
            String unit
    ) {
    }
}