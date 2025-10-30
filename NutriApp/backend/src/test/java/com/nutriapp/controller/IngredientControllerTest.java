package com.nutriapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriapp.entity.Ingredient;
import com.nutriapp.service.IngredientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IngredientControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IngredientService ingredientService;

    @InjectMocks
    private IngredientController ingredientController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(ingredientController).build();
    }

    @Test
    void getAll_returnsList() throws Exception {
        Ingredient a = new Ingredient(); a.setId(1L); a.setName("A"); a.setPortionUnit("g");
        when(ingredientService.findAll()).thenReturn(List.of(a));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ingredients").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void getById_found_returnsOk() throws Exception {
        Ingredient a = new Ingredient(); a.setId(1L); a.setName("A"); a.setPortionUnit("g");
        when(ingredientService.findById(1L)).thenReturn(Optional.of(a));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ingredients/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("A"));
    }

    @Test
    void create_valid_returnsCreated_andCallsServiceWithCorrectPayload() throws Exception {
        Ingredient in = new Ingredient(); in.setName("X"); in.setPortionUnit("g");
        Ingredient saved = new Ingredient(); saved.setId(10L); saved.setName("X"); saved.setPortionUnit("g");
        when(ingredientService.save(any(Ingredient.class))).thenReturn(saved);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        ArgumentCaptor<Ingredient> captor = ArgumentCaptor.forClass(Ingredient.class);
        verify(ingredientService, times(1)).save(captor.capture());
        assertEquals("X", captor.getValue().getName());
    }

    @Test
    void create_invalid_returnsBadRequest() throws Exception {
        Ingredient in = new Ingredient(); // missing name

        mockMvc.perform(MockMvcRequestBuilders.post("/api/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_existing_returnsOk() throws Exception {
        when(ingredientService.findById(1L)).thenReturn(Optional.of(new Ingredient()));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/ingredients/1"))
                .andExpect(status().isOk());

        verify(ingredientService, times(1)).deleteById(1L);
    }

    @Test
    void delete_notFound_returnsNotFound() throws Exception {
        when(ingredientService.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/ingredients/2"))
                .andExpect(status().isNotFound());

        verify(ingredientService, never()).deleteById(2L);
    }

    @Test
    void update_valid_returnsOk() throws Exception {
        Ingredient in = new Ingredient(); in.setName("Updated"); in.setPortionUnit("g");
        Ingredient updated = new Ingredient(); updated.setId(5L); updated.setName("Updated");
        when(ingredientService.update(eq(5L), any(Ingredient.class))).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/ingredients/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void update_notFound_returnsNotFound() throws Exception {
        Ingredient in = new Ingredient(); in.setName("Nope"); in.setPortionUnit("g");
        when(ingredientService.update(eq(99L), any(Ingredient.class))).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(MockMvcRequestBuilders.put("/api/ingredients/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_returnsList() throws Exception {
        Ingredient a = new Ingredient(); a.setId(3L); a.setName("Foo"); a.setPortionUnit("g");
        when(ingredientService.searchIngredients("foo")).thenReturn(List.of(a));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ingredients/search?q=foo").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Foo"));
    }

    @Test
    void count_returnsNumber() throws Exception {
        when(ingredientService.getTotalCount()).thenReturn(42L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ingredients/count").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(42));
    }

    @Test
    void categories_returnsList() throws Exception {
        when(ingredientService.getAllCategories()).thenReturn(List.of("Cat1", "Cat2"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/ingredients/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Cat1"));
    }
}
