package com.nutriapp.repository;

import com.nutriapp.entity.Ingredient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Teste de integração: IngredientRepository")
class IngredientRepositoryIntegrationTest {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    @DisplayName("salvar e encontrar por id")
    void saveAndFindById() {
        Ingredient ing = new Ingredient();
        ing.setName("TesteRepo");
        ing.setPortionUnit("g");
        ing.setEnergyKcal(new BigDecimal("10"));
        Ingredient saved = ingredientRepository.save(ing);

        Optional<Ingredient> found = ingredientRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TesteRepo");
    }

    @Test
    @DisplayName("busca e consultas customizadas")
    void customQueries() {
        Ingredient a = new Ingredient(); a.setName("Arroz Teste"); a.setPortionUnit("g");
        Ingredient b = new Ingredient(); b.setName("Feijao Teste"); b.setPortionUnit("g"); b.setCategory("Legume");
        ingredientRepository.saveAll(List.of(a, b));

        List<Ingredient> byName = ingredientRepository.findByNameContainingIgnoreCase("arroz");
        assertThat(byName).hasSize(1);

        List<String> categories = ingredientRepository.findAllCategories();
        assertThat(categories).contains("Legume");

        List<Ingredient> search = ingredientRepository.searchIngredients("Teste");
        assertThat(search).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("contar ingredientes")
    void countIngredients() {
        long before = ingredientRepository.count();
        Ingredient i = new Ingredient(); i.setName("Cnt"); i.setPortionUnit("g");
        ingredientRepository.save(i);
        Long count = ingredientRepository.countIngredients();
        assertThat(count).isGreaterThanOrEqualTo(before + 1);
    }
}
