package com.nutriapp.repository;

import com.nutriapp.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    
    List<Ingredient> findByNameContainingIgnoreCase(String name);
    
    Optional<Ingredient> findByTbcaCode(String tbcaCode);
    
    List<Ingredient> findByCategory(String category);
    
    @Query("SELECT i FROM Ingredient i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.tbcaCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.category) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Ingredient> searchIngredients(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(i) FROM Ingredient i")
    Long countIngredients();
    
    @Query("SELECT DISTINCT i.category FROM Ingredient i WHERE i.category IS NOT NULL ORDER BY i.category")
    List<String> findAllCategories();
}