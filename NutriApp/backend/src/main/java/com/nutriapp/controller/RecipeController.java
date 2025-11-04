package com.nutriapp.controller;

import com.nutriapp.entity.Recipe;
import com.nutriapp.service.RecipeService;
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
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Receitas", description = "API para gerenciamento de receitas")
public class RecipeController {
    
    private final RecipeService recipeService;
    
    @GetMapping
    @Operation(summary = "Listar todas as receitas")
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        log.info("GET /api/recipes - Listar todas as receitas");
        List<Recipe> recipes = recipeService.findAll();
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Buscar receita por ID", description = "Retorna receita com todos os ingredientes")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        log.info("GET /api/recipes/{} - Buscar receita", id);
        try {
            Recipe recipe = recipeService.findById(id);
            return ResponseEntity.ok(recipe);
        } catch (RuntimeException e) {
            log.error("Receita não encontrada: {}", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @Operation(summary = "Criar nova receita")
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody Recipe recipe) {
        log.info("POST /api/recipes - Criar receita: {}", recipe.getName());
        try {
            Recipe savedRecipe = recipeService.save(recipe);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
        } catch (IllegalArgumentException e) {
            log.error("Erro ao criar receita: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar receita existente")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody Recipe recipe) {
        
        log.info("PUT /api/recipes/{} - Atualizar receita", id);
        try {
            Recipe updatedRecipe = recipeService.update(id, recipe);
            return ResponseEntity.ok(updatedRecipe);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar receita: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar receita")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        log.info("DELETE /api/recipes/{} - Deletar receita", id);
        try {
            recipeService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao deletar receita: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "Pesquisar receitas por nome")
    public ResponseEntity<List<Recipe>> searchRecipes(@RequestParam String q) {
        log.info("GET /api/recipes/search?q={}", q);
        List<Recipe> recipes = recipeService.searchByName(q);
        return ResponseEntity.ok(recipes);
    }
    
    @GetMapping("/count")
    @Operation(summary = "Contar total de receitas")
    public ResponseEntity<Long> getRecipesCount() {
        log.info("GET /api/recipes/count");
        Long count = recipeService.getTotalCount();
        return ResponseEntity.ok(count);
    }
}