package com.naengtori.config;

import com.naengtori.service.RecipeImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Profile("recipe-import")
@RequiredArgsConstructor
public class RecipeImportRunner implements ApplicationRunner {

    private final RecipeImportService recipeImportService;

    @Override
    public void run(ApplicationArguments args) {
        int importedCount = recipeImportService.importRecipes(
                Path.of("data/recipes.json")
        );

        System.out.println(
                "레시피 " + importedCount + "개를 가져왔습니다."
        );
    }
}