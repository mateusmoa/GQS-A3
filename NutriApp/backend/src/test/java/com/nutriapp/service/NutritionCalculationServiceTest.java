package com.nutriapp.service;

import com.nutriapp.dto.NutritionTable;
import com.nutriapp.entity.Ingredient;
import com.nutriapp.entity.Recipe;
import com.nutriapp.entity.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Testes: NutritionCalculationService")
class NutritionCalculationServiceTest {

    @Autowired
    private NutritionCalculationService calculationService;

    private Recipe recipe;
    private Ingredient ingredient;

    @BeforeEach
    void setUp() {
        
        ingredient = new Ingredient();
        ingredient.setId(1L);
        ingredient.setName("Frango");
        ingredient.setCategory("Proteína");
        ingredient.setPortionUnit("g");
        ingredient.setEnergyKcal(new BigDecimal("165.00"));
        ingredient.setEnergyKj(new BigDecimal("690.00"));
        ingredient.setCarbohydrates(new BigDecimal("0.00"));
        ingredient.setTotalSugars(new BigDecimal("0.00"));
        ingredient.setAddedSugars(new BigDecimal("0.00"));
        ingredient.setProteins(new BigDecimal("31.00"));
        ingredient.setTotalFats(new BigDecimal("3.60"));
        ingredient.setSaturatedFats(new BigDecimal("1.30"));
        ingredient.setTransFats(new BigDecimal("0.00"));
        ingredient.setDietaryFiber(new BigDecimal("0.00"));
        ingredient.setSodium(new BigDecimal("74.00"));

        
        recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Frango Grelhado");
        recipe.setPreparationMethod("GRILLED");
        recipe.setTotalPortion(new BigDecimal("200"));
        recipe.setPortionUnit("g");
        recipe.setServings(2);
        recipe.setIngredients(new ArrayList<>());
        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(ingredient);
        ri.setQuantity(new BigDecimal("200"));
        recipe.addIngredient(ri);
    }

    @Test
    @DisplayName("calcula nutrição da receita com um ingrediente e normaliza para 100g")
    void calculateRecipeNutrition_singleIngredient_normalizesTo100g() {
        Ingredient ing = new Ingredient();
        ing.setName("TestIng");
        ing.setPortionUnit("g");
        ing.setEnergyKcal(BigDecimal.valueOf(200));
        ing.setCarbohydrates(BigDecimal.valueOf(20));
        ing.setProteins(BigDecimal.valueOf(10));
        ing.setTotalFats(BigDecimal.valueOf(5));
        ing.setSodium(BigDecimal.ZERO);
        ing.setDietaryFiber(BigDecimal.ZERO);

        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(ing);
        ri.setQuantity(BigDecimal.valueOf(50));

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("R1");
        recipe.setPreparationMethod("RAW");
        recipe.setTotalPortion(BigDecimal.valueOf(50)); 
        recipe.setIngredients(new ArrayList<>());
        recipe.addIngredient(ri);

        NutritionTable table = calculationService.calculateRecipeNutrition(recipe);

        assertEquals(0, table.getEnergyKcal().compareTo(BigDecimal.valueOf(200).setScale(2)));
        assertEquals(0, table.getEnergyDV().compareTo(BigDecimal.valueOf(10.0).setScale(1)));
    }

