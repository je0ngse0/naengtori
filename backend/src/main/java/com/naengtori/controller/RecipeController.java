package com.naengtori.controller;
import com.naengtori.dto.RecipeRecommendation;
import com.naengtori.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService service;
    public RecipeController(RecipeService service) { this.service = service; }
    @GetMapping("/recommendations")
    public List<RecipeRecommendation> recommendations() { return service.recommend(); }
}
