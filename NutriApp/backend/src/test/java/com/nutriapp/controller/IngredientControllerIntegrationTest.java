package com.nutriapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriapp.entity.Ingredient;
import com.nutriapp.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Teste de integração: IngredientController")
class IngredientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredientRepository ingredientRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST cria ingrediente e GET lista ingredientes")
    void postAndGet_createsIngredient() throws Exception {
        Ingredient in = new Ingredient();
        in.setName("IntTest");
        in.setPortionUnit("g");
        in.setEnergyKcal(new BigDecimal("50"));

        mockMvc.perform(post("/api/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated());

        Ingredient found = ingredientRepository.findByNameContainingIgnoreCase("IntTest").stream().findFirst().orElse(null);
        assertThat(found).isNotNull();
    
    assertThat(found.getEnergyKcal()).isEqualByComparingTo(new BigDecimal("50"));

        mockMvc.perform(get("/api/ingredients").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
