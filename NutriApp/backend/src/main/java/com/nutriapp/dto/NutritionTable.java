package com.nutriapp.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class NutritionTable {
    private Long recipeId;
    private String recipeName;
    
    // Valores nutricionais por 100g
    private BigDecimal energyKcal;
    private BigDecimal energyKj;
    private BigDecimal carbohydrates;
    private BigDecimal totalSugars;
    private BigDecimal addedSugars;
    private BigDecimal proteins;
    private BigDecimal totalFats;
    private BigDecimal saturatedFats;
    private BigDecimal transFats;
    private BigDecimal dietaryFiber;
    private BigDecimal sodium;
    
    // Percentual do Valor Diário (%VD) - ANVISA
    private BigDecimal energyDV;
    private BigDecimal carbohydratesDV;
    private BigDecimal totalSugarsDV;
    private BigDecimal addedSugarsDV;
    private BigDecimal proteinsDV;
    private BigDecimal totalFatsDV;
    private BigDecimal saturatedFatsDV;
    private BigDecimal dietaryFiberDV;
    private BigDecimal sodiumDV;
    
    // Metadados
    private String anvisaVersion = "RDC-429-2020";
    private String calculatedAt;
}