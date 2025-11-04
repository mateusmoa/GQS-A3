package com.nutriapp.repository;

import com.nutriapp.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    List<Recipe> findByNameContainingIgnoreCase(String name);
    
    List<Recipe> findByPreparationMethod(String method);
    
    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients ri LEFT JOIN FETCH ri.ingredient WHERE r.id = :id")
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);
    
    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients")
    List<Recipe> findAllWithIngredients();
    
    @Query("SELECT COUNT(r) FROM Recipe r")
    Long countRecipes();
}