package com.nutriapp.service;

import com.nutriapp.dto.NutritionTable;
import com.nutriapp.entity.Ingredient;
import com.nutriapp.entity.Recipe;
import com.nutriapp.entity.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutritionCalculationServiceTest {

    private final NutritionCalculationService calc = new NutritionCalculationService();

    @Test
    void calculateRecipeNutrition_singleIngredient_normalizesTo100g() {
        Ingredient ing = new Ingredient();
        ing.setName("TestIng");
        ing.setPortionUnit("g");
        ing.setEnergyKcal(BigDecimal.valueOf(200));
        ing.setCarbohydrates(BigDecimal.valueOf(20));
        ing.setProteins(BigDecimal.valueOf(10));
        ing.setTotalFats(BigDecimal.valueOf(5));

        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(ing);
        ri.setQuantity(BigDecimal.valueOf(50));

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("R1");
        recipe.setPreparationMethod("RAW");
        recipe.setTotalPortion(BigDecimal.valueOf(50)); 
        recipe.setIngredients(java.util.List.of(ri));

        NutritionTable table = calc.calculateRecipeNutrition(recipe);

        
        assertEquals(0, table.getEnergyKcal().compareTo(BigDecimal.valueOf(200).setScale(2)));

        assertEquals(0, table.getEnergyDV().compareTo(BigDecimal.valueOf(10.0).setScale(1)));
    }
}
