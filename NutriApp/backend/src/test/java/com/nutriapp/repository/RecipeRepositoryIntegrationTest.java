package com.nutriapp.repository;

import com.nutriapp.entity.Ingredient;
import com.nutriapp.entity.Recipe;
import com.nutriapp.entity.RecipeIngredient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Teste de integração: RecipeRepository")
class RecipeRepositoryIntegrationTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    @DisplayName("salvar e encontrar receita por id com ingredientes embutidos")
    void saveAndFindByIdWithIngredients() {
        // Arrange
        Ingredient ing = new Ingredient();
        ing.setName("Arroz");
        ing.setPortionUnit("g");
        ing.setEnergyKcal(new BigDecimal("130"));
        Ingredient saved = ingredientRepository.save(ing);

        Recipe recipe = new Recipe();
        recipe.setName("Arroz com Feijão");
        recipe.setPreparationMethod("RAW");
        recipe.setTotalPortion(new BigDecimal("200"));
        recipe.setPortionUnit("g");
        recipe.setServings(2);

        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(saved);
        ri.setQuantity(new BigDecimal("100"));
        recipe.addIngredient(ri);

        Recipe savedRecipe = recipeRepository.save(recipe);

        Optional<Recipe> found = recipeRepository.findByIdWithIngredients(savedRecipe.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Arroz com Feijão");
        assertThat(found.get().getIngredients()).hasSize(1);
        assertThat(found.get().getIngredients().get(0).getIngredient().getName()).isEqualTo("Arroz");
    }

    @Test
    @DisplayName("buscar todas as receitas com ingredientes embutidos")
    void findAllWithIngredients() {
        
        Ingredient ing1 = new Ingredient();
        ing1.setName("Feijão");
        ing1.setPortionUnit("g");
        ing1.setEnergyKcal(new BigDecimal("76"));
        ingredientRepository.save(ing1);

        Recipe r1 = new Recipe();
        r1.setName("Receita 1");
        r1.setPreparationMethod("RAW");
        r1.setTotalPortion(new BigDecimal("100"));
        r1.setPortionUnit("g");

        Recipe r2 = new Recipe();
        r2.setName("Receita 2");
        r2.setPreparationMethod("COOKED");
        r2.setTotalPortion(new BigDecimal("150"));
        r2.setPortionUnit("g");

        recipeRepository.saveAll(List.of(r1, r2));

        
        List<Recipe> all = recipeRepository.findAllWithIngredients();

        
        assertThat(all).hasSize(2);
        assertThat(all).extracting("name").contains("Receita 1", "Receita 2");
    }

    @Test
    @DisplayName("buscar receitas por nome (case-insensitive)")
    void findByNameContainingIgnoreCase() {
        
        Recipe r1 = new Recipe();
        r1.setName("Frango Assado");
        r1.setPreparationMethod("RAW");
        r1.setTotalPortion(new BigDecimal("300"));
        r1.setPortionUnit("g");

        Recipe r2 = new Recipe();
        r2.setName("Salada de Frango");
        r2.setPreparationMethod("RAW");
        r2.setTotalPortion(new BigDecimal("200"));
        r2.setPortionUnit("g");

        recipeRepository.saveAll(List.of(r1, r2));

        
        List<Recipe> byName = recipeRepository.findByNameContainingIgnoreCase("frango");

        
        assertThat(byName).hasSize(2);
        assertThat(byName).extracting("name").contains("Frango Assado", "Salada de Frango");
    }

    @Test
    @DisplayName("buscar receitas por método de preparo")
    void findByPreparationMethod() {
        
        Recipe r1 = new Recipe();
        r1.setName("Sopa");
        r1.setPreparationMethod("COOKED");
        r1.setTotalPortion(new BigDecimal("500"));
        r1.setPortionUnit("ml");

        Recipe r2 = new Recipe();
        r2.setName("Salada");
        r2.setPreparationMethod("RAW");
        r2.setTotalPortion(new BigDecimal("200"));
        r2.setPortionUnit("g");

        recipeRepository.saveAll(List.of(r1, r2));

        
        List<Recipe> cooked = recipeRepository.findByPreparationMethod("COOKED");

        
        assertThat(cooked).hasSize(1);
        assertThat(cooked.get(0).getName()).isEqualTo("Sopa");
    }

    @Test
    @DisplayName("contar receitas")
    void countRecipes() {
        
        Recipe r1 = new Recipe();
        r1.setName("Receita A");
        r1.setPreparationMethod("RAW");
        r1.setTotalPortion(new BigDecimal("100"));
        r1.setPortionUnit("g");

        Recipe r2 = new Recipe();
        r2.setName("Receita B");
        r2.setPreparationMethod("COOKED");
        r2.setTotalPortion(new BigDecimal("200"));
        r2.setPortionUnit("g");

        recipeRepository.saveAll(List.of(r1, r2));

        
        Long count = recipeRepository.countRecipes();

        assertThat(count).isGreaterThanOrEqualTo(2);
    }
}
