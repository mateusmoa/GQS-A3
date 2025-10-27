package com.nutriapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome da receita é obrigatório")
    @Column(nullable = false)
    private String name;
    
    @NotNull(message = "Método de preparo é obrigatório")
    @Column(name = "preparation_method", nullable = false, length = 20)
    private String preparationMethod; // RAW, BOILED, FRIED, BAKED, GRILLED, STEAMED
    
    @NotNull(message = "Porção total é obrigatória")
    @Column(name = "total_portion", precision = 8, scale = 2, nullable = false)
    private BigDecimal totalPortion;
    
    @Column(name = "portion_unit", nullable = false, length = 2)
    private String portionUnit; // "g" ou "ml"
    
    private Integer servings;
    
    @Column(columnDefinition = "TEXT")
    private String instructions;
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }
    
    public void removeIngredient(RecipeIngredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setRecipe(null);
    }
}
