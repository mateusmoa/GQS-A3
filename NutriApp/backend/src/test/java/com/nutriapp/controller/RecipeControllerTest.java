package com.nutriapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriapp.entity.Recipe;
import com.nutriapp.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste de controlador: RecipeController")
class RecipeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private RecipeController recipeController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(recipeController).build();
    }

    @Test
    @DisplayName("GET /api/recipes retorna lista de todas as receitas")
    void getAll_returnsList() throws Exception {
        Recipe r = new Recipe();
        r.setId(1L);
        r.setName("Arroz");
        r.setPreparationMethod("RAW");
        r.setTotalPortion(new BigDecimal("200"));
        r.setPortionUnit("g");

        when(recipeService.findAll()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/recipes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arroz"))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/recipes/{id} quando encontrado retorna 200")
    void getById_found_returnsOk() throws Exception {
        Recipe r = new Recipe();
        r.setId(5L);
        r.setName("Salada");
        r.setPreparationMethod("RAW");
        r.setTotalPortion(new BigDecimal("150"));
        r.setPortionUnit("g");

        when(recipeService.findById(5L)).thenReturn(r);

        mockMvc.perform(get("/api/recipes/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salada"))
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("GET /api/recipes/{id} quando não encontrado retorna 404")
    void getById_notFound_returnsNotFound() throws Exception {
        when(recipeService.findById(99L)).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/api/recipes/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/recipes válido retorna Created (201)")
    void create_valid_returnsCreated() throws Exception {
        Recipe in = new Recipe();
        in.setName("Nova Receita");
        in.setPreparationMethod("COOKED");
        in.setTotalPortion(new BigDecimal("300"));
        in.setPortionUnit("g");

        Recipe saved = new Recipe();
        saved.setId(10L);
        saved.setName("Nova Receita");
        saved.setPreparationMethod("COOKED");
        saved.setTotalPortion(new BigDecimal("300"));
        saved.setPortionUnit("g");

        when(recipeService.save(any(Recipe.class))).thenReturn(saved);

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Nova Receita"));

        verify(recipeService, times(1)).save(any(Recipe.class));
    }

    @Test
    @DisplayName("POST /api/recipes sem nome retorna Bad Request")
    void create_missingName_returnsBadRequest() throws Exception {
        Recipe in = new Recipe();
        in.setPreparationMethod("RAW");
        in.setTotalPortion(new BigDecimal("100"));
        in.setPortionUnit("g");

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());

        verify(recipeService, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/recipes com IllegalArgumentException retorna Bad Request")
    void create_invalidData_returnsBadRequest() throws Exception {
        Recipe in = new Recipe();
        in.setName("Test");
        in.setPreparationMethod("INVALID");
        in.setTotalPortion(new BigDecimal("100"));
        in.setPortionUnit("g");

        when(recipeService.save(any(Recipe.class))).thenThrow(new IllegalArgumentException("invalid"));

        mockMvc.perform(post("/api/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/recipes/{id} válido retorna OK")
    void update_valid_returnsOk() throws Exception {
        Recipe in = new Recipe();
        in.setName("Receita Atualizada");
        in.setPreparationMethod("COOKED");
        in.setTotalPortion(new BigDecimal("250"));
        in.setPortionUnit("g");

        Recipe updated = new Recipe();
        updated.setId(7L);
        updated.setName("Receita Atualizada");
        updated.setPreparationMethod("COOKED");
        updated.setTotalPortion(new BigDecimal("250"));
        updated.setPortionUnit("g");

        when(recipeService.update(eq(7L), any(Recipe.class))).thenReturn(updated);

        mockMvc.perform(put("/api/recipes/7")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Receita Atualizada"));
    }

    @Test
    @DisplayName("PUT /api/recipes/{id} inexistente retorna Not Found")
    void update_notFound_returnsNotFound() throws Exception {
        Recipe in = new Recipe();
        in.setName("Nope");
        in.setPreparationMethod("RAW");
        in.setTotalPortion(new BigDecimal("100"));
        in.setPortionUnit("g");

        when(recipeService.update(eq(99L), any(Recipe.class))).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(put("/api/recipes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/recipes/{id} existente retorna OK")
    void delete_existing_returnsOk() throws Exception {
        doNothing().when(recipeService).deleteById(3L);

        mockMvc.perform(delete("/api/recipes/3"))
                .andExpect(status().isOk());

        verify(recipeService, times(1)).deleteById(3L);
    }

    @Test
    @DisplayName("DELETE /api/recipes/{id} inexistente retorna Not Found")
    void delete_notFound_returnsNotFound() throws Exception {
        doThrow(new RuntimeException("not found")).when(recipeService).deleteById(99L);

        mockMvc.perform(delete("/api/recipes/99"))
                .andExpect(status().isNotFound());

        verify(recipeService, times(1)).deleteById(99L);
    }

    @Test
    @DisplayName("GET /api/recipes/search retorna lista de receitas pesquisadas")
    void search_returnsList() throws Exception {
        Recipe r = new Recipe();
        r.setId(2L);
        r.setName("Frango");
        r.setPreparationMethod("COOKED");
        r.setTotalPortion(new BigDecimal("400"));
        r.setPortionUnit("g");

        when(recipeService.searchByName("frango")).thenReturn(List.of(r));

        mockMvc.perform(get("/api/recipes/search?q=frango").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Frango"));
    }

    @Test
    @DisplayName("GET /api/recipes/count retorna o número total de receitas")
    void count_returnsNumber() throws Exception {
        when(recipeService.getTotalCount()).thenReturn(15L);

        mockMvc.perform(get("/api/recipes/count").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(15));
    }
}
