package com.nutriapp.service;

import com.nutriapp.entity.Recipe;
import com.nutriapp.entity.RecipeIngredient;
import com.nutriapp.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    
    public List<Recipe> findAll() {
        log.debug("Buscando todas as receitas");
        return recipeRepository.findAll();
    }
    
    public Recipe findById(Long id) {
        log.debug("Buscando receita por ID: {}", id);
        return recipeRepository.findByIdWithIngredients(id)
            .orElseThrow(() -> new RuntimeException("Receita não encontrada: " + id));
    }
    
    public Recipe save(Recipe recipe) {
        log.info("Salvando receita: {}", recipe.getName());
        validateRecipe(recipe);
        
        // Configurar relacionamento bidirecional
        if (recipe.getIngredients() != null) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                ingredient.setRecipe(recipe);
            }
        }
        
        return recipeRepository.save(recipe);
    }
    
    public Recipe update(Long id, Recipe recipe) {
        log.info("Atualizando receita ID: {}", id);
        Recipe existing = findById(id);
        recipe.setId(id);
        recipe.setCreatedAt(existing.getCreatedAt());
        
        // Configurar relacionamento bidirecional
        if (recipe.getIngredients() != null) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                ingredient.setRecipe(recipe);
            }
        }
        
        return recipeRepository.save(recipe);
    }
    
    public void deleteById(Long id) {
        log.info("Deletando receita ID: {}", id);
        recipeRepository.deleteById(id);
    }
    
    public List<Recipe> searchByName(String name) {
        log.debug("Buscando receitas por nome: {}", name);
        return recipeRepository.findByNameContainingIgnoreCase(name);
    }
    
    public Long getTotalCount() {
        return recipeRepository.countRecipes();
    }
    
    private void validateRecipe(Recipe recipe) {
        if (recipe.getName() == null || recipe.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da receita é obrigatório");
        }
        if (recipe.getTotalPortion() == null || recipe.getTotalPortion().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Porção total deve ser maior que zero");
        }
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Receita deve ter pelo menos um ingrediente");
        }
    }
}