    @Test
    @DisplayName("Calcular nutrição com um ingrediente simples")
    void calculateRecipeNutrition_singleIngredient() {
        NutritionTable result = calculationService.calculateRecipeNutrition(recipe);

        assertThat(result).isNotNull();
        assertThat(result.getRecipeId()).isEqualTo(1L);
        assertThat(result.getRecipeName()).isEqualTo("Frango Grelhado");
        assertThat(result.getEnergyKcal()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getProteins()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getTotalFats()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calcular %VD conforme ANVISA RDC 429/2020")
    void calculateRecipeNutrition_dailyValuesCalculated() {
        NutritionTable result = calculationService.calculateRecipeNutrition(recipe);

        assertThat(result.getEnergyDV()).isNotNull();
        assertThat(result.getProteinsDV()).isNotNull();
        assertThat(result.getTotalFatsDV()).isNotNull();
        assertThat(result.getSodiumDV()).isNotNull();
        
        assertThat(result.getEnergyDV()).isBetween(BigDecimal.ZERO, new BigDecimal("200"));
        assertThat(result.getProteinsDV()).isBetween(BigDecimal.ZERO, new BigDecimal("200"));
    }

    @Test
    @DisplayName("Aplicar fator de correção por método de preparo GRILLED")
    void calculateRecipeNutrition_preparationFactorGrilled() {
        recipe.setPreparationMethod("GRILLED");
        NutritionTable grilled = calculationService.calculateRecipeNutrition(recipe);

        recipe.setPreparationMethod("RAW");
        NutritionTable raw = calculationService.calculateRecipeNutrition(recipe);

        assertThat(grilled.getTotalFats()).isLessThan(raw.getTotalFats());
    }

    @Test
    @DisplayName("Aplicar fator de correção por método de preparo FRIED")
    void calculateRecipeNutrition_preparationFactorFried() {
        recipe.setPreparationMethod("FRIED");
        NutritionTable fried = calculationService.calculateRecipeNutrition(recipe);

        recipe.setPreparationMethod("RAW");
        NutritionTable raw = calculationService.calculateRecipeNutrition(recipe);

        assertThat(fried.getTotalFats()).isGreaterThan(raw.getTotalFats());
    }

    @Test
    @DisplayName("Aplicar fator de correção por método de preparo BOILED")
    void calculateRecipeNutrition_preparationFactorBoiled() {
        recipe.setPreparationMethod("BOILED");
        NutritionTable boiled = calculationService.calculateRecipeNutrition(recipe);

        recipe.setPreparationMethod("RAW");
        NutritionTable raw = calculationService.calculateRecipeNutrition(recipe);

        assertThat(boiled.getTotalFats()).isLessThan(raw.getTotalFats());
    }

    @Test
    @DisplayName("Normalizar valores para 100g")
    void calculateRecipeNutrition_normalizeToHundredGrams() {
        recipe.setTotalPortion(new BigDecimal("200"));
        NutritionTable result = calculationService.calculateRecipeNutrition(recipe);

        assertThat(result.getEnergyKcal()).isNotNull();
        assertThat(result.getProteins()).isNotNull();
    }

    @Test
    @DisplayName("Tratar ingrediente com valores nulos")
    void calculateRecipeNutrition_nullNutrientValues() {
        ingredient.setCarbohydrates(null);
        ingredient.setDietaryFiber(null);

        NutritionTable result = calculationService.calculateRecipeNutrition(recipe);

        assertThat(result).isNotNull();
        assertThat(result.getCarbohydrates()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getDietaryFiber()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Erro ao calcular sem ingredientes")
    void calculateRecipeNutrition_noIngredients() {
        recipe.setIngredients(new ArrayList<>());

        assertThatThrownBy(() -> calculationService.calculateRecipeNutrition(recipe))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pelo menos um ingrediente");
    }

    @Test
    @DisplayName("Erro ao calcular com ingredientes nulo")
    void calculateRecipeNutrition_nullIngredients() {
        recipe.setIngredients(null);

        assertThatThrownBy(() -> calculationService.calculateRecipeNutrition(recipe))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pelo menos um ingrediente");
    }

    @Test
    @DisplayName("Calcular com múltiplos ingredientes")
    void calculateRecipeNutrition_multipleIngredients() {
        
        Ingredient rice = new Ingredient();
        rice.setId(2L);
        rice.setName("Arroz");
        rice.setEnergyKcal(new BigDecimal("130.00"));
        rice.setEnergyKj(new BigDecimal("545.00"));
        rice.setCarbohydrates(new BigDecimal("28.00"));
        rice.setTotalSugars(new BigDecimal("0.00"));
        rice.setAddedSugars(new BigDecimal("0.00"));
        rice.setProteins(new BigDecimal("2.70"));
        rice.setTotalFats(new BigDecimal("0.30"));
        rice.setSaturatedFats(new BigDecimal("0.10"));
        rice.setTransFats(new BigDecimal("0.00"));
        rice.setDietaryFiber(new BigDecimal("0.40"));
        rice.setSodium(new BigDecimal("0.00"));

        RecipeIngredient ri2 = new RecipeIngredient();
        ri2.setIngredient(rice);
        ri2.setQuantity(new BigDecimal("100"));
        recipe.addIngredient(ri2);

        recipe.setTotalPortion(new BigDecimal("300")); // 200g frango + 100g arroz

        NutritionTable result = calculationService.calculateRecipeNutrition(recipe);

        assertThat(result.getCarbohydrates()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getProteins()).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.getEnergyKcal()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Timestamp incluído no resultado")
    void calculateRecipeNutrition_timestampIncluded() {
        NutritionTable result = calculationService.calculateRecipeNutrition(recipe);

        assertThat(result.getCalculatedAt()).isNotNull();
        assertThat(result.getCalculatedAt()).isNotEmpty();
    }

    @Test
    @DisplayName("Calcular com diferentes métodos de preparo")
    void calculateRecipeNutrition_allPreparationMethods() {
        String[] methods = {"RAW", "GRILLED", "FRIED", "BOILED", "BAKED", "STEAMED"};

        for (String method : methods) {
            recipe.setPreparationMethod(method);
            NutritionTable result = calculationService.calculateRecipeNutrition(recipe);
            
            assertThat(result).isNotNull();
            assertThat(result.getTotalFats()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("Valores nutricionais com 2 casas decimais")
    void calculateRecipeNutrition_scaleToTwoDecimals() {
        NutritionTable result = calculationService.calculateRecipeNutrition(recipe);

        assertThat(result.getEnergyKcal().scale()).isEqualTo(2);
        assertThat(result.getProteins().scale()).isEqualTo(2);
        assertThat(result.getTotalFats().scale()).isEqualTo(2);
    }
}
