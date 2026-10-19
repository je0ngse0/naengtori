package com.naengtori.controller;
import com.naengtori.dto.Ingredient;
import com.naengtori.service.IngredientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import com.naengtori.security.oauth2.OAuth2Constants;
import java.util.List;
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientService service;
    public IngredientController(IngredientService service) { this.service = service; }
    @GetMapping
    public List<Ingredient> findAll(
            @AuthenticationPrincipal OAuth2User principal
    ) {
        return service.findAll(memberId(principal));
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Ingredient add(
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody AddIngredientRequest request
    ) {
        return service.add(memberId(principal), request.name());
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable long id
    ) {
        service.delete(memberId(principal), id);
    }

    private Long memberId(OAuth2User principal) {
        return principal.getAttribute(OAuth2Constants.MEMBER_ID);
    }

    public record AddIngredientRequest(@NotBlank String name) {}
}
