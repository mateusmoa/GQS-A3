package com.nutriapp.service;

import com.nutriapp.dto.NutritionTable;
import com.nutriapp.entity.Ingredient;
import com.nutriapp.entity.Recipe;
import com.nutriapp.entity.RecipeIngredient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class NutritionCalculationService {
    
    // Valores de referência ANVISA RDC 429/2020
    private static final Map<String, BigDecimal> DAILY_VALUES = Map.of(
        "energyKcal", new BigDecimal("2000"),
        "carbohydrates", new BigDecimal("300"),
        "totalSugars", new BigDecimal("50"),
        "addedSugars", new BigDecimal("50"),
        "proteins", new BigDecimal("50"),
        "totalFats", new BigDecimal("55"),
        "saturatedFats", new BigDecimal("22"),
        "dietaryFiber", new BigDecimal("25"),
        "sodium", new BigDecimal("2400")
    );
    
    // Fatores de correção por método de preparo
    private static final Map<String, Map<String, BigDecimal>> PREPARATION_FACTORS = Map.of(
        "FRIED", Map.of("fat", new BigDecimal("1.15"), "protein", BigDecimal.ONE, "vitamin", new BigDecimal("0.7")),
        "BOILED", Map.of("fat", new BigDecimal("0.95"), "protein", new BigDecimal("0.95"), "vitamin", new BigDecimal("0.8")),
        "BAKED", Map.of("fat", new BigDecimal("1.02"), "protein", BigDecimal.ONE, "vitamin", new BigDecimal("0.9")),
        "RAW", Map.of("fat", BigDecimal.ONE, "protein", BigDecimal.ONE, "vitamin", BigDecimal.ONE),
        "GRILLED", Map.of("fat", new BigDecimal("0.98"), "protein", BigDecimal.ONE, "vitamin", new BigDecimal("0.85")),
        "STEAMED", Map.of("fat", new BigDecimal("0.97"), "protein", new BigDecimal("0.98"), "vitamin", new BigDecimal("0.9"))
    );
    
    /**
     * Calcula a tabela nutricional completa de uma receita
     */
    public NutritionTable calculateRecipeNutrition(Recipe recipe) {
        log.info("Iniciando cálculo nutricional para receita: {} (ID: {})", recipe.getName(), recipe.getId());
        
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Receita deve ter pelo menos um ingrediente");
        }
        
        // Inicializar acumuladores
        BigDecimal totalEnergyKcal = BigDecimal.ZERO;
        BigDecimal totalEnergyKj = BigDecimal.ZERO;
        BigDecimal totalCarbohydrates = BigDecimal.ZERO;
        BigDecimal totalSugars = BigDecimal.ZERO;
        BigDecimal totalAddedSugars = BigDecimal.ZERO;
        BigDecimal totalProteins = BigDecimal.ZERO;
        BigDecimal totalFats = BigDecimal.ZERO;
        BigDecimal totalSaturatedFats = BigDecimal.ZERO;
        BigDecimal totalTransFats = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;
        BigDecimal totalSodium = BigDecimal.ZERO;
        
        // Obter fatores de correção
        Map<String, BigDecimal> factors = PREPARATION_FACTORS.getOrDefault(
            recipe.getPreparationMethod(), 
            Map.of("fat", BigDecimal.ONE, "protein", BigDecimal.ONE, "vitamin", BigDecimal.ONE)
        );
        
        log.debug("Método de preparo: {} - Fatores: {}", recipe.getPreparationMethod(), factors);
        
        // Somar nutrientes de todos os ingredientes
        for (RecipeIngredient recipeIngredient : recipe.getIngredients()) {
            Ingredient ingredient = recipeIngredient.getIngredient();
            BigDecimal quantity = recipeIngredient.getQuantity();
            
            log.debug("Processando ingrediente: {} - Quantidade: {}g", ingredient.getName(), quantity);
            
            // Proporção baseada em 100g/ml (valores TBCA são por 100g)
            BigDecimal proportion = quantity.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            
            // Somar valores energéticos
            totalEnergyKcal = totalEnergyKcal.add(safeMultiply(ingredient.getEnergyKcal(), proportion));
            totalEnergyKj = totalEnergyKj.add(safeMultiply(ingredient.getEnergyKj(), proportion));
            
            // Somar carboidratos e açúcares
            totalCarbohydrates = totalCarbohydrates.add(safeMultiply(ingredient.getCarbohydrates(), proportion));
            totalSugars = totalSugars.add(safeMultiply(ingredient.getTotalSugars(), proportion));
            totalAddedSugars = totalAddedSugars.add(safeMultiply(ingredient.getAddedSugars(), proportion));
            
            // Proteínas com fator de correção
            totalProteins = totalProteins.add(
                safeMultiply(ingredient.getProteins(), proportion).multiply(factors.get("protein"))
            );
            
            // Gorduras com fator de correção
            BigDecimal fatFactor = factors.get("fat");
            totalFats = totalFats.add(
                safeMultiply(ingredient.getTotalFats(), proportion).multiply(fatFactor)
            );
            totalSaturatedFats = totalSaturatedFats.add(
                safeMultiply(ingredient.getSaturatedFats(), proportion).multiply(fatFactor)
            );
            
            // Trans fat não é afetado pelo preparo
            totalTransFats = totalTransFats.add(safeMultiply(ingredient.getTransFats(), proportion));
            
            // Fibra e sódio
            totalFiber = totalFiber.add(safeMultiply(ingredient.getDietaryFiber(), proportion));
            totalSodium = totalSodium.add(safeMultiply(ingredient.getSodium(), proportion));
        }
        
        // Normalizar para 100g/ml (padrão ANVISA)
        BigDecimal normalizationFactor = new BigDecimal("100")
            .divide(recipe.getTotalPortion(), 4, RoundingMode.HALF_UP);
        
        log.debug("Fator de normalização: {} (Porção total: {}g)", normalizationFactor, recipe.getTotalPortion());
        
        // Criar tabela nutricional
        NutritionTable table = new NutritionTable();
        table.setRecipeId(recipe.getId());
        table.setRecipeName(recipe.getName());
        
        // Valores nutricionais normalizados para 100g
        table.setEnergyKcal(totalEnergyKcal.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setEnergyKj(totalEnergyKj.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setCarbohydrates(totalCarbohydrates.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setTotalSugars(totalSugars.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setAddedSugars(totalAddedSugars.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setProteins(totalProteins.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setTotalFats(totalFats.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setSaturatedFats(totalSaturatedFats.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setTransFats(totalTransFats.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setDietaryFiber(totalFiber.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        table.setSodium(totalSodium.multiply(normalizationFactor).setScale(2, RoundingMode.HALF_UP));
        
        // Calcular %VD
        calculateDailyValues(table);
        
        // Metadados
        table.setCalculatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        log.info("Cálculo concluído. Energia: {} kcal/100g, Proteínas: {}g/100g", 
                 table.getEnergyKcal(), table.getProteins());
        
        return table;
    }
    
    /**
     * Calcula os percentuais de Valor Diário (%VD) conforme ANVISA
     */
    private void calculateDailyValues(NutritionTable table) {
        table.setEnergyDV(calculatePercentage(table.getEnergyKcal(), DAILY_VALUES.get("energyKcal")));
        table.setCarbohydratesDV(calculatePercentage(table.getCarbohydrates(), DAILY_VALUES.get("carbohydrates")));
        table.setTotalSugarsDV(calculatePercentage(table.getTotalSugars(), DAILY_VALUES.get("totalSugars")));
        table.setAddedSugarsDV(calculatePercentage(table.getAddedSugars(), DAILY_VALUES.get("addedSugars")));
        table.setProteinsDV(calculatePercentage(table.getProteins(), DAILY_VALUES.get("proteins")));
        table.setTotalFatsDV(calculatePercentage(table.getTotalFats(), DAILY_VALUES.get("totalFats")));
        table.setSaturatedFatsDV(calculatePercentage(table.getSaturatedFats(), DAILY_VALUES.get("saturatedFats")));
        table.setDietaryFiberDV(calculatePercentage(table.getDietaryFiber(), DAILY_VALUES.get("dietaryFiber")));
        table.setSodiumDV(calculatePercentage(table.getSodium(), DAILY_VALUES.get("sodium")));
    }
    
    /**
     * Calcula percentual em relação ao valor de referência
     */
    private BigDecimal calculatePercentage(BigDecimal value, BigDecimal reference) {
        if (value == null || reference.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return value.divide(reference, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(1, RoundingMode.HALF_UP);
    }
    
    /**
     * Multiplicação segura (trata valores null)
     */
    private BigDecimal safeMultiply(BigDecimal value, BigDecimal multiplier) {
        return value != null ? value.multiply(multiplier) : BigDecimal.ZERO;
    }
}