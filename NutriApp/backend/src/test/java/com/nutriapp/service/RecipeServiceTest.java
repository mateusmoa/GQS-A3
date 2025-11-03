package com.nutriapp.service;

import com.nutriapp.entity.Recipe;
import com.nutriapp.entity.RecipeIngredient;
import com.nutriapp.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void save_validRecipe_setsRelationAndSaves() {
        Recipe r = new Recipe();
        r.setName("Receita");
        r.setTotalPortion(BigDecimal.valueOf(100));
        RecipeIngredient ri = new RecipeIngredient(); ri.setQuantity(BigDecimal.valueOf(50));
        r.setIngredients(List.of(ri));

        when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
            Recipe arg = inv.getArgument(0);
            arg.setId(5L);
            return arg;
        });

        Recipe saved = recipeService.save(r);

        assertNotNull(saved);
        assertEquals(5L, saved.getId());
        assertEquals(saved, r);
        assertEquals(saved.getIngredients().get(0).getRecipe(), saved);
        verify(recipeRepository, times(1)).save(any(Recipe.class));
    }

    @Test
    void save_missingName_throws() {
        Recipe r = new Recipe();
        r.setTotalPortion(BigDecimal.valueOf(100));
        r.setIngredients(List.of(new RecipeIngredient()));

        assertThrows(IllegalArgumentException.class, () -> recipeService.save(r));
        verify(recipeRepository, never()).save(any());
    }

    @Test
    void findById_existing_returnsRecipe() {
        Recipe r = new Recipe(); r.setId(2L); r.setName("R");
        when(recipeRepository.findByIdWithIngredients(2L)).thenReturn(Optional.of(r));

        Recipe got = recipeService.findById(2L);

        assertEquals(2L, got.getId());
        verify(recipeRepository, times(1)).findByIdWithIngredients(2L);
    }
}
