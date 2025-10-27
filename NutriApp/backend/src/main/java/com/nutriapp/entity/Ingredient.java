package com.nutriapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do ingrediente é obrigatório")
    @Column(nullable = false)
    private String name;
    
    @Column(name = "portion_unit", nullable = false, length = 2)
    private String portionUnit; // "g" ou "ml"
    
    // Valores nutricionais por 100g/100ml
    @Column(name = "energy_kcal", precision = 8, scale = 2)
    private BigDecimal energyKcal;
    
    @Column(name = "energy_kj", precision = 8, scale = 2)
    private BigDecimal energyKj;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal carbohydrates;
    
    @Column(name = "total_sugars", precision = 8, scale = 2)
    private BigDecimal totalSugars;
    
    @Column(name = "added_sugars", precision = 8, scale = 2)
    private BigDecimal addedSugars;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal proteins;
    
    @Column(name = "total_fats", precision = 8, scale = 2)
    private BigDecimal totalFats;
    
    @Column(name = "saturated_fats", precision = 8, scale = 2)
    private BigDecimal saturatedFats;
    
    @Column(name = "trans_fats", precision = 8, scale = 2)
    private BigDecimal transFats;
    
    @Column(name = "dietary_fiber", precision = 8, scale = 2)
    private BigDecimal dietaryFiber;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal sodium; // em mg
    
    @Column(name = "tbca_code", unique = true)
    private String tbcaCode;
    
    @Column(length = 100)
    private String category;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}