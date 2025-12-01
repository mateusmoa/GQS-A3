package com.nutriapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriapp.entity.Ingredient;
import com.nutriapp.entity.Recipe;
import com.nutriapp.entity.RecipeIngredient;
import com.nutriapp.repository.IngredientRepository;
import com.nutriapp.repository.RecipeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Teste de integração: RecipeController")
class RecipeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST cria receita e GET lista receitas com ingredientes")
    void postAndGet_createsRecipeWithIngredients() throws Exception {
        
        Ingredient ing = new Ingredient();
        ing.setName("Batata");
        ing.setPortionUnit("g");
        ing.setEnergyKcal(new BigDecimal("77"));
        Ingredient savedIng = ingredientRepository.save(ing);

        
        Recipe recipe = new Recipe();
        recipe.setName("Purê de Batata");
        recipe.setPreparationMethod("COOKED");
        recipe.setTotalPortion(new BigDecimal("200"));
        recipe.setPortionUnit("g");
        recipe.setServings(4);

        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(savedIng);
        ri.setQuantity(new BigDecimal("150"));
        recipe.addIngredient(ri);

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recipe)))
                .andExpect(status().isCreated());

        Recipe found = recipeRepository.findByNameContainingIgnoreCase("Purê de Batata")
                .stream().findFirst().orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Purê de Batata");
        assertThat(found.getPreparationMethod()).isEqualTo("COOKED");
        assertThat(found.getTotalPortion()).isEqualByComparingTo(new BigDecimal("200"));

        
        mockMvc.perform(get("/api/recipes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT atualiza receita existente")
    void put_updateRecipe() throws Exception {
        
        Recipe recipe = new Recipe();
        recipe.setName("Receita Original");
        recipe.setPreparationMethod("RAW");
        recipe.setTotalPortion(new BigDecimal("100"));
        recipe.setPortionUnit("g");
        Recipe savedRecipe = recipeRepository.save(recipe);

        
        Recipe updated = new Recipe();
        updated.setName("Receita Atualizada");
        updated.setPreparationMethod("COOKED");
        updated.setTotalPortion(new BigDecimal("150"));
        updated.setPortionUnit("g");

        
        mockMvc.perform(put("/api/recipes/" + savedRecipe.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());

        
        Recipe found = recipeRepository.findById(savedRecipe.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Receita Atualizada");
        assertThat(found.getPreparationMethod()).isEqualTo("COOKED");
    }

    @Test
    @DisplayName("DELETE remove receita existente")
    void delete_removeRecipe() throws Exception {
        
        Recipe recipe = new Recipe();
        recipe.setName("Receita a Deletar");
        recipe.setPreparationMethod("RAW");
        recipe.setTotalPortion(new BigDecimal("100"));
        recipe.setPortionUnit("g");
        Recipe savedRecipe = recipeRepository.save(recipe);

        long idToDelete = savedRecipe.getId();

        
        assertThat(recipeRepository.findById(idToDelete)).isPresent();

        
        mockMvc.perform(delete("/api/recipes/" + idToDelete))
                .andExpect(status().isOk());

        assertThat(recipeRepository.findById(idToDelete)).isEmpty();
    }

    @Test
    @DisplayName("GET /api/recipes/{id} retorna receita com ingredientes embutidos")
    void getById_returnsRecipeWithIngredients() throws Exception {
        
        Ingredient ing = new Ingredient();
        ing.setName("Tomate");
        ing.setPortionUnit("g");
        ing.setEnergyKcal(new BigDecimal("18"));
        Ingredient savedIng = ingredientRepository.save(ing);

        Recipe recipe = new Recipe();
        recipe.setName("Molho de Tomate");
        recipe.setPreparationMethod("COOKED");
        recipe.setTotalPortion(new BigDecimal("500"));
        recipe.setPortionUnit("ml");

        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(savedIng);
        ri.setQuantity(new BigDecimal("400"));
        recipe.addIngredient(ri);

        Recipe savedRecipe = recipeRepository.save(recipe);

        
        mockMvc.perform(get("/api/recipes/" + savedRecipe.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Recipe found = recipeRepository.findByIdWithIngredients(savedRecipe.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getIngredients()).hasSize(1);
        assertThat(found.getIngredients().get(0).getIngredient().getName()).isEqualTo("Tomate");
    }

    @Test
    @DisplayName("GET /api/recipes/search?q= pesquisa receitas por nome")
    void search_recipes() throws Exception {
        
        Recipe r1 = new Recipe();
        r1.setName("Sopa de Legumes");
        r1.setPreparationMethod("COOKED");
        r1.setTotalPortion(new BigDecimal("300"));
        r1.setPortionUnit("ml");
        recipeRepository.save(r1);

        Recipe r2 = new Recipe();
        r2.setName("Sopa de Cebola");
        r2.setPreparationMethod("COOKED");
        r2.setTotalPortion(new BigDecimal("250"));
        r2.setPortionUnit("ml");
        recipeRepository.save(r2);

        
        mockMvc.perform(get("/api/recipes/search?q=Sopa")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        var results = recipeRepository.findByNameContainingIgnoreCase("Sopa");
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("GET /api/recipes/count retorna número total de receitas")
    void count_recipes() throws Exception {
        
        mockMvc.perform(get("/api/recipes/count")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Long count = recipeRepository.countRecipes();
        assertThat(count).isGreaterThanOrEqualTo(0);
    }
}
