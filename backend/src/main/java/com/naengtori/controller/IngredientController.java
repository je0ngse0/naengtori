package com.naengtori.controller;
import com.naengtori.dto.Ingredient;
import com.naengtori.service.IngredientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientService service;
    public IngredientController(IngredientService service) { this.service = service; }
    @GetMapping public List<Ingredient> findAll() { return service.findAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Ingredient add(@Valid @RequestBody AddIngredientRequest request) { return service.add(request.name()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) { service.delete(id); }
    public record AddIngredientRequest(@NotBlank String name) {}
}
