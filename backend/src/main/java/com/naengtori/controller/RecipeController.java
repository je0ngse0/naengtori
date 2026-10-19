package com.naengtori.controller;

import com.naengtori.dto.PublicRecipeResponse;
import com.naengtori.dto.RecipeRecommendation;
import com.naengtori.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.naengtori.security.oauth2.OAuth2Constants;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    @GetMapping("/recommendations")
    public List<RecipeRecommendation> recommendations(
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long memberId = principal.getAttribute(
                OAuth2Constants.MEMBER_ID
        );

        return service.recommend(memberId);
    }

    @GetMapping("/popular")
    public List<PublicRecipeResponse> popular() {
        return service.findPopular();
    }

    @GetMapping("/search")
    public List<PublicRecipeResponse> search(
            @RequestParam String ingredient
    ) {
        return service.search(ingredient);
    }

    @GetMapping("/public/{id}")
    public PublicRecipeResponse findPublicRecipe(
            @PathVariable Long id
    ) {
        return service.findPublicRecipe(id);
    }
}
