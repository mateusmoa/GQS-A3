package com.nutriapp.service;

import com.nutriapp.entity.Ingredient;
import com.nutriapp.repository.IngredientRepository;
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
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void save_validIngredient_callsSaveAndReturn() {
        Ingredient ing = new Ingredient();
        ing.setName("Teste");
        ing.setPortionUnit("g");
        ing.setEnergyKcal(BigDecimal.valueOf(123.45));

        when(ingredientRepository.save(any(Ingredient.class))).thenAnswer(inv -> {
            Ingredient arg = inv.getArgument(0);
            arg.setId(1L);
            return arg;
        });

        Ingredient saved = ingredientService.save(ing);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        assertEquals("Teste", saved.getName());
        verify(ingredientRepository, times(1)).save(any(Ingredient.class));
    }

    @Test
    void save_missingName_throwsIllegalArgumentException() {
        Ingredient ing = new Ingredient();
        ing.setPortionUnit("g");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ingredientService.save(ing));
        assertTrue(ex.getMessage().toLowerCase().contains("nome"));
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    void save_invalidPortionUnit_throwsIllegalArgumentException() {
        Ingredient ing = new Ingredient();
        ing.setName("X");
        ing.setPortionUnit("kg");

        assertThrows(IllegalArgumentException.class, () -> ingredientService.save(ing));
        verify(ingredientRepository, never()).save(any());
    }

    @Test
    void findAll_delegatesToRepository() {
        Ingredient a = new Ingredient(); a.setId(1L); a.setName("A");
        Ingredient b = new Ingredient(); b.setId(2L); b.setName("B");
        when(ingredientRepository.findAll()).thenReturn(List.of(a, b));

        List<Ingredient> list = ingredientService.findAll();

        assertEquals(2, list.size());
        verify(ingredientRepository, times(1)).findAll();
    }

    @Test
    void findById_delegatesToRepository() {
        Ingredient a = new Ingredient(); a.setId(1L); a.setName("A");
        when(ingredientRepository.findById(1L)).thenReturn(Optional.of(a));

        Optional<Ingredient> res = ingredientService.findById(1L);

        assertTrue(res.isPresent());
        assertEquals("A", res.get().getName());
        verify(ingredientRepository, times(1)).findById(1L);
    }
}
