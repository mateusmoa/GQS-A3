package com.nutriapp.service;

import com.nutriapp.entity.Ingredient;
import com.nutriapp.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IngredientService {
    
    private final IngredientRepository ingredientRepository;
    
    public List<Ingredient> findAll() {
        log.debug("Buscando todos os ingredientes");
        return ingredientRepository.findAll();
    }
    
    public Optional<Ingredient> findById(Long id) {
        log.debug("Buscando ingrediente por ID: {}", id);
        return ingredientRepository.findById(id);
    }
    
    public Ingredient save(Ingredient ingredient) {
        log.info("Salvando ingrediente: {}", ingredient.getName());
        validateIngredient(ingredient);
        return ingredientRepository.save(ingredient);
    }
    
    public Ingredient update(Long id, Ingredient ingredient) {
        log.info("Atualizando ingrediente ID: {}", id);
        return ingredientRepository.findById(id)
            .map(existing -> {
                ingredient.setId(id);
                ingredient.setCreatedAt(existing.getCreatedAt());
                return ingredientRepository.save(ingredient);
            })
            .orElseThrow(() -> new RuntimeException("Ingrediente não encontrado: " + id));
    }
    
    public void deleteById(Long id) {
        log.info("Deletando ingrediente ID: {}", id);
        ingredientRepository.deleteById(id);
    }
    
    public List<Ingredient> searchByName(String name) {
        log.debug("Buscando ingredientes por nome: {}", name);
        return ingredientRepository.findByNameContainingIgnoreCase(name);
    }
    
    public List<Ingredient> searchIngredients(String searchTerm) {
        log.debug("Pesquisa global: {}", searchTerm);
        return ingredientRepository.searchIngredients(searchTerm);
    }
    
    public List<String> getAllCategories() {
        return ingredientRepository.findAllCategories();
    }
    
    public Long getTotalCount() {
        return ingredientRepository.countIngredients();
    }
    
    private void validateIngredient(Ingredient ingredient) {
        if (ingredient.getName() == null || ingredient.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do ingrediente é obrigatório");
        }
        if (ingredient.getPortionUnit() == null || 
            (!ingredient.getPortionUnit().equals("g") && !ingredient.getPortionUnit().equals("ml"))) {
            throw new IllegalArgumentException("Unidade deve ser 'g' ou 'ml'");
        }
    }
}
