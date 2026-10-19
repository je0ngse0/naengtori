package com.naengtori.dto;

import java.util.List;

public record PublicRecipeResponse(
        Long id,
        String name,
        int minutes,
        long viewCount,
        List<String> ingredients,
        List<String> steps
) {
}
