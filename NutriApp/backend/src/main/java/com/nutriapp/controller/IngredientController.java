package com.nutriapp.controller;

import com.nutriapp.entity.Ingredient;
import com.nutriapp.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ingredientes", description = "API para gerenciamento de ingredientes (TBCA)")
public class IngredientController {
    
    private final IngredientService ingredientService;
    
    @GetMapping
    @Operation(summary = "Listar todos os ingredientes", description = "Retorna lista completa de ingredientes da base TBCA")
    public ResponseEntity<List<Ingredient>> getAllIngredients() {
        log.info("GET /api/ingredients - Listar todos os ingredientes");
        List<Ingredient> ingredients = ingredientService.findAll();
        return ResponseEntity.ok(ingredients);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar ingrediente por ID")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        log.info("GET /api/ingredients/{} - Buscar ingrediente", id);
        return ingredientService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Criar novo ingrediente")
    public ResponseEntity<Ingredient> createIngredient(@Valid @RequestBody Ingredient ingredient) {
        log.info("POST /api/ingredients - Criar ingrediente: {}", ingredient.getName());
        try {
            Ingredient savedIngredient = ingredientService.save(ingredient);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedIngredient);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao criar ingrediente: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ingrediente existente")
    public ResponseEntity<Ingredient> updateIngredient(
            @PathVariable Long id,
            @Valid @RequestBody Ingredient ingredient) {
        
        log.info("PUT /api/ingredients/{} - Atualizar ingrediente", id);
        try {
            Ingredient updatedIngredient = ingredientService.update(id, ingredient);
            return ResponseEntity.ok(updatedIngredient);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar ingrediente: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar ingrediente")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        log.info("DELETE /api/ingredients/{} - Deletar ingrediente", id);
        if (ingredientService.findById(id).isPresent()) {
            ingredientService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/search")
    @Operation(summary = "Pesquisar ingredientes", description = "Busca por nome, código TBCA ou categoria")
    public ResponseEntity<List<Ingredient>> searchIngredients(@RequestParam String q) {
        log.info("GET /api/ingredients/search?q={}", q);
        List<Ingredient> ingredients = ingredientService.searchIngredients(q);
        return ResponseEntity.ok(ingredients);
    }
    
    @GetMapping("/count")
    @Operation(summary = "Contar total de ingredientes")
    public ResponseEntity<Long> getIngredientsCount() {
        log.info("GET /api/ingredients/count");
        Long count = ingredientService.getTotalCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/categories")
    @Operation(summary = "Listar categorias disponíveis")
    public ResponseEntity<List<String>> getCategories() {
        log.info("GET /api/ingredients/categories");
        List<String> categories = ingredientService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}